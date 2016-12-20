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
import org.wso2.carbon.uuf.maven.model.ComponentConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser for component configuration YAML file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentConfigParser {

    /**
     * Parses the specified component configuration YAML file.
     *
     * @param componentConfigFilePath path to component config YAML file
     * @return component's configurations or {@code null} if specified component configuration file does not exists
     * @throws ParsingException if cannot read or parse the content of the specified component configuration file
     */
    public static ComponentConfig parse(String componentConfigFilePath) throws ParsingException {
        Path configFIle = Paths.get(componentConfigFilePath);
        if (!Files.exists(configFIle)) {
            return null; // File does not exists.
        }

        try {
            String content = new String(Files.readAllBytes(configFIle), StandardCharsets.UTF_8);
            return new Yaml().loadAs(content, ComponentConfig.class);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of component configuration file '" + configFIle + "'.",
                                       e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of component configuration file '" + configFIle + "'.",
                                       e);
        }
    }
}