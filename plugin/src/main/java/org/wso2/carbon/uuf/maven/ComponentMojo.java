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
import org.wso2.carbon.uuf.maven.parser.ComponentManifestParser;
import org.wso2.carbon.uuf.maven.parser.ConfigurationParser;
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;
import org.wso2.carbon.uuf.maven.util.ZipCreator;

import java.io.File;
import java.util.Map;

/**
 * UUF Component creation Mojo that generates the osgi-imports when this component uses any OSGi imports and creates the
 * component zip archive for the given component project.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-component", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ComponentMojo extends UUFMojo {

    protected static final String CONFIGURATION_IMPORT_PACKAGE = "Import-Package";
    protected static final String FILE_CONFIG = "config.yaml";
    protected static final String FILE_COMPONENT_MANIFEST = "component.yaml";

    /**
     * OSGi {@code <Import-Package>} instructions for this UUF Component.
     */
    @Parameter(readonly = true, required = false)
    protected Map<String, String> instructions;

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
            new ConfigurationParser().parse(configFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException("Configuration file '" + configFilePath + "' of is invalid.", e);
        }
        // Validation: Parse component manifest file to make sure it is valid.
        String componentManifestFilePath = pathOf(sourceDirectoryPath, FILE_COMPONENT_MANIFEST);
        try {
            new ComponentManifestParser().parse(componentManifestFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException(
                    "Component manifest file '" + componentManifestFilePath + "' of is invalid.", e);
        }

        // Create OSGi imports file.
        if ((instructions != null) && !instructions.isEmpty()) {
            ConfigFileCreator.createOsgiImports(instructions.get(CONFIGURATION_IMPORT_PACKAGE), outputDirectoryPath);
        }
        // Create zip file.
        File archive = ZipCreator.createArchive(sourceDirectoryPath, artifactId, outputDirectoryPath, finalName);
        project.getArtifact().setFile(archive);
        projectHelper.attachArtifact(project, ZipCreator.ARCHIVE_FORMAT, null, archive);
    }
}
