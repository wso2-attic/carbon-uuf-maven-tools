/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.maven.model;

public class Bundle {

    private String symbolicName;
    private String version;

    /**
     * Returns the OSGI symbolic name which is in the manifest file of the artifact represented by this carbon artifact.
     * This is applicable only if the represented carbon artifact is an OSGI bundle.
     *
     * @return String symbolic name
     */
    public String getSymbolicName() {
        return symbolicName;
    }

    /**
     * Sets the OSGI symbolic name which is in the manifest file of the artifact represented by this carbon artifact.
     * This is applicable only if the represented carbon artifact is an OSGI bundle.
     *
     * @param symbolicName String
     */
    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }


    /**
     * Returns the maven version for this artifact. <p> <b>Important:</b> This is not the OSGI version </p>
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the maven version for this artifact. <p> <b>Important:</b> This is not the OSGI version </p>
     *
     * @param version String
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
