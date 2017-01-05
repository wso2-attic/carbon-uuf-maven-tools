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

import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser for YAML configuration files in an UUF apps, themes, and components.
 *
 * @since 1.0.0
 */
public class YamlFileParser {

    /**
     * Parses the given YAML configuration file and de-serialize the content into given bean type.
     *
     * @param configFilePath path to YAML file
     * @param type           class of the bean to be used when de-serializing
     * @param <T>            type of the bean class to be used when de-serializing
     * @return returns the populated bean instance
     * @throws ParsingException if cannot read or parse the content of the specified YAML file
     */
    public static <T> T parse(String configFilePath, Class<T> type) throws ParsingException {
        Path configurationFile = Paths.get(configFilePath);
        if (!Files.exists(configurationFile)) {
            throw new ParsingException("Mandatory configuration file '" + configurationFile + "' does not exists.");
        }

        T loadedBean;
        try {
            String content = new String(Files.readAllBytes(configurationFile), StandardCharsets.UTF_8);
            loadedBean = parseString(content, type);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of configuration file '" + configurationFile + "'.",
                    e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the configuration file '" + configurationFile + "'.",
                    e);
        }
        if (loadedBean == null) {
            // Either configuration file is empty or has comments only.
            throw new ParsingException(
                    "Cannot parse the configuration file '" + configurationFile + "' as it is empty.");
        }
        return loadedBean;
    }

    static <T> T parseString(String configFileContent, Class<T> type) {
        return new Yaml().loadAs(configFileContent, type);
    }
}
