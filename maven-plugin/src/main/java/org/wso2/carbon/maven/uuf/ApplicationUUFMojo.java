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

package org.wso2.carbon.maven.uuf;

import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.wso2.carbon.maven.uuf.util.MojoUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 */
@Mojo(name = "create-application", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ApplicationUUFMojo extends AbstractUUFMojo {

    private static final String KEY_DEPENDENCY_TREE_FILE = "dependency.tree";
    /**
     * The computed dependency tree root node of the Maven project.
     */
    private DependencyNode rootNode;

    /**
     * The dependency tree builder to use.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * The dependency plugin version to use.
     */
    @Parameter(defaultValue = "2.1")
    private String dependencyPluginVersion;

    /**
     * Plugin manager to unpack the dependencies.
     */
    @Component
    private BuildPluginManager pluginManager;

    private static DependencyHolder getDependencies(Path rootDir) throws IOException {
        Set<Path> components = new HashSet<>();
        Set<Path> themes = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDir, new DirectoriesFilter())) {
            for (Path dir : directoryStream) {
                System.out.println(dir.getFileName());
                if (Files.exists(dir.resolve("theme.yaml"))) {
                    themes.add(dir);
                } else {
                    components.add(dir);
                }
            }
        }
        return new DependencyHolder(components, themes);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        unpackDependencies();
        createDependencyConfig("::" + UUF_THEME_ASSEMBLY_FORMAT + ":");
        normalizeAppDependencies();
        super.execute();
    }

    private void unpackDependencies() throws MojoExecutionException {
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"),
                           version(dependencyPluginVersion)), goal("unpack-dependencies"),
                    configuration(element(name("outputDirectory"), getUUFTempDirectory().toString())),
                    executionEnvironment(getProject(), getMavenSession(), pluginManager));
    }

    private void createDependencyConfig(String excludes) throws MojoExecutionException {
        executeMojo(plugin(groupId("org.apache.maven.plugins"), artifactId("maven-dependency-plugin"),
                           version(dependencyPluginVersion)), goal("tree"),
                    configuration(element(name("verbose"), "true"),
                                  element(name("outputFile"),
                                          getUUFTempDirectory().resolve(KEY_DEPENDENCY_TREE_FILE).toString()),
                                  element(name("excludes"), excludes)),
                    executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
    }

    @Override
    public Assembly getAssembly() throws MojoFailureException {
        return createApplicationAssembly("make-application", "/" + getArtifactId());
    }

    private Assembly createApplicationAssembly(String assemblyId, String baseDirectory) throws MojoFailureException {
        Assembly assembly = new Assembly();
        assembly.setId(assemblyId);
        assembly.setBaseDirectory(baseDirectory);
        List<FileSet> fileSets = new ArrayList<>();
        fileSets.add(createFileSet(getBasedir().getAbsolutePath(), "./components/root"));

        Path uufTempDirectory = getUUFTempDirectory();
        try {
            DependencyHolder dependencies = getDependencies(uufTempDirectory);
            for (Path currentTheme : dependencies.getThemes()) {
                fileSets.add(createFileSet(currentTheme.toString(), "./themes/" + currentTheme.getFileName()));
            }
            for (Path currentComponent : dependencies.getComponents()) {
                fileSets.add(
                        createFileSet(currentComponent.toString(), "./components/" + currentComponent.getFileName()));
            }
        } catch (IOException e) {
            throw new MojoFailureException(
                    "Error occurred while reading extracted dependencies on '" + uufTempDirectory.toString() + "'");
        }
        assembly.setFileSets(fileSets);

        ArrayList<FileItem> fileItems = new ArrayList<>();
        FileItem fileItem = new FileItem();
        fileItem.setSource(uufTempDirectory.resolve(KEY_DEPENDENCY_TREE_FILE).toString());
        fileItem.setOutputDirectory("./components/");
        fileItems.add(fileItem);
        assembly.setFiles(fileItems);

        List<String> formatsList = new ArrayList<>();
        formatsList.add(UUF_COMPONENT_ASSEMBLY_FORMAT);
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
            Path rootCompPath = getUUFTempDirectory().resolve("root");
            createDirectoryIfNotExists(rootCompPath);
            MojoUtils.AppsFinder appsFinder = new MojoUtils.AppsFinder("**/components/root/**", rootCompPath,
                                                                       getUUFTempDirectory());
            Files.walkFileTree(rootDir, appsFinder);
            appsFinder.deleteMatchedApplications();
        } catch (IOException e) {
            throw new MojoExecutionException("Error normalizing app dependencies", e);
        }
    }

    private static class DependencyHolder {
        private final Set<Path> components;
        private final Set<Path> themes;

        public DependencyHolder(Set<Path> components, Set<Path> themes) {
            this.components = components;
            this.themes = themes;
        }

        public Set<Path> getComponents() {
            return components;
        }

        public Set<Path> getThemes() {
            return themes;
        }
    }

    private static class DirectoriesFilter implements DirectoryStream.Filter<Path> {
        @Override
        public boolean accept(Path entry) throws IOException {
            return Files.isDirectory(entry);
        }
    }
}
