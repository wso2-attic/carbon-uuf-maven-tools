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
import org.wso2.carbon.uuf.maven.bean.mojo.Bundle;
import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.wso2.carbon.uuf.maven.bean.mojo.BundleListConfig;
import org.wso2.carbon.uuf.maven.serializer.YamlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.maven.ComponentMojo.FILE_BUNDLE_DEPENDENCIES;

/**
 * Utility class that creates various configuration files needed by the UUF project creation Mojo's.
 *
 * @since 1.0.0
 */
public class ConfigFileCreator {

    private static final String FILE_CONFIGURATION_YAML = "configuration.yaml";
    private static final String FILE_DEPENDENCY_TREE = "dependency-tree.yaml";
    private static final String FILE_OSGI_IMPORTS = "osgi-imports";
    private static final String TEMPLATE_FEATURE_PROPERTIES = "feature.properties";
    private static final String TEMPLATE_P2_INF = "p2.inf";
    private static final String TEMPLATE_GENERATED_FILE = "generated-file";

    /**
     * Creates the OSGI imports file with the specified content in the specified path.
     *
     * @param osgiImportsContent  content of the OSGi imports file to be written
     * @param outputDirectoryPath path to the directory that OSGi imports file to be created
     * @throws MojoExecutionException if an error occurred when creating the OSGi imports file
     */
    public static void createOsgiImports(String osgiImportsContent, String outputDirectoryPath)
            throws MojoExecutionException {
        if ((osgiImportsContent == null) || osgiImportsContent.isEmpty()) {
            return;
        }

        try {
            createDirectory(Paths.get(outputDirectoryPath));
            String osgiImports = Arrays.stream(osgiImportsContent.trim().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("\n"));
            String content = applyTemplate(TEMPLATE_GENERATED_FILE, osgiImports);
            writeFile(Paths.get(outputDirectoryPath, FILE_OSGI_IMPORTS), content);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_OSGI_IMPORTS + "' file in '" + outputDirectoryPath + "'.", e);
        }
    }

    /**
     * Creates the bundle-dependencies.yaml file in the given output directory path.
     *
     * @param bundles the list of bundles instance used with creating the bundle-dependencies.yaml
     * @param outputDirectoryPath output location path use with creating yaml file
     * @throws MojoExecutionException thrown when an error occurs while creating or writing the yaml file
     */
    public static void createBundleDependenciesYaml(List<Bundle> bundles, String outputDirectoryPath)
            throws MojoExecutionException {
        if (bundles == null || bundles.isEmpty()) {
           return;
        }
        try {
            createDirectory(Paths.get(outputDirectoryPath));
            BundleListConfig bundleListConfig = new BundleListConfig();
            bundleListConfig.setBundles(bundles);
            String content;
            try {
                content = YamlSerializer.serialize(bundleListConfig);
            } catch (SerializationException e) {
                throw new MojoExecutionException("Cannot serialize configuration " + bundleListConfig + ".", e);
            }
            writeFile(Paths.get(outputDirectoryPath, FILE_BUNDLE_DEPENDENCIES),
                    applyTemplate(TEMPLATE_GENERATED_FILE, content));
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_BUNDLE_DEPENDENCIES + "' file in '" + outputDirectoryPath + "'.", e);
        }
    }

    /**
     * Creates the config file with the specified content in the specified path.
     *
     * @param configurationYamlContent content of the config file to be written
     * @param outputDirectoryPath      path to the directory that config file to be created
     * @throws MojoExecutionException if an error occurred when creating the config file
     */
    public static void createConfigurationYaml(String configurationYamlContent, String outputDirectoryPath)
            throws MojoExecutionException {
        try {
            String content = applyTemplate(TEMPLATE_GENERATED_FILE, configurationYamlContent);
            writeFile(Paths.get(outputDirectoryPath, FILE_CONFIGURATION_YAML), content);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_CONFIGURATION_YAML + "' file in '" + outputDirectoryPath + "'.", e);
        }
    }

    /**
     * Creates the dependency tree file with the specified content in the specified path.
     *
     * @param dependencyTreeContent content of the dependency tree file to be written
     * @param outputDirectoryPath   path to the directory that dependency tree file to be created
     * @throws MojoExecutionException if an error occurred when creating the dependency tree file
     */
    public static void createDependencyTree(String dependencyTreeContent, String outputDirectoryPath)
            throws MojoExecutionException {
        try {
            String content = applyTemplate(TEMPLATE_GENERATED_FILE, dependencyTreeContent);
            writeFile(Paths.get(outputDirectoryPath, FILE_DEPENDENCY_TREE), content);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + FILE_DEPENDENCY_TREE + "' file in '" + outputDirectoryPath + "'.", e);
        }
    }

    /**
     * Creates the Carbon Feature property file in the specified path.
     *
     * @param outputDirectoryPath path to the directory where the Carbon Feature property file should be created (will
     *                            be created if not exists)
     * @return path to the created Carbon Feature property file
     * @throws MojoExecutionException if an error occurred when creating the Carbon Feature property file
     */
    public static String createFeatureProperties(String outputDirectoryPath) throws MojoExecutionException {
        Path outputDirectory = Paths.get(outputDirectoryPath);
        Path buildPropertiesFile = outputDirectory.resolve(TEMPLATE_FEATURE_PROPERTIES);
        try {
            createDirectory(outputDirectory);
            String content = readTemplate(TEMPLATE_FEATURE_PROPERTIES);
            writeFile(buildPropertiesFile, content);
            return buildPropertiesFile.toString();
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + TEMPLATE_FEATURE_PROPERTIES + "' file in '" + outputDirectoryPath + "'.", e);
        }
    }

    /**
     * Creates the P2 INF file for the given feature in the specified path.
     *
     * @param featureName         name of the feature
     * @param outputDirectoryPath path to the directory where the P2 INF file should be created (will be created if not
     *                            exists)
     * @throws MojoExecutionException if an error occurred when creating the P2 INF file
     */
    public static void createP2Inf(String featureName, String outputDirectoryPath) throws MojoExecutionException {
        Path outputDirectory = Paths.get(outputDirectoryPath);
        Path p2InfFile = outputDirectory.resolve(TEMPLATE_P2_INF);
        try {
            createDirectory(outputDirectory);
            String content = applyTemplate(TEMPLATE_P2_INF, featureName, featureName);
            writeFile(p2InfFile, content);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Cannot create '" + TEMPLATE_P2_INF + "' file in '" + outputDirectoryPath + "'.", e);
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

    private static String readTemplate(String templateName) throws IOException {
        try (InputStream inputStream = ConfigFileCreator.class.getResourceAsStream("/templates/" + templateName)) {
            if (inputStream == null) {
                throw new IOException("Cannot find template file '" + templateName + "' in classpath resources.");
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IOException("Cannot read template file '" + templateName + "' from classpath resources.", e);
        }
    }

    private static String applyTemplate(String templateName, String... variables) throws IOException {
        return String.format(readTemplate(templateName), variables);
    }

    private static void writeFile(Path file, String content) throws IOException {
        try {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Cannot write to file '" + file + "'.", e);
        }
    }
}
