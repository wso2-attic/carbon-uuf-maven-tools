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

    /**
     * Parses the specified config YAML file.
     *
     * @param configFilePath path to config YAML file
     * @return configuration found in the file or {@code null} if specified config file does not exists
     * @throws ParsingException if cannot read or parse the content of the specified config file
     */
    public static Map parse(String configFilePath) throws ParsingException {
        Path configFile = Paths.get(configFilePath);
        if (!Files.exists(configFile)) {
            return null; // File does not exists.
        }

        try {
            String content = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
            return parseString(content);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of config file '" + configFilePath + "'.", e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of config file '" + configFilePath + "'.", e);
        }
    }

    static Map parseString(String config) throws Exception {
        return new Yaml().loadAs(config, Map.class);
    }

    /**
     * A generic parse method that parses the given yaml content file and de-serialize the content into given bean type.
     *
     * @param configFilePath path to yaml file
     * @param type type of the bean class to be used when de-serializing
     * @param <T> type of the bean class to be used when de-serializing
     * @return returns the populated bean instance
     * @throws ParsingException thrown when the given yaml file cannot be parsed properly
     */
    public static <T> T parse(String configFilePath, Class<T> type) throws ParsingException {
        Path configFile = Paths.get(configFilePath);
        if (!Files.exists(configFile)) {
            return null;
        }
        try {
            String content = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
            return new Yaml().loadAs(content, type);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of config file '" + configFilePath + "'.", e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of config file '" + configFilePath + "'.", e);
        }
    }
}
