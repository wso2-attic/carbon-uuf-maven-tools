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

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.maven.bean.AppConfig;

import java.nio.charset.StandardCharsets;

/**
 * Test cases for app's configuration parser.
 */
public class AppConfigParserTest {

    private static final String RESOURCE_FILE_APP_CONFIG = "/app.yaml";

    public static AppConfig parseConfigFile(String resourceConfigFilePath) throws Exception {
        String content = IOUtils.toString(AppConfigParserTest.class.getResourceAsStream(resourceConfigFilePath),
                                          StandardCharsets.UTF_8.toString());
        return AppConfigParser.parseString(content);
    }

    @Test
    public void testParse() throws Exception {
        try {
            parseConfigFile(RESOURCE_FILE_APP_CONFIG);
        } catch (Exception e) {
            Assert.fail("Cannot parse config file '" + RESOURCE_FILE_APP_CONFIG + "'.", e);
        }
    }
}
