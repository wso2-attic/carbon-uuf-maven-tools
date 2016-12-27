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

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bean class that represents the app's config file of an UUF App.
 *
 * @since 1.0.0
 */
public class AppConfig {

    public static final Pattern HTTP_STATUS_CODES_PATTERN = Pattern.compile("[1-9][0-9][0-9]");

    private String contextPath;
    private String theme;
    private String loginPageUri;
    private Map<String, String> errorPages;
    private Map<String, MenuItem[]> menus;
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
        if (contextPath == null) {
            return;
        }

        if (contextPath.isEmpty()) {
            throw new IllegalArgumentException("Value of 'contextPath' cannot be empty.");
        } else if (contextPath.charAt(0) != '/') {
            throw new IllegalArgumentException("Value of 'contextPath' must start with a '/'. Instead found '" +
                                                       contextPath.charAt(0) + "' at the beginning.");
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
        if (theme == null) {
            return;
        }

        if (theme.isEmpty()) {
            throw new IllegalArgumentException("Value of 'theme' cannot be empty.");
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
        if (loginPageUri == null) {
            return;
        }
        if (loginPageUri.isEmpty()) {
            throw new IllegalArgumentException("Value of 'loginPageUri' cannot be empty.");
        }
        if (loginPageUri.charAt(0) != '/') {
            throw new IllegalArgumentException("Value of 'loginPageUri' must start with a '/'. Instead found '" +
                                                       loginPageUri.charAt(0) + "' at the beginning.");
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
        for (Map.Entry<String, String> entry : errorPages.entrySet()) {
            String httpStatusCode = entry.getKey();
            String errorPageUri = entry.getValue();

            if (!httpStatusCode.equals("default") && !HTTP_STATUS_CODES_PATTERN.matcher(httpStatusCode).matches()) {
                throw new IllegalArgumentException(
                        "HTTP status code must be between 100 and 999. Instead found '" + httpStatusCode +
                                "' for error page URI '" + errorPageUri + "'.");
            }

            if (errorPageUri.isEmpty()) {
                throw new IllegalArgumentException(
                        "Error page URI for HTTP status code '" + httpStatusCode + "' cannot be empty.");
            } else if (errorPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException("Error page URI for HTTP status code '" + httpStatusCode +
                                                           "' must start with a '/'. Instead found '" +
                                                           errorPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.errorPages = errorPages;
    }

    /**
     * Returns the menus in this app's config.
     *
     * @return menus in this app's config
     */
    public Map<String, MenuItem[]> getMenus() {
        return menus;
    }

    /**
     * Sets the menus in this app's config.
     *
     * @param menus menus to be set
     */
    public void setMenus(Map<String, MenuItem[]> menus) {
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
     * Bean class that represents a menu item in the app's config file of an UUF Component.
     *
     * @since 1.0.0
     */
    public static class MenuItem {

        private String text;
        private String link;
        private String icon;
        private MenuItem[] submenus;

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
        public MenuItem[] getSubmenus() {
            return submenus;
        }

        /**
         * Sets the sub-menus of this menu item.
         *
         * @param submenus sub-menus to be set
         */
        public void setSubmenus(MenuItem[] submenus) {
            this.submenus = submenus;
        }
    }

    /**
     * Bean class that represents security related configurations in the app's config file of an UUF Component.
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
     * Bean class that represents security related URI patterns configurations in the app's config file of an UUF
     * Component.
     *
     * @since 1.0.0
     */
    public static class PatternsConfig {

        private String[] allow;
        private String[] deny;

        /**
         * Returns allowing URI patterns of this URI pattern configuration.
         *
         * @return allowing URI patterns
         */
        public String[] getAllow() {
            return allow;
        }

        /**
         * Sets the allowing URI patterns of this URI pattern configuration.
         *
         * @param allow allowing URI patterns to be set
         */
        public void setAllow(String[] allow) {
            this.allow = allow;
        }

        /**
         * Returns denying URI patterns of this URI pattern configuration.
         *
         * @return denying URI patterns
         */
        public String[] getDeny() {
            return deny;
        }

        /**
         * Sets the denying URI patterns of this URI pattern configuration.
         *
         * @param deny denying URI patterns to be set
         */
        public void setDeny(String[] deny) {
            this.deny = deny;
        }
    }
}
