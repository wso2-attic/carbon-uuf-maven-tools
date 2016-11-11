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

import org.apache.maven.plugin.Mojo;

/**
 * Base interface for all the UUF archetypes Mojo's
 *
 * @since 1.0.0
 */
public interface UUFMojo extends Mojo {

    String ARTIFACT_TYPE_UUF_APP = "carbon-feature";
    String ARTIFACT_TYPE_UUF_COMPONENT = "uuf-component";
    String ARTIFACT_TYPE_UUF_THEME = "uuf-theme";

    String DIRECTORY_COMPONENTS = "/components";
    String DIRECTORY_THEMES = "/themes";

    String EXPRESSION_PROJECT = "${project}";
    String EXPRESSION_PROJECT_PACKAGING = "${project.packaging}";
    String EXPRESSION_SOURCE_DIRECTORY_PATH = "${project.basedir}/src/main/";
    String EXPRESSION_ARTIFACT_ID = "${project.artifactId}";
    String EXPRESSION_INSTRUCTIONS = "instructions";

    String CONFIGURATION_IMPORT_PACKAGE = "Import-Package";
}
