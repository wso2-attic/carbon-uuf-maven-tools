/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.wso2.carbon.uuf.maven.bean.AppConfig;
import org.wso2.carbon.uuf.maven.bean.ComponentConfig;
import org.wso2.carbon.uuf.maven.bean.Configuration;
import org.wso2.carbon.uuf.maven.bean.DependencyNode;
import org.wso2.carbon.uuf.maven.bean.mojo.BundleListConfig;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.wso2.carbon.uuf.maven.parser.DependencyTreeParser;
import org.wso2.carbon.uuf.maven.parser.YamlFileParser;
import org.wso2.carbon.uuf.maven.serializer.DependencyTreeSerializer;
import org.wso2.carbon.uuf.maven.serializer.YamlSerializer;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Base class for all UUF app creation Mojos.
 *
 * @since 1.0.0
 */
public abstract class AbstractAppMojo extends ComponentMojo {

    private static final String FILE_APP_CONFIG = "app.yaml";
    private static final String FILE_DEPENDENCY_TREE = "dependency.tree";
    private static final String DIRECTORY_COMPONENTS = "components";
    private static final String DIRECTORY_THEMES = "themes";
    private static final String DIRECTORY_ROOT_COMPONENT = "root";

    /**
     * The Maven session associated with this Mojo.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * Maven Dependency Plugin version to use.
     */
    @Parameter(defaultValue = "2.9", readonly = true, required = false)
    private String dependencyPluginVersion;

    /**
     * Maven Resources Plugin version to use.
     */
    @Parameter(defaultValue = "2.7", readonly = true, required = false)
    private String resourcesPluginVersion;

    /**
     * Plugin manager to execute other Maven plugins.
     */
    @Component(hint = "default")
    protected BuildPluginManager pluginManager;

    /**
     * Returns the path to the output directory.
     *
     * @return path to the output directory.
     */
    protected abstract String getOutputDirectoryPath();

    /**
     * Creates the final artifact of the this Maven project.
     *
     * @throws MojoExecutionException if an error occurs during artifact creation
     */
    protected abstract void createProjectArtifact() throws MojoExecutionException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Do validations.
        validate();

        String outputDirectoryPath = getOutputDirectoryPath();
        // Categorize dependencies.
        @SuppressWarnings("unchecked")
        Set<Artifact> allDependencies = project.getArtifacts();
        Set<Artifact> allComponentDependencies = allDependencies.stream()
                .filter(artifact -> ARTIFACT_TYPE_UUF_COMPONENT.equals(artifact.getClassifier()))
                .collect(Collectors.toSet());
        Set<Artifact> allThemeDependencies = allDependencies.stream()
                .filter(artifact -> ThemeMojo.ARTIFACT_TYPE_UUF_THEME.equals(artifact.getClassifier()))
                .collect(Collectors.toSet());
        String allComponentsDirectory = pathOf(outputDirectoryPath, DIRECTORY_COMPONENTS);
        String allThemesDirectory = pathOf(outputDirectoryPath, DIRECTORY_THEMES);

