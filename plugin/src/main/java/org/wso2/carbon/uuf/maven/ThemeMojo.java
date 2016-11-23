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
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.wso2.carbon.uuf.maven.parser.ThemeConfigParser;
import org.wso2.carbon.uuf.maven.util.ZipCreator;

import java.io.File;

/**
 * UUF Theme creation Mojo that zip archive for the given theme project.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-theme", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ThemeMojo extends UUFMojo {

    private static final String FILE_THEME = "theme.yaml";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Validation: Packaging type should be 'uuf-theme'.
        if (!ARTIFACT_TYPE_UUF_THEME.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF Theme should be '" + ARTIFACT_TYPE_UUF_THEME + "'. Instead found '" +
                            packaging + "'.");
        }
        // Validation: Parse configuration file to make sure it is a valid YAML file.
        String themeConfigFilePath = pathOf(sourceDirectoryPath, FILE_THEME);
        try {
            new ThemeConfigParser().parse(themeConfigFilePath);
        } catch (ParsingException e) {
            throw new MojoExecutionException(
                    "Configuration file '" + themeConfigFilePath + "' of this UUF Theme is invalid.", e);
        }

        File archive = ZipCreator.createArchive(sourceDirectoryPath, artifactId, outputDirectoryPath, finalName);
        project.getArtifact().setFile(archive);
        projectHelper.attachArtifact(project, ZipCreator.ARCHIVE_FORMAT, null, archive);
    }
}
