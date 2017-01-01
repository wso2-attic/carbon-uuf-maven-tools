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
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bean class that represents the app's config file of an UUF App.
 *
 * @since 1.0.0
 */
public class AppConfig {

    private static final Pattern HTTP_STATUS_CODES_PATTERN = Pattern.compile("[1-9][0-9][0-9]");

    private String contextPath;
    private String theme;
    private String loginPageUri;
    private Map<String, String> errorPages;
    private Map<String, List<MenuItem>> menus;
    private SecurityConfig security;

    /**
     * Returns the client-side context path in this app's config.
     *
     * @return client-side context path in this app's config.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the client-side context path in this app's config.
     *
     * @param contextPath client-side context path to be set
     * @throws IllegalArgumentException if {@code contextPath} is empty or doesn't start with a '/'.
     */
    public void setContextPath(String contextPath) {
        if (contextPath != null) {
            if (contextPath.isEmpty()) {
                throw new IllegalArgumentException(
                        "Context path configured with 'contextPath' key in the app's config cannot be empty.");
            } else if (contextPath.charAt(0) != '/') {
                throw new IllegalArgumentException(
                        "Context path configured with 'contextPath' key in the app's config must start with a '/'. " +
                                "Instead found '" + contextPath.charAt(0) + "' at the beginning.");
            }
        }
        this.contextPath = contextPath;
    }

    /**
     * Returns the theme name in this app's config.
     *
     * @return theme name in this app's config
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the theme name in this app's config.
     *
     * @param theme theme name to be set
     * @throws IllegalArgumentException if {@code theme} is an empty string
     */
    public void setTheme(String theme) {
        if (theme != null) {
            if (theme.isEmpty()) {
                throw new IllegalArgumentException(
                        "Theme name configured with 'theme' key in the app's config cannot be empty.");
            }
        }
        this.theme = theme;
    }

    /**
     * Returns the login page URI in this app's config.
     *
     * @return URI of the login page in this app's config
     */
    public String getLoginPageUri() {
        return loginPageUri;
    }

