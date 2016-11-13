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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a node in the dependency tree.
 */
public class DependencyNode {

    private final String artifactId;
    private final String version;
    private final DependencyNode parent;
    private final List<DependencyNode> dependencies;

    /**
     * Creates a new node.
     *
     * @param artifactId artifact ID of the UUF Component reflected by this node
     * @param version    version of the UUF Component reflected by this node
     * @param parent     parent node of the creating node; can be {@code null} if the creating node is root node
     */
    public DependencyNode(String artifactId, String version, DependencyNode parent) {
        this.artifactId = artifactId;
        this.version = version;
        this.parent = parent;
        this.dependencies = new ArrayList<>();
    }

    /**
     * Returns the artifact ID of the UUF Component which is reflected by this node.
     *
     * @return artifact ID of the UUF Component
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the version of the UUF Component which is reflected by this node.
     *
     * @return version of the UUF Component
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the dependencies of the UUF Component which is reflected by this node.
     *
     * @return dependencies of the UUF Component
     */
    public List<DependencyNode> getDependencies() {
        return dependencies;
    }

    /**
     * Adds the specified dependency to this node. Parent of the adding dependency should be this node.
     *
     * @param dependency a dependency of this node
     * @throws IllegalArgumentException if the parent of the specified dependecy is not this node
     */
    public void addDependency(DependencyNode dependency) {
        if (this != dependency.parent) {
            throw new IllegalArgumentException(
                    "Dependency Node '" + dependency + "' is not a dependency of '" + this + "' parent node.");
        }
        dependencies.add(dependency);
    }

    /**
     * Returns the parent node in the specified level of this node.
     *
     * @param level level of the parent, where {@code 1} means immediate parent and {@code 2} means parent of that
     *              parent and so on
     * @return parent node
     * @throws IllegalStateException    if there is no parent in this node, which idicates that this is the root node
     * @throws IllegalArgumentException if {@code level < 1}
     */
    public DependencyNode getParent(int level) {
        if (parent == null) {
            throw new IllegalStateException("This is the root node.");
        }
        if (level < 1) {
            throw new IllegalArgumentException("Parent level cannot be less than 1.");
        }

        DependencyNode currentParent = parent;
        for (int i = 1; i < level; i++) {
            currentParent = currentParent.parent;
        }
        return currentParent;
    }

    /**
     * Traverse this node and its dependencies in depth-first manner.
     *
     * @param nodeConsumer consumer that consumes each node
     */
    public void traverse(Consumer<DependencyNode> nodeConsumer) {
        dependencies.forEach(nodeConsumer);
        nodeConsumer.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{" + artifactId + ", " + version + "}";
    }
}