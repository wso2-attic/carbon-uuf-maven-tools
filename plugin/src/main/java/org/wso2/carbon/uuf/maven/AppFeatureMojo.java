/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.maven;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.wso2.carbon.uuf.maven.bean.mojo.Bundle;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;

import java.util.Collections;

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
 * Creates a Carbon Feature for an UUF app.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-app", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AppFeatureMojo extends AbstractAppMojo {

    public static final String ARTIFACT_TYPE_UUF_APP = "carbon-feature";
    private static final String APP_ARTIFACT_ID_TAIL = ".feature";

    /**
     * Path to the output directory of this Mojo
     */
    @Parameter(defaultValue = "${project.build.directory}/maven-shared-archive-resources/uufapps/",
               readonly = true, required = true)
    private String outputDirectoryPath;

    /**
     * Carbon Feature Plugin version to use.
     */
    @Parameter(defaultValue = "3.0.0", readonly = true, required = false)
    private String carbonFeaturePluginVersion;

    @Override
    protected String getOutputDirectoryPath() {
        return pathOf(outputDirectoryPath, getAppFulyQualifiedName(artifactId));
    }

    @Override
    protected void createProjectArtifact() throws MojoExecutionException {
        String appFullyQualifiedName = getAppFulyQualifiedName(artifactId);
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

    @Override
    protected void validate() throws MojoExecutionException {
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
        super.validate();
    }

    private static String getAppFulyQualifiedName(String artifactId) {
        // Compute the App's fully qualified name by removing ".feature" from the artifact ID.
        return artifactId.substring(0, (artifactId.length() - APP_ARTIFACT_ID_TAIL.length()));
    }
}
