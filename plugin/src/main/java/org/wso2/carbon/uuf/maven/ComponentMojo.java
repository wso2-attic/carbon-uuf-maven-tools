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
import org.wso2.carbon.uuf.maven.util.ConfigFileCreator;

import java.nio.file.Paths;
import java.util.Map;

/**
 * UUF Component creation Mojo that generates the osgi-imports when this component uses any OSGi imports and creates the
 * component zip archive for the given component project.
 *
 * @since 1.0.0
 */
@Mojo(name = "create-component", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ComponentMojo extends AbstractZipMojo {

    /**
     * OSGi {@code <Import-Package>} instructions for this UUF Component.
     */
    @Parameter(readonly = true, required = false)
    private Map<String, String> instructions;

    @Override
    public String getZipBaseDirectory() {
        int lastIndex = artifactId.lastIndexOf(".");
        if (lastIndex > -1) {
            return artifactId.substring(lastIndex + 1);
        }
        return artifactId;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!ARTIFACT_TYPE_UUF_COMPONENT.equals(packaging)) {
            throw new MojoExecutionException(
                    "Packaging type of an UUF Component should be '" + ARTIFACT_TYPE_UUF_COMPONENT +
                            "'. Instead found '" + packaging + "'.");
        }

        // create OSGi imports file
        if ((instructions != null) && !instructions.isEmpty()) {
            ConfigFileCreator.createOsgiImports(instructions.get(CONFIGURATION_IMPORT_PACKAGE),
                                                Paths.get(outputDirectoryPath));
        }
        super.execute();
    }
}
