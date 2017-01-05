/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.maven.bean;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.maven.bean.mojo.Bundle;
import org.wso2.carbon.uuf.maven.bean.mojo.BundleListConfig;
import org.wso2.carbon.uuf.maven.parser.YamlFileParserTest;

import java.util.List;

/**
 * Test class for testing bundle-dependencies.yaml file de-serialization.
 *
 * @since 1.0.0
 */
public class BundleDependenciesConfigTest {

    @Test
    public void testBundleDependencyYaml() throws Exception {
        BundleListConfig bundleListConfig = new YamlFileParserTest().testBundleDependencyConfiguration();
        List<Bundle> bundles = bundleListConfig.getBundles();
        Assert.assertNotNull(bundles);
        Assert.assertEquals(bundles.size(), 4);
        bundles.forEach(bundle -> {
            Assert.assertTrue(bundle.getSymbolicName().startsWith("org.wso2.carbon.uuf.sample.test"));
            Assert.assertEquals(bundle.getVersion(), "1.0.0-SNAPSHOT");
        });
    }
}
