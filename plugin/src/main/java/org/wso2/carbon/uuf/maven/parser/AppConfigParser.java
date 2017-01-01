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

import org.wso2.carbon.uuf.maven.bean.AppConfig;
import org.wso2.carbon.uuf.maven.exception.ParsingException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser for app configuration YAML file in an UUF App.
 *
 * @since 1.0.0
 */
public class AppConfigParser {

    /**
     * Parses the specified app configuration YAML file.
     *
     * @param appConfigFilePath path to app config YAML file
     * @return app's configurations
     * @throws ParsingException if cannot read or parse the content of the specified app configuration file
     */
    public static AppConfig parse(String appConfigFilePath) throws ParsingException {
        Path configFIle = Paths.get(appConfigFilePath);
        if (!Files.exists(configFIle)) {
            throw new ParsingException("Mandatory app config file '" + appConfigFilePath + "' does not exists.");
        }

        AppConfig appConfig;
        try {
            String content = new String(Files.readAllBytes(configFIle), StandardCharsets.UTF_8);
            appConfig = parseString(content);
        } catch (IOException e) {
            throw new ParsingException("Cannot read the content of app configuration file '" + configFIle + "'.", e);
        } catch (Exception e) {
            throw new ParsingException("Cannot parse the content of app configuration file '" + configFIle + "'.", e);
        }

        // Parsed app config can be null if the configuration file is empty or has comments only.
        return (appConfig == null) ? new AppConfig() : appConfig;
    }

    static AppConfig parseString(String appConfig) {
        return new Yaml().loadAs(appConfig, AppConfig.class);
    }
}
