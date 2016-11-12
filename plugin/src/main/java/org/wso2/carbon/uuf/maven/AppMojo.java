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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.wso2.carbon.uuf.maven.model.Bundle;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;

import java.nio.file.Path;
import java.nio.file.Paths;
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
 * UUF Application creation Mojo that generates dependency.tree, p2.inf files and finally creates the carbon-feature for
 * the given app project.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-app", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AppMojo implements UUFMojo {

    private static final String FILE_DEPENDENCY_TREE = "dependency.tree";
    private static final String DIRECTORY_ROOT_COMPONENT = "/root";

    /**
     * The Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Maven project.
     */
    @Parameter(defaultValue = EXPRESSION_PROJECT, readonly = true, required = true)
    private MavenProject project;

    /**
     * Packaging type of the project.
     */
    @Parameter(defaultValue = EXPRESSION_PROJECT_PACKAGING, readonly = true, required = true)
    protected String packaging;

    /**
     * Source directory path for UUF Maven plugin.
     */
    @Parameter(defaultValue = EXPRESSION_SOURCE_DIRECTORY_PATH, readonly = true, required = true)
    private String sourceDirectoryPath;

    /**
     * The output directory for UUF Maven plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/maven-shared-archive-resources/uufapps/",
               readonly = true, required = true)
    private String outputDirectoryPath;

    /**
     * The temporary directory for UUF Maven plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/uuf-temp/", readonly = true, required = true)
    private String tempDirectoryPath;

    /**
     * The artifact ID of the project.
     */
    @Parameter(defaultValue = EXPRESSION_ARTIFACT_ID, readonly = true, required = true)
    private String artifactId;

    /**
     * The artifact repository to use.
     */
    @Parameter(property = "localRepository", readonly = true, required = true)
    private ArtifactRepository localRepository;

    /**
     * OSGi {@code <Import-Package>} instructions for this UUF App.
     */
    @Parameter(property = EXPRESSION_INSTRUCTIONS, readonly = true, required = false)
    private Map<String, String> instructions;

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
    @Parameter(defaultValue = "2.0.0", readonly = true, required = false)
    private String carbonFeaturePluginVersion;

    /**
     * Plugin manager to execute other Maven plugins.
     */
    @Component(hint = "default")
    private BuildPluginManager pluginManager;

    private Log log = new SystemStreamLog();

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Validation: Packaging type should be 'carbon-feature'
        if (!ARTIFACT_TYPE_UUF_APP.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF App should be '" + ARTIFACT_TYPE_UUF_APP + "'. Instead found '" +
                            packaging + "'.");
        }
        // Validation: Artifact ID should end with '.feature'
        if (!artifactId.endsWith(".feature")) {
            throw new MojoExecutionException(
                    "Artifact ID of an UUF App should end with '.feature' as it is packaged as a Carbon Feature.");
        }

        // Remove ".feature" from the artifact ID.
        String featureName = artifactId.substring(0, (artifactId.length() - ".feature".length()));
        outputDirectoryPath += featureName;

        // Categorize dependencies.
        @SuppressWarnings("unchecked")
        Set<Artifact> allDependencies = ((Set<Artifact>) project.getArtifacts());
        Set<Artifact> uufComponentDependencies = allDependencies.stream()
                .filter(artifact -> ARTIFACT_TYPE_UUF_COMPONENT.equals(artifact.getClassifier()))
                .collect(Collectors.toSet());
        Set<Artifact> uufComponentThemeDependencies = allDependencies.stream()
                .filter(artifact -> ARTIFACT_TYPE_UUF_THEME.equals(artifact.getClassifier()))
                .collect(Collectors.toSet());

        // 1. Unpack UUF Component dependencies.
        unpackDependencies(uufComponentDependencies, outputDirectoryPath + DIRECTORY_COMPONENTS);
        // 2.1. Create "root" component.
        // TODO: 10/19/16 Exclude unnecessary files (e.g. .iml, .DS_Store) when packing the root component
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-resources-plugin"),
                        version(resourcesPluginVersion)
                ),
                goal("copy-resources"),
                configuration(
                        element(name("outputDirectory"),
                                outputDirectoryPath + DIRECTORY_COMPONENTS + DIRECTORY_ROOT_COMPONENT),
                        element(name("resources"),
                                element(name("resource"),
                                        element(name("directory"), sourceDirectoryPath),
                                        element(name("filtering"), "false")
                                )
                        )
                ),
                executionEnvironment(project, session, pluginManager)
        );
        // 2.2 Create "osgi-imports" file for the "root" component.
        if ((instructions != null) && !instructions.isEmpty()) {
            Path rootComponentDir = Paths.get(outputDirectoryPath, DIRECTORY_COMPONENTS, DIRECTORY_ROOT_COMPONENT);
            ConfigFileCreator.createOsgiImports(instructions.get(CONFIGURATION_IMPORT_PACKAGE), rootComponentDir);
        }
        // 3. Unpack UUF Theme dependencies.
        unpackDependencies(uufComponentThemeDependencies, outputDirectoryPath + DIRECTORY_THEMES);
        // 4. Create dependencies tree.
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
                                outputDirectoryPath + DIRECTORY_COMPONENTS + "/" + FILE_DEPENDENCY_TREE
                        ),
                        element(name("includes"), uufComponentDependencies.stream()
                                .map(artifact -> artifact.getGroupId() + ":" + artifact.getArtifactId() + "::")
                                .collect(Collectors.joining(",")))
                ),
                executionEnvironment(project, session, pluginManager)
        );
        // 5.1.1 Create a 'resources' directory and add it to the project as a resources directory.
        Path tempDirectory = Paths.get(tempDirectoryPath);
        Path tempResourcesDirectory = tempDirectory.resolve("resources");
        Resource resource = new Resource();
        resource.setDirectory(tempResourcesDirectory.toString());
        project.addResource(resource);
        // 5.1.2 Create the "p2.inf" file in that 'resources' directory.
        ConfigFileCreator.createP2Inf(featureName, tempResourcesDirectory);
        // 5.2 Create Carbon P2 Feature.
        executeMojo(
                plugin(
                        groupId("org.wso2.carbon.maven"),
                        artifactId("carbon-feature-plugin"),
                        version(carbonFeaturePluginVersion)
                ),
                goal("generate"),
                configuration(
                        element(name("propertyFile"),
                                ConfigFileCreator.createFeatureProperties(tempDirectory).toAbsolutePath().toString()),
                        element(name("adviceFileContents"),
                                element(name("advice"),
                                        element(name("name"), "org.wso2.carbon.p2.category.type"),
                                        element(name("value"), "server")
                                ),
                                element(name("advice"),
                                        element(name("name"), "org.eclipse.equinox.p2.type.group"),
                                        element(name("value"), "false")
                                )
                        ),
                        element(name("bundles"), (bundles == null ? Collections.<Bundle>emptyList() : bundles).stream()
                                .map(bundle -> element(name("bundle"),
                                                       element(name("symbolicName"), bundle.getSymbolicName()),
                                                       element(name("version"), bundle.getVersion()))
                                ).toArray(MojoExecutor.Element[]::new))
                ),
                executionEnvironment(project, session, pluginManager)
        );
    }

    private void unpackDependencies(Set<Artifact> includes, String outputDirectory) throws MojoExecutionException {
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
    }
}