        // 1. Unpack UUF Component dependencies.
        unpackDependencies(allComponentDependencies, allComponentsDirectory);
        // 2.1. Create "root" component.
        copyFiles(sourceDirectoryPath, pathOf(allComponentsDirectory, DIRECTORY_ROOT_COMPONENT));
        // 2.2 Create "osgi-imports" file for the "root" component.
        if ((instructions != null) &&
                (instructions.getImportPackage() != null) && (!instructions.getImportPackage().isEmpty())) {
            ConfigFileCreator.createOsgiImports(instructions.getImportPackage(),
                                                pathOf(allComponentsDirectory, DIRECTORY_ROOT_COMPONENT));
        }
        // 3.1. Create dependency tree.
        DependencyNode rootNode = getDependencyTree(allComponentDependencies);
        // 3.2. Read the bundle-dependencies.yaml file of all the components and add the entries the "bundles" instance.
        addComponentBundleDependencies(rootNode, allComponentsDirectory);
        // 3.3. Create the final configuration.
        createConfigurationFile(rootNode, allComponentsDirectory);
        // 3.4. Create dependency tree file.
        createDependencyTree(rootNode, allComponentsDirectory);
        // 4. Unpack UUF Theme dependencies.
        unpackDependencies(allThemeDependencies, allThemesDirectory);
        // 5. Create final artifact.
        createProjectArtifact();
    }

    protected void validate() throws MojoExecutionException {
        // Validation: Parse component configuration file to make sure it is valid.
        String componentConfigFilePath = pathOf(sourceDirectoryPath, FILE_COMPONENT_CONFIG);
        try {
            YamlFileParser.parse(componentConfigFilePath, ComponentConfig.class);
        } catch (ParsingException e) {
            throw new MojoExecutionException("Component configuration file '" + componentConfigFilePath + "' of '" +
                                                     artifactId + "' UUF App is invalid.", e);
        }
        // Validation: Parse app configuration file to make sure it is valid.
        parseAppConfig();
    }

    private DependencyNode getDependencyTree(Set<Artifact> componentDependencies) throws MojoExecutionException {
        if (componentDependencies.isEmpty()) {
            // There are no component dependencies. So only one node is in the dependency tree and its the root app.
            return new DependencyNode(this.artifactId, this.version, null);
        }

        String dependencyTreeFilePath = tempDirectoryPath + FILE_DEPENDENCY_TREE;
        try {
            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-dependency-plugin"),
                            version(dependencyPluginVersion)
                    ),
                    goal("tree"),
                    configuration(
                            element(name("verbose"), "true"),
                            element(name("outputFile"), dependencyTreeFilePath),
                            element(name("includes"), componentDependencies.stream()
                                    .map(artifact -> artifact.getGroupId() + ":" + artifact.getArtifactId() + "::")
                                    .collect(Collectors.joining(",")))
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException(
                    "Cannot generate dependency tree for '" + artifactId + "'.", e);
        }
        try {
            return DependencyTreeParser.parse(dependencyTreeFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException(
                    "Cannot parse generated dependency tree in '" + dependencyTreeFilePath + "'.", e);
        }
    }

    private void createConfigurationFile(DependencyNode rootNode, String componentsDirectory)
            throws MojoExecutionException {
        Configuration configuration = new Configuration(parseAppConfig());
        // Create the final configuration by traversing through the dependency tree.
        try {
            rootNode.traverse(node -> {
                String configFilePath = getFilePathIn(node, componentsDirectory, FILE_COMPONENT_CONFIG);
                // Since we are in a lambda, we throw RuntimeExceptions.
                ComponentConfig componentConfig;
                try {
                    componentConfig = YamlFileParser.parse(configFilePath, ComponentConfig.class);
                } catch (ParsingException e) {
                    throw new RuntimeException("Cannot parse '" + FILE_COMPONENT_CONFIG + "' of " + node +
                                                       " which read from '" + configFilePath + "' path.", e);
                }
                try {
                    configuration.merge(componentConfig.getConfig());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(
                            "Cannot merge configuration Map parsed from '" + FILE_COMPONENT_CONFIG + "' of " + node +
                                    " which read from '" + configFilePath + "' path.", e);
                }
            });
        } catch (RuntimeException e) {
            // Catch above thrown RuntimeExceptions.
            throw new MojoExecutionException("Cannot create final configuration for " + rootNode + ".", e);
        }
        // Now create the app's configuration file by serializing the final configuration.
        String content;
        try {
            content = YamlSerializer.serialize(configuration);
        } catch (SerializationException e) {
            throw new MojoExecutionException("Cannot serialize configuration " + configuration + ".", e);
        }
        ConfigFileCreator.createConfigurationYaml(content, componentsDirectory);
    }

    /**
     * This method reads the bundle dependencies coming from all the components and add them to the application's
     * "bundles" instance which is later used by the carbon-feature-plugin to create the feature.
     *
     * @param rootNode            the current app's dependency node (i.e the root node of the app)
     * @param componentsDirectory the "components" directory within the app
     * @throws MojoExecutionException thrown on error while reading/populating the bundle dependencies
     */
    private void addComponentBundleDependencies(DependencyNode rootNode, String componentsDirectory)
            throws MojoExecutionException {
        try {
            rootNode.traverse(node -> {
                String bundleDependenciesFilePath = getFilePathIn(node, componentsDirectory, FILE_BUNDLES);
                if (!Files.exists(Paths.get(bundleDependenciesFilePath))) {
                    return;
                }
                BundleListConfig bundleListConfig = null;
                try {
                    bundleListConfig = YamlFileParser.parse(bundleDependenciesFilePath, BundleListConfig.class);
                } catch (ParsingException e) {
                    throw new RuntimeException("Cannot parse '" + FILE_BUNDLES + "' of " + node +
                                                       " which read from '" + bundleDependenciesFilePath + "' path.",
                                               e);
                }
                if (bundleListConfig == null) {
                    return;
                }
                bundleListConfig.getBundles().forEach(bundles::add);
                //delete the file after reading its content to prevent it from getting packed with the app
                try {
                    Files.delete(Paths.get(bundleDependenciesFilePath));
                } catch (IOException e) {
                    throw new RuntimeException("Cannot delete '" + FILE_BUNDLES + "' of " + node +
                                                       " which read from '" + bundleDependenciesFilePath + "' path.",
                                               e);
                }
            });
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Cannot add component level bundle dependencies for " + rootNode + ".", e);
        }
    }

    private void createDependencyTree(DependencyNode rootNode, String componentsDirectory)
            throws MojoExecutionException {
        String content;
        try {
            content = DependencyTreeSerializer.serialize(rootNode);
        } catch (SerializationException e) {
            throw new MojoExecutionException(
                    "Cannot serialize dependency tree where root node is " + rootNode + ".", e);
        }
        ConfigFileCreator.createDependencyTree(content, componentsDirectory);
    }


    private void copyFiles(String sourcePath, String destinationPath) throws MojoExecutionException {
        // TODO: 10/19/16 Exclude unnecessary files (e.g. .iml, .DS_Store) when packing the root component
        try {
            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-resources-plugin"),
                            version(resourcesPluginVersion)
                    ),
                    goal("copy-resources"),
                    configuration(
                            element(name("outputDirectory"), destinationPath),
                            element(name("resources"),
                                    element(name("resource"),
                                            element(name("directory"), sourcePath),
                                            element(name("filtering"), "false")
                                    )
                            )
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException(
                    "Cannot copy sources from '" + sourcePath + "' to '" + destinationPath + "'.", e);
        }
    }

    private void unpackDependencies(Set<Artifact> dependencies, String outputDirectory) throws MojoExecutionException {
        if (dependencies.isEmpty()) {
            return; // nothing to unpack
        }

        try {
            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-dependency-plugin"),
                            version(dependencyPluginVersion)
                    ),
                    goal("unpack-dependencies"),
                    configuration(
                            element(name("outputDirectory"), outputDirectory),
                            element(name("includeArtifactIds"),
                                    dependencies.stream().map(Artifact::getArtifactId).collect(Collectors.joining(",")))
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException("Cannot unpack dependencies " + dependencies + ".", e);
        }
    }

    private String getFilePathIn(DependencyNode node, String componentsDirectory, String fileName) {
        if (artifactId.equals(node.getArtifactId())) {
            // root component
            return pathOf(componentsDirectory, DIRECTORY_ROOT_COMPONENT, fileName);
        } else {
            return pathOf(componentsDirectory, node.getContextPath(), fileName);
        }
    }

    private AppConfig parseAppConfig() throws MojoExecutionException {
        String appConfigFilePath = pathOf(sourceDirectoryPath, FILE_APP_CONFIG);
        try {
            return YamlFileParser.parse(appConfigFilePath, AppConfig.class);
        } catch (ParsingException e) {
            throw new MojoExecutionException("App configuration file '" + appConfigFilePath + "' of '" +
                                                     artifactId + "' UUF App is invalid.", e);
        }
    }
}
