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
 */
public class DependencyTreeSerializer {

    /**
     * Serialize the specified dependency ree to a valid YAML text.
     *
     * @param rootNode root node of the dependency tree to be serialize
     * @return YAML representation of the dependency tree
     */
    public static String serialize(DependencyNode rootNode) {
        Representer representer = new DependencyNodeRepresenter();
        return new Yaml(representer).dumpAs(rootNode, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
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
