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
 * Base class for all the UUF Mojos.
 *
 * @since 1.0.0
 */
public abstract class AbstractUUFMojo extends AbstractMojo {

    /**
     * Associated Maven project with this Mojo.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * The artifact ID of the associated Maven project.
     */
    @Parameter(defaultValue = "${project.artifactId}", readonly = true, required = true)
    protected String artifactId;

    /**
     * The version of the associated Maven project.
     */
    @Parameter(defaultValue = "${project.version}", readonly = true, required = true)
    protected String version;

    /**
     * Packaging type of the associated Maven project.
     */
    @Parameter(defaultValue = "${project.packaging}", readonly = true, required = true)
    protected String packaging;

    /**
     * Path to the directory where source files of the associated Maven project resides.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/", readonly = true, required = true)
    protected String sourceDirectoryPath;

    /**
     * Path to the output directory of this Mojo.
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    protected String outputDirectoryPath;

    /**
     * File name of the final artifact created by the associated Maven project.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    protected String finalName;

    /**
     * Project helper for the associated Maven project.
     */
    @Component
    protected MavenProjectHelper projectHelper;

    protected static String pathOf(String part1, String... parts) {
        return Paths.get(part1, parts).toString();
    }
}
