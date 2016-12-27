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

/**
 * Bean class that represents the component's config file of an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentConfig {

    private List<API> apis;
    private List<Binding> bindings;
    private Map<String, Object> config;

    /**
     * Returns the APIs entries in this component's config.
     *
     * @return APIs in this component's config
     */
    public List<API> getApis() {
        return apis;
    }

    /**
     * Sets the APIs in this component's config.
     *
     * @param apis APIs to be set
     */
    public void setApis(List<API> apis) {
        this.apis = apis;
    }

    /**
     * Returns the bindings entries in this component's config.
     *
     * @return bindings in this component's config
     */
    public List<Binding> getBindings() {
        return bindings;
    }

    /**
     * Sets the bindings in this component's config.
     *
     * @param bindings bindings to be set
     */
    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }

    /**
     * Returns the configurations in this component's config.
     *
     * @return configurations in this component's config
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * Sets the configurations of this component's config.
     *
     * @param config configurations to be set
     */
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    /**
     * Bean class that represents an API entry in the component's config file of an UUF Component.
     *
     * @since 1.0.0
     */
    public static class API {

        private String className;
        private String uri;

        /**
         * Returns the name of the class of this API.
         *
         * @return name of the class of this API
         */
        public String getClassName() {
            return className;
        }

        /**
         * Sets the name of the class of this API.
         *
         * @param className name of the class to be set
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * Returns the URI of this API.
         *
         * @return URI of this API
         */
        public String getUri() {
            return uri;
        }

        /**
         * Sets the URI of this API.
         *
         * @param uri URI to be set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    /**
     * Bean class that represents a binding entry in the component's config file of an UUF Component.
     *
     * @since 1.0.0
     */
    public static class Binding {

        /**
         * Binding combine mode prepend.
         */
        public static final String MODE_PREPEND = "prepend";
        /**
         * Binging combine mode append.
         */
        public static final String MODE_APPEND = "append";
        /**
         * Binding combine mode overwrite.
         */
        public static final String MODE_OVERWRITE = "overwrite";

        private String zoneName;
        private String mode;
        private List<String> fragments;

        /**
         * Returns the zone name of this binding.
         *
         * @return zone name of this binding
         */
        public String getZoneName() {
            return zoneName;
        }

        /**
         * Sets the zone name of this binding.
         *
         * @param zoneName zone name to be set
         */
        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        /**
         * Returns the mode of this binding.
         *
         * @return mode of this binding
         */
        public String getMode() {
            return mode;
        }

        /**
         * Sets the mode of this binding.
         *
         * @param mode mode to be set
         * @throws IllegalArgumentException if the {@code mode} is not {@link #MODE_PREPEND} or {@link #MODE_APPEND} or
         *                                  {@link #MODE_OVERWRITE}
         * @see #MODE_PREPEND
         * @see #MODE_APPEND
         * @see #MODE_OVERWRITE
         */
        public void setMode(String mode) {
            if (mode == null) {
                this.mode = MODE_PREPEND; // default mode is prepend.
            } else if (MODE_PREPEND.equals(mode) || MODE_APPEND.equals(mode) || MODE_OVERWRITE.equals(mode)) {
                this.mode = mode;
            } else {
                throw new IllegalArgumentException(
                        "Binding mode should be either '" + MODE_PREPEND + "', '" + MODE_APPEND + "' or '" +
                                MODE_OVERWRITE + "'. Instead found '" + mode + "'.");
            }
        }

        /**
         * Returns the names of the fragments of this binding.
         *
         * @return fragment names
         */
        public List<String> getFragments() {
            return fragments;
        }

        /**
         * Sets the names of the fragments of this binding.
         *
         * @param fragments names of the Fragments to be set
         */
        public void setFragments(List<String> fragments) {
            this.fragments = fragments;
        }

        /**
         * Combines the specified binding with this binding.
         *
         * @param other other binding to be merged
         * @throws IllegalArgumentException if specified binding's zone name != this binding's zone name
         */
        public void merge(Binding other) {
            if (!this.zoneName.equals(other.zoneName)) {
                throw new IllegalArgumentException("Other binding's zone and this binding's zone are not equal.");
            }

            switch (other.mode) {
                case MODE_PREPEND:
                    this.fragments.addAll(0, other.fragments);
                    break;
                case MODE_APPEND:
                    this.fragments.addAll(other.fragments);
                    break;
                case MODE_OVERWRITE:
                    this.fragments = other.fragments;
                    break;
            }
        }
    }
}
