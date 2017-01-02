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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.wso2.carbon.uuf.maven.model.Bundle;
import org.wso2.carbon.uuf.maven.model.DependencyNode;
import org.wso2.carbon.uuf.maven.parser.ComponentManifestParser;
import org.wso2.carbon.uuf.maven.parser.ConfigurationParser;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;
import org.wso2.carbon.uuf.maven.util.ZipCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UUF Component creation Mojo.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-component", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ComponentMojo extends AbstractUUFMojo {

    protected static final String CONFIGURATION_IMPORT_PACKAGE = "Import-Package";
    protected static final String FILE_CONFIG = "config.yaml";
    protected static final String FILE_COMPONENT_MANIFEST = "component.yaml";
    public static final String FILE_BUNDLE_DEPENDENCIES = "bundle-dependencies.yaml";

    /**
     * Path to the temporary directory for UUF Maven plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/uuf-temp/", readonly = true, required = true)
    protected String tempDirectoryPath;

    /**
     * OSGi {@code <Import-Package>} instructions for this UUF Component.
     */
    @Parameter(readonly = true, required = false)
    protected Map<String, String> instructions;

    /**
     * Configured OSGi bundles.
     */
    @Parameter(property = "bundles", readonly = true, required = false)
    protected List<Bundle> bundles;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Validation: Packaging type should be 'uuf-component'.
        if (!ARTIFACT_TYPE_UUF_COMPONENT.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF Component should be '" + ARTIFACT_TYPE_UUF_COMPONENT +
                            "'. Instead found '" + packaging + "'.");
        }
        // Validation: Parse configuration file to make sure it is a valid YAML file.
        String configFilePath = pathOf(sourceDirectoryPath, FILE_CONFIG);
        try {
            ConfigurationParser.parse(configFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException(
                    "Configuration file '" + configFilePath + "' of this UUF Component is invalid.", e);
        }
        // Validation: Parse component manifest file to make sure it is valid.
        String componentManifestFilePath = pathOf(sourceDirectoryPath, FILE_COMPONENT_MANIFEST);
        try {
            ComponentManifestParser.parse(componentManifestFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException(
                    "Component manifest file '" + componentManifestFilePath + "' of this UUF Component is invalid.", e);
        }

        List<String> sourceDirectoryPaths = new ArrayList<>();
        // Create OSGi imports file.
        if ((instructions != null) && !instructions.isEmpty()) {
            String osgiImportsContent = instructions.get(CONFIGURATION_IMPORT_PACKAGE);
            if ((osgiImportsContent != null) && !osgiImportsContent.trim().isEmpty()) {
                ConfigFileCreator.createOsgiImports(osgiImportsContent, tempDirectoryPath);
                sourceDirectoryPaths.add(tempDirectoryPath);
            }
        }
        // Create component level bundle-dependencies.yaml file.
        if (bundles != null && !bundles.isEmpty()) {
            ConfigFileCreator.createBundleDependenciesYaml(bundles, tempDirectoryPath);
            sourceDirectoryPaths.add(tempDirectoryPath);
        }
        // Create zip file.
        sourceDirectoryPaths.add(sourceDirectoryPath);
        String baseDirectoryName = new DependencyNode(artifactId, version, null).getContextPath();
        File archive = ZipCreator.createArchive(sourceDirectoryPaths, baseDirectoryName, outputDirectoryPath,
                                                finalName);
        project.getArtifact().setFile(archive);
        projectHelper.attachArtifact(project, ZipCreator.ARCHIVE_FORMAT, null, archive);
    }
}
