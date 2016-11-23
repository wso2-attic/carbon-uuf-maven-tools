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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class that creates various configuration files needed by the UUF project creation Mojo's.
 *
 * @since 1.0.0
 */
public class ConfigFileCreator {

    private static final String FILE_OSGI_IMPORTS = "osgi-imports";
    private static final String FILE_CONFIG_YAML = "config.yaml";
    private static final String FILE_DEPENDENCY_TREE = "dependency-tree.yaml";
    private static final String FILE_FEATURE_PROPERTIES = "feature.properties";
    private static final String FILE_P2_INF = "p2.inf";

    public static void createOsgiImports(String osgiImportsContent, String outputDirectoryPath)
            throws MojoExecutionException {
        if ((osgiImportsContent == null) || osgiImportsContent.isEmpty()) {
            return;
        }

        try {
            String osgiImports = Arrays.stream(osgiImportsContent.trim().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("\n"));
            String content = applyTemplate(FILE_OSGI_IMPORTS, osgiImports);
            writeFile(Paths.get(outputDirectoryPath, FILE_OSGI_IMPORTS), content);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_OSGI_IMPORTS + "' file in '" + outputDirectoryPath + "'. " +
                            e.getMessage(), e);
        }
    }

    public static void createConfigYaml(String configYamlContent, String outputDirectoryPath)
            throws MojoExecutionException {
        try {
            writeFile(Paths.get(outputDirectoryPath, FILE_CONFIG_YAML), configYamlContent);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create '" + FILE_CONFIG_YAML + "' file in '" + outputDirectoryPath
                                                     + "'. " + e.getMessage(), e);
        }
    }

    public static void createDependencyTree(String dependencyTreeContent, String outputDirectoryPath)
            throws MojoExecutionException {
        try {
            writeFile(Paths.get(outputDirectoryPath, FILE_DEPENDENCY_TREE), dependencyTreeContent);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_DEPENDENCY_TREE + "' file in '" + outputDirectoryPath + "'. " +
                            e.getMessage(), e);
        }
    }

    public static String createFeatureProperties(String outputDirectoryPath) throws MojoExecutionException {
        Path outputDirectory = Paths.get(outputDirectoryPath);
        Path buildPropertiesFile = outputDirectory.resolve(FILE_FEATURE_PROPERTIES);
        try {
            createDirectory(outputDirectory);
            String content = readTemplate(FILE_FEATURE_PROPERTIES);
            writeFile(buildPropertiesFile, content);
            return buildPropertiesFile.toString();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create file '" + buildPropertiesFile + "'. " + e.getMessage(), e);
        }
    }

    public static void createP2Inf(String featureName, String outputDirectoryPath) throws MojoExecutionException {
        Path outputDirectory = Paths.get(outputDirectoryPath);
        Path p2InfFile = outputDirectory.resolve(FILE_P2_INF);
        try {
            createDirectory(outputDirectory);
            String content = applyTemplate(FILE_P2_INF, featureName, featureName);
            writeFile(p2InfFile, content);
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
