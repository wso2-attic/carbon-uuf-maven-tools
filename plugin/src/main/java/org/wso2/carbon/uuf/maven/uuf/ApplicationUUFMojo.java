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

package org.wso2.carbon.uuf.maven.uuf;

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
import org.wso2.carbon.uuf.maven.util.AppsFinder;

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
    private static final String ROOT_COMPONENT_NAME = "root";
    private static final String COMPONENTS_NAME = "components";
    private static final String THEMES_PATH = "./themes/";
    private static final String COMPONENTS_PATH = "./" + COMPONENTS_NAME + "/";
    private static final String THEME_CONFIG_FILE_NAME = "theme.yaml";
    private static final String DEPENDENCY_TREE_FILE_NAME = "dependency.tree";

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

    public void execute() throws MojoExecutionException, MojoFailureException {
        unpackDependencies();
        createDependencyConfig("::" + UUF_THEME_ASSEMBLY_FORMAT + ":");
        normalizeAppDependencies();
        super.execute();
    }

    @Override
    protected Assembly getAssembly() throws MojoFailureException {
        return createApplicationAssembly("make-application", "/" + getArtifactId());
    }

    private void unpackDependencies() throws MojoExecutionException {
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version(dependencyPluginVersion)
                ),
                goal("unpack-dependencies"),
                configuration(element(name("outputDirectory"), getUUFTempDirectory().toString())),
                executionEnvironment(getProject(), getMavenSession(), pluginManager)
        );
    }

    private void createDependencyConfig(String excludes) throws MojoExecutionException {
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version(dependencyPluginVersion)
                ),
                goal("tree"),
                configuration(
                        element(name("verbose"), "true"),
                        element(name("outputFile"),
                                getUUFTempDirectory().resolve(DEPENDENCY_TREE_FILE_NAME).toString()
                        ),
                        element(name("excludes"), excludes)
                ),
                executionEnvironment(getProject(), getMavenSession(), getPluginManager())
        );
    }

    protected void normalizeAppDependencies() throws MojoExecutionException {
        try {
            Path rootDir = getUUFTempDirectory();
            Path rootCompPath = getUUFTempDirectory().resolve(ROOT_COMPONENT_NAME);
            createDirectoryIfNotExists(rootCompPath);
            String rootComponentPattern = "**/" + COMPONENTS_NAME + "/" + ROOT_COMPONENT_NAME + "/**";
            AppsFinder appsFinder = new AppsFinder(rootComponentPattern, rootCompPath, getUUFTempDirectory());
            Files.walkFileTree(rootDir, appsFinder);
            appsFinder.deleteMatchedApplications();
        } catch (IOException e) {
            throw new MojoExecutionException("Error normalizing app dependencies", e);
        }
    }

    private Assembly createApplicationAssembly(String assemblyId, String baseDirectory) throws MojoFailureException {
        Assembly assembly = new Assembly();
        assembly.setId(assemblyId);
        assembly.setBaseDirectory(baseDirectory);

        //Adding root component
        List<FileSet> fileSets = new ArrayList<>();
        String rootComponentPath = COMPONENTS_PATH + ROOT_COMPONENT_NAME;
        fileSets.add(createFileSet(getBasedir().getAbsolutePath(), rootComponentPath));

        //Adding dependent components and themes
        Path uufTempDirectory = getUUFTempDirectory();
        try {
            DependencyHolder dependencies = getDependencies(uufTempDirectory);
            for (Path currentTheme : dependencies.getThemes()) {
                fileSets.add(createFileSet(currentTheme.toString(), THEMES_PATH + currentTheme.getFileName()));
            }
            for (Path currentComponent : dependencies.getComponents()) {
                fileSets.add(
                        createFileSet(currentComponent.toString(), COMPONENTS_PATH + currentComponent.getFileName())
                );
            }
        } catch (IOException e) {
            throw new MojoFailureException(
                    "Error occurred while reading extracted dependencies on '" + uufTempDirectory.toString() + "'");
        }
        assembly.setFileSets(fileSets);

        //Adding dependency.tree file
        ArrayList<FileItem> fileItems = new ArrayList<>();
        FileItem fileItem = new FileItem();
        fileItem.setSource(uufTempDirectory.resolve(DEPENDENCY_TREE_FILE_NAME).toString());
        fileItem.setOutputDirectory(COMPONENTS_PATH);
        fileItems.add(fileItem);
        assembly.setFiles(fileItems);

        //Setting format
        List<String> formatsList = new ArrayList<>();
        formatsList.add(UUF_COMPONENT_ASSEMBLY_FORMAT);
        assembly.setFormats(formatsList);
        return assembly;
    }

    private static DependencyHolder getDependencies(Path rootDir) throws IOException {
        Set<Path> components = new HashSet<>();
        Set<Path> themes = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootDir, new DirectoriesFilter())) {
            for (Path dir : directoryStream) {
                if (Files.exists(dir.resolve(THEME_CONFIG_FILE_NAME))) {
                    themes.add(dir);
                } else {
                    components.add(dir);
                }
            }
        }
        return new DependencyHolder(components, themes);
    }

    protected Path getUUFOsgiConfigOutDirectory() {
        return getUUFTempDirectory().resolve(ROOT_COMPONENT_NAME);
    }

    protected BuildPluginManager getPluginManager() {
        return this.pluginManager;
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
