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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.nio.file.Paths;

/**
 * Base interface for all the UUF archetypes Mojo's
 *
 * @since 1.0.0
 */
public abstract class UUFMojo extends AbstractMojo {

    public static final String ARTIFACT_TYPE_UUF_APP = "carbon-feature";
    public static final String ARTIFACT_TYPE_UUF_COMPONENT = "uuf-component";
    public static final String ARTIFACT_TYPE_UUF_THEME = "uuf-theme";

    /**
     * This Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Packaging type of this Maven project.
     */
    @Parameter(defaultValue = "${project.packaging}", readonly = true, required = true)
    protected String packaging;

    /**
     * Path to the directory where source files resides.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/", readonly = true, required = true)
    protected String sourceDirectoryPath;

    /**
     * The output directory for UUF Maven plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/uuf/", readonly = true, required = true)
    protected String outputDirectoryPath;

    /**
     * The artifact ID of this Maven project.
     */
    @Parameter(defaultValue = "${project.artifactId}", readonly = true, required = true)
    protected String artifactId;

    /**
     * File name of the final artifact created by this Maven project.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    protected String finalName;

    /**
     * Maven ProjectHelper.
     */
    @Component
    protected MavenProjectHelper projectHelper;

    protected static String pathOf(String part1, String... parts) {
        return Paths.get(part1, parts).toString();
    }
}
