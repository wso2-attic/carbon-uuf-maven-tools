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

import org.wso2.carbon.uuf.maven.bean.ThemeConfig;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser for theme configuration YAML file in an UUF Theme.
 *
 * @since 1.0.0
 */
public class ThemeConfigParser {

    /**
     * Parses the specified theme configuration YAML file.
     *
     * @param themeConfigFilePath path to theme config YAML file
     * @return theme's configurations
     * @throws ParsingException if cannot read or parse the content of the specified theme configuration file
     */
    public static ThemeConfig parse(String themeConfigFilePath) throws ParsingException {
        Path manifestFile = Paths.get(themeConfigFilePath);
        if (!Files.exists(manifestFile)) {
            throw new ParsingException("Mandatory theme config file '" + themeConfigFilePath + "' does not exists.");
        }

        ThemeConfig themeConfig;
        try {
            String content = new String(Files.readAllBytes(manifestFile), StandardCharsets.UTF_8);
            themeConfig = new Yaml().loadAs(content, ThemeConfig.class);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of theme configuration file '" + manifestFile + "'.",
                                       e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of theme configuration file '" + manifestFile + "'.",
                                       e);
        }

        // Parsed theme config can be null if the configuration file is empty or has comments only.
        return (themeConfig == null) ? new ThemeConfig() : themeConfig;
    }
}
