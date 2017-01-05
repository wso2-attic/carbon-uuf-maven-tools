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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for dependency node bean.
 */
public class DependencyNodeTest {

    @Test
    public void testArtifactIdValidations() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new DependencyNode(null, "1.0.0", null));
        Assert.assertThrows(IllegalArgumentException.class, () -> new DependencyNode("", "1.0.0", null));

        new DependencyNode("org.wso2.carbon.uuf.sample.foundation.ui", "1.0.0", null);
    }

    @Test
    public void testVersionValidations() {
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> new DependencyNode("org.wso2.carbon.uuf.foundation.ui", null, null));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> new DependencyNode("org.wso2.carbon.uuf.foundation.ui", "", null));

        new DependencyNode("org.wso2.carbon.uuf.foundation.ui", "1.0.0", null);
    }

    @Test
    public void testAddDependencyValidations() {
        DependencyNode parent = new DependencyNode("parent", "1.0.0", null);
        DependencyNode child = new DependencyNode("child1", "1.0.0", parent);
        DependencyNode anotherNode = new DependencyNode("another-node", "2.0.0", null);

        parent.addDependency(child);
        Assert.assertThrows(IllegalArgumentException.class, () -> anotherNode.addDependency(child));
    }

    @Test
    public void testContextPath() {
        Assert.assertEquals(new DependencyNode("org.wso2.carbon.uuf.foundation.ui", "1.0.0", null).getContextPath(),
                            "foundation");
        Assert.assertEquals(new DependencyNode("org.wso2.carbon.uuf.store.feature", "1.0.0", null).getContextPath(),
                            "store");
        Assert.assertEquals(new DependencyNode("org.wso2.carbon.uuf.simple-auth", "1.0.0", null).getContextPath(),
                            "simple-auth");
    }

    @Test
    public void testParent() {
        DependencyNode root = new DependencyNode("root", "1.0.0", null);
        DependencyNode child1 = new DependencyNode("child-root", "1.0.0", root);
        DependencyNode child2 = new DependencyNode("child-child-root", "1.0.0", child1);
        DependencyNode child3 = new DependencyNode("child-child-child-root", "1.0.0", child2);

        Assert.assertThrows(IllegalArgumentException.class, () -> child1.getParent(0));
        Assert.assertThrows(IllegalArgumentException.class, () -> child2.getParent(-1));

        Assert.assertThrows(IllegalStateException.class, () -> root.getParent(1));

        Assert.assertEquals(child1.getParent(1), root);
        Assert.assertEquals(child1.getParent(2), null);
        Assert.assertThrows(NullPointerException.class, () -> child1.getParent(3));

        Assert.assertEquals(child2.getParent(1), child1);
        Assert.assertEquals(child2.getParent(2), root);
        Assert.assertEquals(child2.getParent(3), null);
        Assert.assertThrows(NullPointerException.class, () -> child2.getParent(4));

        Assert.assertEquals(child3.getParent(1), child2);
        Assert.assertEquals(child3.getParent(2), child1);
        Assert.assertEquals(child3.getParent(3), root);
        Assert.assertEquals(child3.getParent(4), null);
        Assert.assertThrows(NullPointerException.class, () -> child3.getParent(5));
    }
}
