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
import org.wso2.carbon.uuf.maven.parser.ComponentConfigParserTest;

import java.util.List;
import java.util.Map;

/**
 * Test cases for configuration bean.
 */
public class ConfigurationTest {

    public static void mergeConfiguration(Configuration configuration, String resourceConfigFilePath) throws Exception {
        ComponentConfig componentConfig = ComponentConfigParserTest.parseConfigFile(resourceConfigFilePath);
        configuration.merge(componentConfig.getConfig());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMergeConfiguration() throws Exception {
        Configuration configuration = new Configuration();

        mergeConfiguration(configuration, ComponentConfigParserTest.RESOURCE_FILE_COMPONENT_CONFIG_1);
        Map<String, Object> configMap = configuration.getOther();
        Assert.assertEquals(configMap.get("appName"), "test app 1");
        Assert.assertEquals(configMap.get("pageSize"), 10);
        Assert.assertTrue(configMap.get("users") instanceof List);
        List<String> users = (List<String>) configMap.get("users");
        Assert.assertEquals(users.size(), 2);
        Assert.assertEquals(users.get(0), "Kamal");
        Assert.assertTrue(configMap.get("devices") instanceof Map);
        Map<String, ?> devices = (Map<String, ?>) configMap.get("devices");
        Assert.assertEquals(devices.size(), 2);
        Assert.assertTrue(devices.get("android") instanceof Map);
        Map<String, ?> androidDevice = (Map<String, ?>) devices.get("android");
        Assert.assertEquals(androidDevice.get("id"), 12345);
        Assert.assertEquals(androidDevice.get("location"), "Colombo");
        Assert.assertEquals(androidDevice.get("locked"), null);


        mergeConfiguration(configuration, ComponentConfigParserTest.RESOURCE_FILE_COMPONENT_CONFIG_2);
        configMap = configuration.getOther();
        Assert.assertEquals(configMap.get("appName"), "test app 2");
        Assert.assertEquals(configMap.get("pageSize"), 10);
        Assert.assertEquals(configMap.get("successMsg"), "Hoooooray!!!");
        users = (List<String>) configMap.get("users");
        Assert.assertEquals(users.size(), 3);
        Assert.assertEquals(users.get(2), "Sirimal");
        devices = (Map<String, ?>) configMap.get("devices");
        Assert.assertEquals(devices.size(), 3);
        androidDevice = (Map<String, ?>) devices.get("android");
        Assert.assertEquals(androidDevice.get("id"), 12345);
        Assert.assertEquals(androidDevice.get("location"), "Colombo");
        Assert.assertEquals(androidDevice.get("locked"), true);
        Assert.assertTrue(devices.get("ios") instanceof Map);
        Map<String, ?> iosDevice = (Map<String, ?>) devices.get("ios");
        Assert.assertEquals(iosDevice.get("id"), 9999);
        Assert.assertEquals(iosDevice.get("location"), "Anuradhapura");
        Assert.assertEquals(iosDevice.get("locked"), null);
    }
}
