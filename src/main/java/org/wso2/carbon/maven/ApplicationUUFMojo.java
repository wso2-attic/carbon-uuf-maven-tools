/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.maven;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Create a UUF application artifact.
 *
 */
@Mojo(name = "create-application", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ApplicationUUFMojo extends AbstractUUFMojo {

    /**
     * The computed dependency tree root node of the Maven project.
     */
    private DependencyNode rootNode;

    /**
     * The dependency tree builder to use.
     */
    @Component(hint = "default") private DependencyGraphBuilder dependencyGraphBuilder;

    @Parameter(defaultValue = "2.1") private String dependencyPluginVersion;

    @Component private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
        unpackDependencies();
        createDependencyConfig();
        normalizeAppDependencies();
        super.execute();
    }

    private void unpackDependencies() throws MojoExecutionException {
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"),
                version(dependencyPluginVersion)), goal("unpack-dependencies"),
                configuration(element(name("outputDirectory"), getUUFTempDirectory().toString())),
                executionEnvironment(getProject(), getMavenSession(), pluginManager));
    }

    private void createDependencyConfig() throws MojoExecutionException {
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"),
                version(dependencyPluginVersion)), goal("tree"), configuration(element(name("verbose"), "true"),
                element(name("outputFile"), getUUFTempDirectory().resolve("dependency.tree").toString())),
                executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
    }

    @Override
    public Assembly getAssembly() {
        return createApplicationAssembly("make-application", "/" + getSimpleArtifactId());
    }

    private Assembly createApplicationAssembly(String assemblyId, String baseDirectory) {
        Assembly assembly = new Assembly();
        assembly.setId(assemblyId);
        assembly.setBaseDirectory(baseDirectory);
        FileSet fileSet1 = createFileSet(getBasedir().getAbsolutePath(), "/components/root");
        FileSet fileSet2 = createFileSet(getUUFTempDirectory().toString(), "/components/");
        assembly.setFileSets(createFileSetList(fileSet1, fileSet2));
        List<String> formatsList = new ArrayList<>();
        formatsList.add(UUF_ASSEMBLY_FORMAT);
        assembly.setFormats(formatsList);
        return assembly;
    }

    protected Path getUUFOsgiConfigOutDirectory() {
        return getUUFTempDirectory().resolve("root");
    }

    protected BuildPluginManager getPluginManager() {
        return this.pluginManager;
    }

    protected void normalizeAppDependencies() throws MojoExecutionException {
        try {
            Path rootDir = getUUFTempDirectory();
            EnumSet fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Path rootCompPath = getUUFTempDirectory().resolve("root");
            createDirectoryIfNotExists(rootCompPath);
            AppsVisitor appsVisitor = new AppsVisitor("**/components/root/**", rootCompPath, getUUFTempDirectory());
            Files.walkFileTree(rootDir, appsVisitor);
            for (Path application : appsVisitor.getApplications()) {
                FileUtils.deleteQuietly(application.toFile());
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error normalizing app dependencies", e);
        }
    }

    public class AppsVisitor extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final Path targetPath;
        private final Path rootPath;
        private final Set<Path> applications = new HashSet<>();
        private Log log;

        public AppsVisitor(String rootComponentPattern, Path target, Path root) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + rootComponentPattern);
            this.targetPath = target;
            this.rootPath = root;
        }

        private boolean isRootComponent(Path file) {
            return matcher.matches(file);
        }

        /**
         * This will return all applications found.
         * @return all applications
         */
        public Set<Path> getApplications() {
            return applications;
        }

        /**
         * This method will be called when visiting all *files* inside root component.
         * This will copy files into targetPath.
         * @param file visiting file
         * @param attrs file attributes
         * @return FileVisitResult
         * @throws IOException
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (isRootComponent(file)) {
                String relativePath = rootPath.relativize(file).toString();
                updateApplication(relativePath);
                try {
                    Files.copy(file, targetPath.resolve(removeAppName(relativePath)));
                } catch (FileAlreadyExistsException e) {
                    getLog().warn("File Already Exists! Ignoring `" + file + "`");
                }
            }
            return FileVisitResult.CONTINUE;
        }

        /**
         * This method will be called when visiting all *folders* inside root component.
         * This will create new folders in targetPath relative to the root component.
         * relative to the root component.
         * @param dir visiting directory
         * @param attrs directory attributes
         * @return FileVisitResult
         * @throws IOException
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (isRootComponent(dir)) {
                String relativePath = rootPath.relativize(dir).toString();
                updateApplication(relativePath);
                try {
                    Files.createDirectories(targetPath.resolve(removeAppName(relativePath)));
                } catch (FileAlreadyExistsException e) {
                    //ignore
                }
            }
            return FileVisitResult.CONTINUE;
        }

        private void updateApplication(String relativePath) {
            Path application = rootPath.resolve(relativePath.substring(0, relativePath.indexOf(File.separator)));
            applications.add(application);
        }

        private String removeAppName(String relativePath) {
            int thirdSlash = indexOfNthOccurrence(relativePath, File.separator, 3);
            return relativePath.substring(thirdSlash + 1);
        }

        private int indexOfNthOccurrence(String str, String toFind, int occurrence) {
            int index = str.indexOf(toFind);
            int found = 0;
            while (++found < occurrence && index > 0) {
                index = str.indexOf(toFind, index + 1);
            }
            return index;
        }

        private Log getLog() {
            if (this.log == null) {
                this.log = new SystemStreamLog();
            }
            return this.log;
        }
    }
}
