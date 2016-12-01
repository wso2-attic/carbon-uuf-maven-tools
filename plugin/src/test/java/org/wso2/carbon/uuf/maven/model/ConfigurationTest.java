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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.maven.parser.ConfigurationParserTest;

import java.util.Map;

public class ConfigurationTest {

    public static void mergeConfiguration(Configuration configuration, String resourceConfigFilePath) throws Exception {
        configuration.merge(ConfigurationParserTest.parseConfigFile(resourceConfigFilePath));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMergeConfiguration() throws Exception {
        Configuration configuration = new Configuration();

        mergeConfiguration(configuration, ConfigurationParserTest.RESOURCE_FILE_CONFIG_1);
        Map<String, Object> configMap = configuration.asMap();
        Assert.assertEquals(configMap.get("appName"), "test app 1");
        Assert.assertTrue(configMap.get("menu") instanceof Map);
        Map<String, ?> menuMap = (Map<String, ?>) configMap.get("menu");
        Assert.assertEquals(menuMap.size(), 2);
        Assert.assertTrue(menuMap.get("main") instanceof Map);
        Map<String, ?> mainMenuMap = (Map<String, ?>) menuMap.get("main");
        Assert.assertEquals(mainMenuMap.size(), 2);
        Assert.assertTrue(mainMenuMap.get("Home") instanceof Map);
        Map<String, ?> mainHomeMenuMap = (Map<String, ?>) mainMenuMap.get("Home");
        Assert.assertEquals(mainHomeMenuMap.get("link"), "#home-1");
        Assert.assertEquals(mainHomeMenuMap.get("icon"), "fw fw-home");
        Assert.assertTrue(mainMenuMap.get("Pets") instanceof Map);
        Map<String, ?> mainPetsMenuMap = (Map<String, ?>) mainMenuMap.get("Pets");
        Assert.assertEquals(mainPetsMenuMap.size(), 2);

        mergeConfiguration(configuration, ConfigurationParserTest.RESOURCE_FILE_CONFIG_2);
        configMap = configuration.asMap();
        Assert.assertEquals(configMap.get("appName"), "test app 2");
        menuMap = (Map<String, ?>) configMap.get("menu");
        Assert.assertEquals(menuMap.size(), 2);
        mainMenuMap = (Map<String, ?>) menuMap.get("main");
        Assert.assertEquals(mainMenuMap.size(), 3);
        mainHomeMenuMap = (Map<String, ?>) mainMenuMap.get("Home");
        Assert.assertEquals(mainHomeMenuMap.get("link"), "#home-2");
        Assert.assertEquals(mainHomeMenuMap.get("icon"), "fw fw-home");
        mainPetsMenuMap = (Map<String, ?>) mainMenuMap.get("Pets");
        Assert.assertEquals(mainPetsMenuMap.size(), 3);
        Assert.assertTrue(mainMenuMap.get("Devices") instanceof Map);
        Map<String, ?> mainDevicesMenuMap = (Map<String, ?>) mainMenuMap.get("Devices");
        Assert.assertEquals(mainDevicesMenuMap.size(), 2);
    }
}
