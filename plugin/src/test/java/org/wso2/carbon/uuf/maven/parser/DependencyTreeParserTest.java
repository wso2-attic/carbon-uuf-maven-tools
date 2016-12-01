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

package org.wso2.carbon.uuf.maven.parser;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.maven.model.DependencyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DependencyTreeParserTest {

    public static List<String> getDependencyTreeLines() {
        List<String> dependencyTreeLines = new ArrayList<>();
        Scanner scanner = new Scanner(DependencyTreeParserTest.class.getResourceAsStream("/dependency.tree"));
        while (scanner.hasNextLine()) {
            dependencyTreeLines.add(scanner.nextLine());
        }
        return dependencyTreeLines;
    }

    @Test
    public void testParse() throws Exception {
        DependencyNode rootNode = DependencyTreeParser.parseLines(getDependencyTreeLines());
        Assert.assertEquals(rootNode.getDependencies().size(), 21);
        Assert.assertEquals(rootNode.getDependencies().get(0), new DependencyNode("snakeyaml", "1.16.0.wso2v1", null));
        Assert.assertEquals(rootNode.getDependencies().get(0).getDependencies().size(), 0);
        Assert.assertEquals(rootNode.getDependencies().get(0).getParent(1), rootNode);
        Assert.assertEquals(rootNode.getDependencies().get(0).getParent(2), null);

        Assert.assertEquals(rootNode.getDependencies().get(1), new DependencyNode("handlebars", "4.0.3.wso2v1", null));
        Assert.assertEquals(rootNode.getDependencies().get(2), new DependencyNode("cache-api", "1.0.0", null));
        Assert.assertEquals(rootNode.getDependencies().get(3).getDependencies().size(), 3);
        Assert.assertEquals(rootNode.getDependencies().get(4),
                            new DependencyNode("org.wso2.carbon.jndi", "1.0.0-m1", null));

        Assert.assertEquals(rootNode.getDependencies().get(20),
                            new DependencyNode("mockito-core", "2.0.44-beta", null));
        Assert.assertEquals(rootNode.getDependencies().get(20).getDependencies().size(), 2);
        Assert.assertEquals(rootNode.getDependencies().get(20).getDependencies().get(0).getParent(2), rootNode);
    }
}