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
import org.wso2.carbon.uuf.maven.model.ComponentManifest;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser for component manifest YAML file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentManifestParser {

    private final Yaml yaml = new Yaml();

    /**
     * Parses the specified component manifest YAML file.
     *
     * @param componentManifestFilePath path to component manifest YAML file
     * @return component manifest in the file or {@code null} if specified component manifest file does not exists
     * @throws ParsingException if cannot read or parse the content of the specified component manifest file
     */
    public ComponentManifest parse(String componentManifestFilePath) throws ParsingException {
        Path manifestFile = Paths.get(componentManifestFilePath);
        if (!Files.exists(manifestFile)) {
            return null; // File does not exists.
        }
        String content;
        try {
            content = new String(Files.readAllBytes(manifestFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of component manifest file '" + manifestFile + "'.", e);
        }
        try {
            return parseString(content);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of component manifest file '" + manifestFile + "'.",
                                       e);
        }
    }

    ComponentManifest parseString(String componentManifest) throws Exception {
        return yaml.loadAs(componentManifest, ComponentManifest.class);
    }
}
