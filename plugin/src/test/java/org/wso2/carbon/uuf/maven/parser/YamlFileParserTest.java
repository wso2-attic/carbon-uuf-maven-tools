/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.maven.bean.AppConfig;
import org.wso2.carbon.uuf.maven.bean.ComponentConfig;
import org.wso2.carbon.uuf.maven.bean.ThemeConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Test cases for YAML configuration file parser.
 */
public class YamlFileParserTest {

    @Test
    public void testAppConfiguration() throws Exception {
        YamlFileParser.parseString(readResourceFile("/app.yaml"), AppConfig.class);
    }

    @Test
    public ComponentConfig testComponentConfiguration() throws Exception {
        return YamlFileParser.parseString(readResourceFile("/component.yaml"), ComponentConfig.class);
    }

    @Test
    public ComponentConfig testRootComponentConfiguration() throws Exception {
        return YamlFileParser.parseString(readResourceFile("/root-component.yaml"), ComponentConfig.class);
    }

    @Test
    public void testThemeConfiguration() throws Exception {
        YamlFileParser.parseString(readResourceFile("/theme.yaml"), ThemeConfig.class);
    }

    private static String readResourceFile(String resourceFileName) throws IOException {
        return IOUtils.toString(YamlFileParserTest.class.getResourceAsStream(resourceFileName),
                                StandardCharsets.UTF_8.toString());
    }
}