    /**
     * Sets the login page URI in this app's config.
     *
     * @param loginPageUri URI of the login page to be set
     * @throws IllegalArgumentException if {@code loginPageUri} is empty or doesn't start with a '/'.
     */
    public void setLoginPageUri(String loginPageUri) {
        if (loginPageUri != null) {
            if (loginPageUri.isEmpty()) {
                throw new IllegalArgumentException(
                        "Login page URI configured with 'loginPageUri' key in the app's config cannot be empty.");
            }
            if (loginPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException(
                        "Login page URI configured with 'loginPageUri' key in the app's config must start with a '/'." +
                                " Instead found '" + loginPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.loginPageUri = loginPageUri;
    }

    /**
     * Returns the error pages URIs in this app's config.
     *
     * @return URIs of error pages in this app's config
     */
    public Map<String, String> getErrorPages() {
        return errorPages;
    }

    /**
     * Sets the error pages URIs in this app's config.
     *
     * @param errorPages URIs of error pages to be set
     * @throws IllegalArgumentException if an error page URI is empty or doesn't start with a '/'.
     */
    public void setErrorPages(Map<String, String> errorPages) {
        if (errorPages != null) {
            for (Map.Entry<String, String> entry : errorPages.entrySet()) {
                String httpStatusCode = entry.getKey();
                String errorPageUri = entry.getValue();

                if (!httpStatusCode.equals("default") && !HTTP_STATUS_CODES_PATTERN.matcher(httpStatusCode).matches()) {
                    throw new IllegalArgumentException(
                            "HTTP status code of an error page entry in the app's config must be between 100 and 999." +
                                    " Instead found '" + httpStatusCode + "' for URI '" + errorPageUri + "'.");
                }

                if (errorPageUri.isEmpty()) {
                    throw new IllegalArgumentException(
                            "URI of an error page entry in the app's config cannot be empty. " +
                                    "Found an empty URI for HTTP status code '" + httpStatusCode + "'.");
                } else if (errorPageUri.charAt(0) != '/') {
                    throw new IllegalArgumentException(
                            "URI of an error page entry in the app's config must start with a '/'. Instead found '" +
                                    errorPageUri.charAt(0) + "' at the beginning of the URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                }
            }
        }
        this.errorPages = errorPages;
    }

    /**
     * Returns the menus in this app's config.
     *
     * @return menus in this app's config
     */
    public Map<String, List<MenuItem>> getMenus() {
        return menus;
    }

    /**
     * Sets the menus in this app's config.
     *
     * @param menus menus to be set
     */
    public void setMenus(Map<String, List<MenuItem>> menus) {
        this.menus = menus;
    }

    /**
     * Returns the security related configurations in this app's config.
     *
     * @return security related configurations in this app's config
     */
    public SecurityConfig getSecurity() {
        return security;
    }

    /**
     * Sets the security related configurations in this app's config.
     *
     * @param security security configs to be set
     */
    public void setSecurity(SecurityConfig security) {
        this.security = security;
    }

    /**
     * Bean class that represents a menu item in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class MenuItem {

        private String text;
        private String link;
        private String icon;
        private List<MenuItem> submenus;

        /**
         * Returns the text of this menu item.
         *
         * @return text of this menu item
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the text of this menu item.
         *
         * @param text text to be set
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * Returns the link of this menu item.
         *
         * @return link of this menu item.
         */
        public String getLink() {
            return link;
        }

        /**
         * Sets the link of this menu item.
         *
         * @param link link to be set
         */
        public void setLink(String link) {
            this.link = link;
        }

        /**
         * Returns the icon CSS class of this menu item.
         *
         * @return icon CSS class of this menu item
         */
        public String getIcon() {
            return icon;
        }

        /**
         * Sets the icon CSS class of this menu item.
         *
         * @param icon icon CSS class to be set
         */
        public void setIcon(String icon) {
            this.icon = icon;
        }

        /**
         * Returns the sub-menus of this menu item.
         *
         * @return sub-menus of this menu item
         */
        public List<MenuItem> getSubmenus() {
            return submenus;
        }

        /**
         * Sets the sub-menus of this menu item.
         *
         * @param submenus sub-menus to be set
         */
        public void setSubmenus(List<MenuItem> submenus) {
            this.submenus = submenus;
        }
    }

    /**
     * Bean class that represents security related configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class SecurityConfig {

        private PatternsConfig csrfPatterns;
        private PatternsConfig xssPatterns;
        private Map<String, String> responseHeaders;

        /**
         * Returns CSRF URI patterns of this security configuration.
         *
         * @return CSRF URI patterns
         */
        public PatternsConfig getCsrfPatterns() {
            return csrfPatterns;
        }

        /**
         * Sets the CSRF URI patterns of this security configuration.
         *
         * @param csrfPatterns CSRF URI patterns to be set
         */
        public void setCsrfPatterns(PatternsConfig csrfPatterns) {
            this.csrfPatterns = csrfPatterns;
        }

        /**
         * Returns XSS URI patterns of this security configuration.
         *
         * @return XSS URI patterns
         */
        public PatternsConfig getXssPatterns() {
            return xssPatterns;
        }

        /**
         * Sets the XSS URI patterns of this security configuration.
         *
         * @param xssPatterns XSS URI patterns to be set
         */
        public void setXssPatterns(PatternsConfig xssPatterns) {
            this.xssPatterns = xssPatterns;
        }

        /**
         * Returns HTTP response headers of this security configuration.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getResponseHeaders() {
            return responseHeaders;
        }

        /**
         * Sets the HTTP response headers of this security configuration.
         *
         * @param responseHeaders HTTP response headers to be set
         */
        public void setResponseHeaders(Map<String, String> responseHeaders) {
            this.responseHeaders = responseHeaders;
        }
    }

    /**
     * Bean class that represents security related URI patterns configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class PatternsConfig {

        private List<String> accept;
        private List<String> reject;

        /**
         * Returns allowing URI patterns of this URI pattern configuration.
         *
         * @return allowing URI patterns
         */
        public List<String> getAccept() {
            return accept;
        }

        /**
         * Sets the allowing URI patterns of this URI pattern configuration.
         *
         * @param accept allowing URI patterns to be set
         */
        public void setAccept(List<String> accept) {
            if (accept != null) {
                for (String uriPattern : accept) {
                    if (uriPattern.isEmpty()) {
                        throw new IllegalArgumentException("Accepting URI pattern cannot be empty.");
                    }
                    // TODO: 12/29/16 Check whether uriPattern is a valid pattern.
                }
            }
            this.accept = accept;
        }

        /**
         * Returns denying URI patterns of this URI pattern configuration.
         *
         * @return denying URI patterns
         */
        public List<String> getReject() {
            return reject;
        }

        /**
         * Sets the denying URI patterns of this URI pattern configuration.
         *
         * @param reject denying URI patterns to be set
         */
        public void setReject(List<String> reject) {
            if (reject != null) {
                for (String uriPattern : reject) {
                    if (uriPattern.isEmpty()) {
                        throw new IllegalArgumentException("Rejecting URI pattern cannot be empty.");
                    }
                    // TODO: 12/29/16 Check whether uriPattern is a valid pattern.
                }
            }
            this.reject = reject;
        }
    }
}
