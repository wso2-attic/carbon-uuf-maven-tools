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

package org.wso2.carbon.uuf.maven.serializer;

import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.wso2.carbon.uuf.maven.model.DependencyNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * YAML serializer which can serialize a dependency tree.
 *
 * @since 1.0.0
 */
public class DependencyTreeSerializer {

    private final Yaml yaml = new Yaml(new DependencyNodeRepresenter());

    /**
     * Serialize the specified dependency tree to YAML.
     *
     * @param rootNode root node of the dependency tree to be serialize
     * @return YAML representation of the dependency tree
     * @throws SerializationException if an error occurred during serialization
     */
    public String serialize(DependencyNode rootNode) throws SerializationException {
        try {
            return yaml.dumpAs(rootNode, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
        } catch (Exception e) {
            throw new SerializationException("Cannot serialize dependency tree where root node " + rootNode + ".", e);
        }
    }

    /**
     * {@link org.yaml.snakeyaml.representer.Representer} for {@link org.wso2.carbon.uuf.maven.model.DependencyNode}
     * class.
     *
     * @implNote This class controls what properties will be serialized and the appearing order of those serialized
     * properties in the output YAML when serializing DependencyNode.
     * @see org.yaml.snakeyaml.representer.Representer
     */
    private static class DependencyNodeRepresenter extends Representer {

        /**
         * Creates a new {@link DependencyNodeRepresenter}.
         *
         * @throws RuntimeException if cannot retrieve JavaBean properties from {@link DependencyNode} class.
         */
        public DependencyNodeRepresenter() {
            Set<Property> properties = new LinkedHashSet<>(); // to preserve order
            try {
                properties.add(new MethodProperty(new PropertyDescriptor("artifactId", DependencyNode.class,
                                                                         "getArtifactId", null)));
                properties.add(new MethodProperty(new PropertyDescriptor("version", DependencyNode.class,
                                                                         "getVersion", null)));
                properties.add(new MethodProperty(new PropertyDescriptor("dependencies", DependencyNode.class,
                                                                         "getDependencies", null)));
            } catch (IntrospectionException e) {
                throw new RuntimeException(
                        "Cannot retrieve properties in JavaBean '" + DependencyNode.class.getName() + "'.", e);
            }
            this.representers.put(DependencyNode.class, data -> representJavaBean(properties, data));
        }
    }
}
