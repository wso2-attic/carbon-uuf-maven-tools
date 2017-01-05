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

package org.wso2.carbon.uuf.maven.bean;

import java.util.List;

/**
 * Bean class that represents the theme's config file of an UUF Theme.
 *
 * @since 1.0.0
 */
public class ThemeConfig {

    private List<String> css;
    private List<String> headJs;
    private List<String> js;

    /**
     * Returns the CSS relatives paths of this theme configuration.
     *
     * @return CSS relative paths
     */
    public List<String> getCss() {
        return css;
    }

    /**
     * Sets the CSS relative paths of this theme configuration.
     *
     * @param css CSS relative paths to be set
     * @throws IllegalArgumentException if a CSS file relative path starts with a '/'
     */
    public void setCss(List<String> css) {
        if (css != null) {
            for (String relativePath : css) {
                if (relativePath.charAt(0) == '/') {
                    throw new IllegalArgumentException(
                            "CSS file relative path cannot start with a '/'. '" + relativePath + "' is incorrect.");
                }
            }
        }
        this.css = css;
    }

    /**
     * Returns the header JS relatives paths of this theme configuration.
     *
     * @return header JS relative paths
     */
    public List<String> getHeadJs() {
        return headJs;
    }

    /**
     * Sets the head JS relative paths of this theme configuration.
     *
     * @param headJs head JS relative paths to be set
     * @throws IllegalArgumentException if a header JS file relative path starts with a '/'
     */
    public void setHeadJs(List<String> headJs) {
        if (headJs != null) {
            for (String relativePath : headJs) {
                if (relativePath.charAt(0) == '/') {
                    throw new IllegalArgumentException("Header JS file relative path cannot start with a '/'. '" +
                                                               relativePath + "' is incorrect.");
                }
            }
        }
        this.headJs = headJs;
    }

    /**
     * Returns the footer JS relatives paths of this theme configuration.
     *
     * @return footer JS relative paths
     */
    public List<String> getJs() {
        return js;
    }

    /**
     * Sets the footer JS relative paths of this theme configuration.
     *
     * @param js footer JS relative paths to be set
     * @throws IllegalArgumentException if a JS file relative path starts with a '/'
     */
    public void setJs(List<String> js) {
        if (js != null) {
            for (String relativePath : js) {
                if (relativePath.charAt(0) == '/') {
                    throw new IllegalArgumentException(
                            "JS file relative path cannot start with a '/'. '" + relativePath + "' is incorrect.");
                }
            }
        }
        this.js = js;
    }
}
