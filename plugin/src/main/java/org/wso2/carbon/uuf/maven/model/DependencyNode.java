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

public class DependencyNode {

    private final String artifactId;
    private final String version;
    private final DependencyNode parent;
    private final List<DependencyNode> dependencies;

    public DependencyNode(String artifactId, String version, DependencyNode parent) {
        this.artifactId = artifactId;
        this.version = version;
        this.parent = parent;
        this.dependencies = new ArrayList<>();
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public List<DependencyNode> getDependencies() {
        return dependencies;
    }

    public void addDependency(DependencyNode dependency) {
        if (this != dependency.parent) {
            throw new IllegalArgumentException(
                    "Dependency Node '" + dependency + "' is not a dependency of '" + this + "' parent node.");
        }
        dependencies.add(dependency);
    }

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

    public void travese(Consumer<DependencyNode> nodeConsumer) {
        dependencies.forEach(nodeConsumer);
        nodeConsumer.accept(this);
    }

    @Override
    public String toString() {
        return "{" + artifactId + ", " + version + "}";
    }
}
