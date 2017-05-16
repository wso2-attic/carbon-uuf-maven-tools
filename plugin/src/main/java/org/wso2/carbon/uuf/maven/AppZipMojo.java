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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wso2.carbon.uuf.maven.util.ZipCreator;

import java.io.File;
import java.util.Collections;

/**
 * Creates a zip archive for an UUF app.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-app-zip", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class AppZipMojo extends AbstractAppMojo {

    public static final String ARTIFACT_TYPE_UUF_APP = "uuf-app";

    @Override
    protected String getOutputDirectoryPath() {
        return pathOf(tempDirectoryPath, "app");
    }

    @Override
    protected void validate() throws MojoExecutionException {
        // Validation: Packaging type should be 'carbon-feature'
        if (!ARTIFACT_TYPE_UUF_APP.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF App should be '" + ARTIFACT_TYPE_UUF_APP + "'. Instead found '" +
                            packaging + "'.");
        }
        super.validate();
    }

    @Override
    protected void createProjectArtifact() throws MojoExecutionException {
        File archive = ZipCreator.createArchive(Collections.singletonList(getOutputDirectoryPath()), null,
                                                outputDirectoryPath, finalName);
        project.getArtifact().setFile(archive);
        projectHelper.attachArtifact(project, ZipCreator.ARCHIVE_FORMAT, null, archive);
    }
}
