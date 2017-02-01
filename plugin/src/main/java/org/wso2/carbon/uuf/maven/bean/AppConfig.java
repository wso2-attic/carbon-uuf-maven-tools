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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bean class that represents the app's config file of an UUF App.
 *
 * @since 1.0.0
 */
public class AppConfig {

    private static final Pattern HTTP_STATUS_CODES_PATTERN = Pattern.compile("[1-5][0-9][0-9]");

    private String contextPath;
    private String theme;
    private String loginPageUri;
    private Map<String, String> errorPages = Collections.emptyMap();
    private List<Menu> menus = Collections.emptyList();
    private SecurityConfig security = new SecurityConfig();

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
        if (theme != null && theme.isEmpty()) {
            throw new IllegalArgumentException(
                    "Theme name configured with 'theme' key in the app's config cannot be empty.");
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
        if (errorPages == null) {
            this.errorPages = Collections.emptyMap();
        } else {
            for (Map.Entry<String, String> entry : errorPages.entrySet()) {
                String httpStatusCode = entry.getKey();
                String errorPageUri = entry.getValue();

                if (!httpStatusCode.equals("default") && !HTTP_STATUS_CODES_PATTERN.matcher(httpStatusCode).matches()) {
                    throw new IllegalArgumentException(
                            "HTTP status code of an error page entry in the app's config must be between 100 and 599." +
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
            this.errorPages = errorPages;
        }
    }

    /**
     * Returns the menus in this app's config.
     *
     * @return menus in this app's config
     */
    public List<Menu> getMenus() {
        return menus;
    }

    /**
     * Sets the menus in this app's config.
     *
     * @param menus menus to be set
     */
    public void setMenus(List<Menu> menus) {
        this.menus = (menus == null) ? Collections.emptyList() : menus;
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
        this.security = (security == null) ? new SecurityConfig() : security;
    }

    /**
     * Bean class that represents a menu in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class Menu {

        private String name;
        private List<MenuItem> items;

        /**
         * Returns the name of this menu.
         *
         * @return text of this menu
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of this menu.
         *
         * @param name name to be set
         */
        public void setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of a menu cannot be null.");
            } else if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of a menu cannot be empty.");
            }
            this.name = name;
        }

        /**
         * Returns the menu items of this menu.
         *
         * @return menu items of this menu
         */
        public List<MenuItem> getItems() {
            return items;
        }

        /**
         * Sets the menu items of this menu.
         *
         * @param items menu items to be set
         */
        public void setItems(List<MenuItem> items) {
            if (items == null) {
                throw new IllegalArgumentException("Items of a menu cannot be null.");
            } else if (items.isEmpty()) {
                throw new IllegalArgumentException("Items of a menu cannot be empty list.");
            }
            this.items = items;
        }
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
        private List<MenuItem> submenus = Collections.emptyList();

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
            if (text == null) {
                throw new IllegalArgumentException("Text of a menu item cannot be null.");
            }
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
            if (link == null) {
                throw new IllegalArgumentException("Link of a menu item cannot be null.");
            }
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
            this.submenus = (submenus == null) ? Collections.emptyList() : submenus;
        }
    }

    /**
     * Bean class that represents security related configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class SecurityConfig {

        private List<String> csrfIgnoreUris = Collections.emptyList();
        private List<String> xssIgnoreUris = Collections.emptyList();
        private Map<String, String> responseHeaders = Collections.emptyMap();

        /**
         * Returns the list of URI's that doesn't require CSRF protection.
         *
         * @return list of URI's that doesn't require CSRF protection.
         */
        public List<String> getCsrfIgnoreUris() {
            return csrfIgnoreUris;
        }

        /**
         * Sets the list of URI's that doesn't require CSRF protection.
         *
         * @param csrfIgnoreUris list of URI's that doesn't require CSRF protection.
         * @throws IllegalArgumentException if URIs in {@code csrfIgnoreUris} is empty or doesnn't start with '/'.
         */
        public void setCsrfIgnoreUris(List<String> csrfIgnoreUris) {
            if (csrfIgnoreUris != null) {
                for (String csrfUri : csrfIgnoreUris) {
                    if (csrfUri != null) {
                        if (csrfUri.isEmpty()) {
                            throw new IllegalArgumentException("CSRF ignore URI in the app's config cannot be empty.");
                        } else if (!csrfUri.startsWith("/")) {
                            throw new IllegalArgumentException(
                                    "XSS ignore URI in the app's config must start with a '/'. Instead found '" +
                                            csrfUri.charAt(0) + "' at the beginning of the ignore URI '" + csrfUri + "'.");
                        }
                    }
                }
                this.csrfIgnoreUris = csrfIgnoreUris;
            } else {
                this.csrfIgnoreUris = Collections.emptyList();
            }
        }

        /**
         * Returns the list of URI's that doesn't require XSS protection.
         *
         * @return list of URI's that doesn't require XSS protection.
         */
        public List<String> getXssIgnoreUris() {
            return xssIgnoreUris;
        }

        /**
         * Sets the list of URI's that doesn't require XSS protection.
         *
         * @param xssIgnoreUris the list of URI's that doesn't require XSS protection.
         * @throws IllegalArgumentException if URIs in {@code xssIgnoreUris} is empty or doesn't start with '/'.
         */
        public void setXssIgnoreUris(List<String> xssIgnoreUris) {
            if (xssIgnoreUris != null) {
                for (String xssUri : xssIgnoreUris) {
                    if (xssUri != null) {
                        if (xssUri.isEmpty()) {
                            throw new IllegalArgumentException("XSS ignore URI in the app's config cannot be empty.");
                        } else if (!xssUri.startsWith("/")) {
                            throw new IllegalArgumentException(
                                    "XSS ignore URI in the app's config must start with a '/'. Instead found '" +
                                            xssUri.charAt(0) + "' at the beginning of the ignore URI '" + xssUri + "'.");
                        }
                    }
                }
                this.xssIgnoreUris = xssIgnoreUris;
            } else {
                this.xssIgnoreUris = Collections.emptyList();
            }
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
            this.responseHeaders = (responseHeaders == null) ? Collections.emptyMap() : responseHeaders;
        }
    }

}
