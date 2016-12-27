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
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.wso2.carbon.uuf.maven.model.Bundle;
import org.wso2.carbon.uuf.maven.bean.ComponentConfig;
import org.wso2.carbon.uuf.maven.bean.Configuration;
import org.wso2.carbon.uuf.maven.bean.DependencyNode;
import org.wso2.carbon.uuf.maven.parser.AppConfigParser;
import org.wso2.carbon.uuf.maven.parser.ComponentConfigParser;
import org.wso2.carbon.uuf.maven.parser.DependencyTreeParser;
import org.wso2.carbon.uuf.maven.serializer.ConfigurationSerializer;
import org.wso2.carbon.uuf.maven.serializer.DependencyTreeSerializer;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * UUF Application creation Mojo.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-app", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AppMojo extends ComponentMojo {

    private static final String FILE_APP_CONFIG = "app.yaml";
    private static final String FILE_DEPENDENCY_TREE = "dependency.tree";
    private static final String DIRECTORY_COMPONENTS = "components";
    private static final String DIRECTORY_THEMES = "themes";
    private static final String DIRECTORY_ROOT_COMPONENT = "root";
    private static final String APP_ARTIFACT_ID_TAIL = ".feature";

    /**
     * The Maven session associated with this Mojo.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Path to the output directory of this Mojo
     */
    @Parameter(defaultValue = "${project.build.directory}/maven-shared-archive-resources/uufapps/",
               readonly = true, required = true)
    private String outputDirectoryPath;

    /**
     * Configured OSGi bundles.
     */
    @Parameter(property = "bundles", readonly = true, required = false)
    private List<Bundle> bundles;

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
     * Carbon Feature Plugin version to use.
     */
    @Parameter(defaultValue = "2.0.1", readonly = true, required = false)
    private String carbonFeaturePluginVersion;

    /**
     * Plugin manager to execute other Maven plugins.
     */
    @Component(hint = "default")
    private BuildPluginManager pluginManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Do validations.
        validate();

        // Compute the App's fully qualified name by removing ".feature" from the artifact ID.
        String appFullyQualifiedName = artifactId.substring(0, (artifactId.length() - APP_ARTIFACT_ID_TAIL.length()));
        outputDirectoryPath += appFullyQualifiedName;
        // Categorize dependencies.
        @SuppressWarnings("unchecked")
        Set<Artifact> allDependencies = project.getArtifacts();
        Set<Artifact> allComponentDependencies = allDependencies.stream()
                .filter(artifact -> ARTIFACT_TYPE_UUF_COMPONENT.equals(artifact.getClassifier()))
                .collect(Collectors.toSet());
        Set<Artifact> allThemeDependencies = allDependencies.stream()
                .filter(artifact -> ARTIFACT_TYPE_UUF_THEME.equals(artifact.getClassifier()))
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
        // 3.2. Create the final configuration.
        createConfigFile(rootNode, allComponentsDirectory);
        // 3.3 Create dependency tree file.
        createDependencyTree(rootNode, allComponentsDirectory);
        // 4. Unpack UUF Theme dependencies.
        unpackDependencies(allThemeDependencies, allThemesDirectory);
        // 5. Create Carbon Feature.
        createCarbonFeature(appFullyQualifiedName);
    }

    private void validate() throws MojoExecutionException {
        // Validation: Packaging type should be 'carbon-feature'
        if (!ARTIFACT_TYPE_UUF_APP.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF App should be '" + ARTIFACT_TYPE_UUF_APP + "'. Instead found '" +
                            packaging + "'.");
        }
        // Validation: Artifact ID should end with '.feature'
        if (!artifactId.endsWith(APP_ARTIFACT_ID_TAIL)) {
            throw new MojoExecutionException(
                    "Artifact ID of an UUF App should end with '.feature' as it is packaged as a Carbon Feature.");
        }
        // Validation: Parse component configuration file to make sure it is valid.
        String componentConfigFilePath = pathOf(sourceDirectoryPath, FILE_COMPONENT_CONFIG);
        try {
            ComponentConfigParser.parse(componentConfigFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException("Component configuration file '" + componentConfigFilePath + "' of '" +
                                                     artifactId + "' UUF App is invalid.", e);
        }
        // Validation: Parse app configuration file to make sure it is valid.
        String appConfigFilePath = pathOf(sourceDirectoryPath, FILE_APP_CONFIG);
        try {
            AppConfigParser.parse(componentConfigFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException("App configuration file '" + componentConfigFilePath + "' of '" +
                                                     artifactId + "' UUF App is invalid.", e);
        }
    }

    private DependencyNode getDependencyTree(Set<Artifact> includes) throws MojoExecutionException {
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
                            element(name("includes"), includes.stream()
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

    private void createConfigFile(DependencyNode rootNode, String componentsDirectory) throws MojoExecutionException {
        Configuration configuration = new Configuration();
        // Create the final configuration by traversing through the dependency tree.
        try {
            rootNode.traverse(node -> {
                String configFilePath = getFilePathIn(node.getArtifactId(), componentsDirectory, FILE_COMPONENT_CONFIG);
                Map configMap;
                // Since we are in a lambda, we throw RuntimeExceptions.
                try {
                    ComponentConfig componentConfig = ComponentConfigParser.parse(configFilePath);
                    configMap = (componentConfig == null) ? null : componentConfig.getConfig();
                } catch (ParsingException e) {
                    throw new RuntimeException("Cannot parse '" + FILE_COMPONENT_CONFIG + "' of " + node +
                                                       " which read from '" + configFilePath + "' path.", e);
                }
                try {
                    configuration.merge(configMap);
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
        ConfigurationSerializer serializer = new ConfigurationSerializer();
        String content;
        try {
            content = serializer.serialize(configuration);
        } catch (SerializationException e) {
            throw new MojoExecutionException("Cannot serialize configuration " + configuration + ".", e);
        }
        ConfigFileCreator.createConfigYaml(content, componentsDirectory);
    }

    private void createDependencyTree(DependencyNode rootNode, String componentsDirectory)
            throws MojoExecutionException {
        String content;
        try {
            content = new DependencyTreeSerializer().serialize(rootNode);
        } catch (SerializationException e) {
            throw new MojoExecutionException(
                    "Cannot serialize dependency tree where root node is " + rootNode + ".", e);
        }
        ConfigFileCreator.createDependencyTree(content, componentsDirectory);
    }

    private void createCarbonFeature(String appFullyQualifiedName) throws MojoExecutionException {
        // Create a 'resources' directory and add it to the project as a resources directory.
        String tempResourcesDirectoryPath = pathOf(tempDirectoryPath, "resources");
        Resource resource = new Resource();
        resource.setDirectory(tempResourcesDirectoryPath);
        project.addResource(resource);
        // Create the "p2.inf" file in that 'resources' directory.
        ConfigFileCreator.createP2Inf(appFullyQualifiedName, tempResourcesDirectoryPath);
        // Create Carbon Feature.
        try {
            executeMojo(
                    plugin(
                            groupId("org.wso2.carbon.maven"),
                            artifactId("carbon-feature-plugin"),
                            version(carbonFeaturePluginVersion)
                    ),
                    goal("generate"),
                    configuration(
                            element(name("propertyFile"), ConfigFileCreator.createFeatureProperties(tempDirectoryPath)),
                            element(name("adviceFileContents"),
                                    element(name("advice"),
                                            element(name("name"), "org.wso2.carbon.p2.category.type"),
                                            element(name("value"), "server")
                                    )
                            ),
                            element(name("bundles"),
                                    (bundles == null ? Collections.<Bundle>emptyList() : bundles).stream()
                                            .map(bundle -> element(name("bundle"),
                                                                   element(name("symbolicName"),
                                                                           bundle.getSymbolicName()),
                                                                   element(name("version"), bundle.getVersion()))
                                            ).toArray(MojoExecutor.Element[]::new))
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException(
                    "Cannot create Carbon Feature for UUF App '" + appFullyQualifiedName + "'.", e);
        }
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

    private void unpackDependencies(Set<Artifact> includes, String outputDirectory) throws MojoExecutionException {
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
                                    includes.stream().map(Artifact::getArtifactId).collect(Collectors.joining(",")))
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException("Cannot unpack dependencies " + includes + ".", e);
        }
    }

    private String getFilePathIn(String componentArtifactId, String componentsDirectory, String fileName) {
        if (artifactId.equals(componentArtifactId)) {
            // root component
            return pathOf(componentsDirectory, DIRECTORY_ROOT_COMPONENT, fileName);
        } else {
            int lastIndex = componentArtifactId.lastIndexOf(".");
            String componentContext;
            if (lastIndex > -1) {
                componentContext = componentArtifactId.substring(lastIndex + 1);
            } else {
                componentContext = componentArtifactId;
            }
            return pathOf(componentsDirectory, componentContext, fileName);
        }
    }
}
