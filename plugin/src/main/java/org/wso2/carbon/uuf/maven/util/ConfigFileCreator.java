/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.maven.util;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class that creates various configuration files needed by the UUF project creation Mojo's.
 *
 * @since 1.0.0
 */
public class ConfigFileCreator {

    private static final String FILE_OSGI_IMPORTS = "osgi-imports";
    private static final String FILE_FEATURE_PROPERTIES = "feature.properties";
    private static final String FILE_P2_INF = "p2.inf";

    public static void createOsgiImports(String osgiImportsConfig, Path outputDirectory) throws MojoExecutionException {
        if ((osgiImportsConfig == null) || osgiImportsConfig.isEmpty()) {
            return;
        }

        Path osgiImportsFile = outputDirectory.resolve(FILE_OSGI_IMPORTS);
        try {
            createDirectory(outputDirectory);
            String osgiImports = Arrays.stream(osgiImportsConfig.trim().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("\n"));
            String content = applyTemplate(FILE_OSGI_IMPORTS, osgiImports);
            writeFile(osgiImportsFile, content);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create file '" + osgiImportsFile + "'. " + e.getMessage(), e);
        }
    }

    public static Path createFeatureProperties(Path outputDirectory) throws MojoExecutionException {
        Path buildPropertiesFile = outputDirectory.resolve(FILE_FEATURE_PROPERTIES);
        try {
            createDirectory(outputDirectory);
            String content = readTemplate(FILE_FEATURE_PROPERTIES);
            writeFile(buildPropertiesFile, content);
            return buildPropertiesFile;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create file '" + buildPropertiesFile + "'. " + e.getMessage(), e);
        }
    }

    public static Path createP2Inf(String featureName, Path outputDirectory) throws MojoExecutionException {
        Path p2InfFile = outputDirectory.resolve(FILE_P2_INF);
        try {
            createDirectory(outputDirectory);
            String content = applyTemplate(FILE_P2_INF, featureName, featureName);
            writeFile(p2InfFile, content);
            return p2InfFile;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create file '" + p2InfFile + "'. " + e.getMessage(), e);
        }
    }

    private static void createDirectory(Path directory) throws IOException {
        try {
            Files.createDirectories(directory);
        } catch (FileAlreadyExistsException e) {
            throw new IOException("Cannot create directory  '" + directory + "' as a file already exists in there.", e);
        } catch (IOException e) {
            throw new IOException("Cannot create directory '" + directory + "'.", e);
        }
    }

    private static String readTemplate(String template) throws IOException {
        try (InputStream featureProperties = ConfigFileCreator.class.getResourceAsStream("/templates/" + template)) {
            if (featureProperties == null) {
                throw new IOException("Cannot find template file '" + template + "' in classpath resources.");
            }
            return IOUtils.toString(featureProperties, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IOException("Cannot read template file '" + template + "' from classpath resources.", e);
        }
    }

    private static String applyTemplate(String template, String... variables) throws IOException {
        return String.format(readTemplate(template), variables);
    }

    private static void writeFile(Path file, String content) throws IOException {
        try {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Cannot write to file '" + file + "'.", e);
        }
    }
}
