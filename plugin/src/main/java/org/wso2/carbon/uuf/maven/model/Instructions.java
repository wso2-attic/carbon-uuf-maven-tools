/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.maven.model;

/**
 * Bean class that represent OSGi bundling instructions specified inside a {@code <configurations>} tag in a POM file.
 *
 * @since 1.0.0
 */
public class Instructions {

    private String importPackage;

    /**
     * Returns the import packages entry ({@code <Import-Package>}) in this instructions.
     *
     * @return import package entry
     */
    public String getImportPackage() {
        return importPackage;
    }

    /**
     * Sets the import package entry entry ({@code <Import-Package>}) in this instructions.
     *
     * @param importPackage import package entry to be set
     */
    public void setImportPackage(String importPackage) {
        this.importPackage = importPackage;
    }
}
