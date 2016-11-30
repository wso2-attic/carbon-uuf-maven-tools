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
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A bean class that represents a node in the dependency tree.
 *
 * @since 1.0.0
 */
public class DependencyNode {

    private static final String ARTIFACT_ID_TAIL_UI = ".ui";
    private static final String ARTIFACT_ID_TAIL_FEATURE = ".feature";

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
     * Returns the context path of the UUF Component which is reflected by this node.
     *
     * @return context path of the UUF Component
     */
    public String getContextPath() {
        String correctedArtifactId;
        if (artifactId.endsWith(ARTIFACT_ID_TAIL_FEATURE)) {
            correctedArtifactId = artifactId.substring(0, (artifactId.length() - ARTIFACT_ID_TAIL_FEATURE.length()));
        } else if (artifactId.endsWith(ARTIFACT_ID_TAIL_UI)) {
            correctedArtifactId = artifactId.substring(0, (artifactId.length() - ARTIFACT_ID_TAIL_UI.length()));
        } else {
            correctedArtifactId = artifactId;
        }
        int indexOfLastDot = correctedArtifactId.lastIndexOf('.');
        if (indexOfLastDot == -1) {
            return correctedArtifactId;
        } else {
            return correctedArtifactId.substring(indexOfLastDot + 1);
        }
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
     * @throws IllegalStateException    if there is no parent in this node, which indicates that this is the root node
     * @throws IllegalArgumentException if {@code level < 1}
     * @throws NullPointerException     if there is no parent
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
        dependencies.forEach(dependencyNode -> dependencyNode.traverse(nodeConsumer));
        nodeConsumer.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof DependencyNode)) {
            DependencyNode otherNode = (DependencyNode) obj;
            return Objects.equals(artifactId, otherNode.artifactId) && Objects.equals(version, otherNode.version);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{" + artifactId + ", " + version + "}";
    }
}
