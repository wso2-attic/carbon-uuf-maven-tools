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

import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Parser for configuration file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ConfigurationParser {

    private final Yaml yaml = new Yaml();

    /**
     * Parses the specified config YAML file.
     *
     * @param configFilePath path to config YAML file
     * @return configuration found in the file or {@code null} if specified config file does not exists
     * @throws ParsingException if cannot read or parse the content of the specified config file
     */
    public Map parse(String configFilePath) throws ParsingException {
        Path configFile = Paths.get(configFilePath);
        if (!Files.exists(configFile)) {
            return null; // File does not exists.
        }
        String content;
        try {
            content = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of config file '" + configFilePath + "'.", e);
        }
        try {
            return parseString(content);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of config file '" + configFilePath + "'.", e);
        }
    }

    Map parseString(String config) throws Exception {
        return yaml.loadAs(config, Map.class);
    }
}
