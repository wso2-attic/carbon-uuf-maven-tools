/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugin.assembly.mojos.AbstractAssemblyMojo;
import org.apache.maven.plugin.assembly.utils.AssemblyFormatUtils;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractUUFMojo extends AbstractAssemblyMojo {

    protected static final String UUF_COMPONENT_ASSEMBLY_FORMAT = "zip";
    protected static final String UUF_THEME_ASSEMBLY_FORMAT = "tar";
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * Maven AssemblyArchiver.
     */
    @Component
    private AssemblyArchiver assemblyArchiver;
    /**
     * Indicates if zip archives (jar,zip etc) being added to the assembly should be compressed again. Compressing again
     * can result in smaller archive size, but gives noticeably longer execution time.
     */
    @Parameter(defaultValue = "true")
    private boolean recompressZippedFiles;
    /**
     * sets the merge manifest mode in the JarArchiver
     */
    @Parameter
    private String mergeManifestMode;
    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;
    /**
     * Controls whether the assembly plugin tries to attach the resulting assembly to the project.
     */
    @Parameter(property = "assembly.attach", defaultValue = "true")
    private boolean attach;
    /**
     * The filename of the assembled distribution file.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;
    /**
     * The artifactId of the project.
     */
    @Parameter(defaultValue = "${project.artifactId}", required = true, readonly = true)
    private String artifactId;
    @Parameter(defaultValue = "${import.package}", readonly = true)
    private List<String> osgiImports;
    /**
     * The output directory of the assembled distribution file.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String
            outputDirectoryPath;

    public abstract Assembly getAssembly() throws MojoFailureException;

    /**
     * Create the binary distribution.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        // AbstractAssemblyMojo does not allow child classes to plug-in a custom AssemblyReader.
        // Hence needed to reimplement the below method.
        setAppendAssemblyId(false);
        createOsgiImportsConfig();
        Assembly assembly = getAssembly();
        List<String> formats = assembly.getFormats();
        if (formats.isEmpty()) {
            throw new MojoFailureException("Assembly is incorrectly configured: " + assembly.getId() +
                                                   "archive format is not specified");
        }
        final String fullName = AssemblyFormatUtils.getDistributionName(assembly, this);
        try {
            final File destFile = assemblyArchiver.createArchive(assembly, fullName, formats.get(0), this,
                                                                 recompressZippedFiles);
            final MavenProject project = getProject();
            final String classifier = getClassifier();
            final String type = project.getArtifact().getType();
            if (attach && destFile.isFile()) {
                if (isAssemblyIdAppended()) {
                    projectHelper.attachArtifact(project, UUF_COMPONENT_ASSEMBLY_FORMAT, assembly.getId(), destFile);
                } else if (classifier != null) {
                    projectHelper.attachArtifact(project, UUF_COMPONENT_ASSEMBLY_FORMAT, classifier, destFile);
                } else if (!"pom".equals(type) && UUF_COMPONENT_ASSEMBLY_FORMAT.equals(type)) {
                    final StringBuilder message = new StringBuilder();
                    message.append("Configuration options: 'appendAssemblyId' is set to false, "
                                           + "and 'classifier' is missing.");
                    message.append("\nInstead of attaching the assembly file: ").append(destFile);
                    message.append(", it will become the file for main project artifact.");
                    message.append("\nNOTE: If multiple descriptors or descriptor-formats are provided " +
                                           "for this project, the value of this file will be " + "non-deterministic!");
                    getLog().warn(message);
                    final File existingFile = project.getArtifact().getFile();
                    if ((existingFile != null) && existingFile.exists()) {
                        getLog().warn("Replacing pre-existing project main-artifact file: " + existingFile +
                                              "\n with assembly file: " + destFile);
                    }
                    project.getArtifact().setFile(destFile);
                } else {
                    projectHelper.attachArtifact(project, UUF_COMPONENT_ASSEMBLY_FORMAT, null, destFile);
                }
            } else if (attach) {
                getLog().warn("Assembly file: " + destFile + " is not a regular file (it may be a directory). " +
                                      "It cannot be attached to the project build for installation or " +
                                      "deployment.");
            }
        } catch (final ArchiveCreationException | AssemblyFormattingException e) {
            throw new MojoExecutionException("Failed to create assembly: " + e.getMessage(), e);
        } catch (final InvalidAssemblerConfigurationException e) {
            throw new MojoFailureException(assembly, "Assembly is incorrectly configured: " + assembly.getId(),
                                           "Assembly: " + assembly.getId() + " is not configured correctly: " +
                                                   e.getMessage());
        }
    }

    protected FileSet createFileSet(String sourceDirectory, String destDirectory) {
        FileSet fileSet = new FileSet();
        fileSet.setDirectory(sourceDirectory);
        fileSet.setOutputDirectory(destDirectory);
        fileSet.setExcludes(createExcludesList());
        return fileSet;
    }

    protected List<String> createExcludesList() {
        List<String> excludes = new ArrayList<>();
        excludes.add("**/target/**");
        excludes.add("**/pom.xml");
        excludes.add("**/assembly.xml");
        excludes.add("**/*.iml");
        excludes.add("**/*.ipr");
        excludes.add("**/*.iwr");
        excludes.add("**/*.eclipse");
        return excludes;
    }

    protected List<FileSet> createFileSetList(FileSet... fileSets) {
        return Arrays.asList(fileSets);
    }

    public void createOsgiImportsConfig() throws MojoExecutionException {
        if (osgiImports.isEmpty()) {
            //if no osgi imports found, just skip file creation...
            return;
        }
        Path uufOsgiConfigOutDirectory = getUUFOsgiConfigOutDirectory();
        try {
            createDirectoryIfNotExists(uufOsgiConfigOutDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating directory: " + uufOsgiConfigOutDirectory, e);
        }
        Path osgiImportsConfig = uufOsgiConfigOutDirectory.resolve("osgi-imports");
        StringBuilder content = new StringBuilder();
        content.append("# Auto-generated by UUF Maven Plugin. Do NOT modify manually.\n");
        for (String importLine : getOsgiImports()) {
            content.append(importLine.trim()).append("\n");
        }
        try {
            Files.write(osgiImportsConfig, content.toString().getBytes());
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create file '" + osgiImportsConfig +
                                                     "' when trying to create osgi imports config", e);
        }
    }

    protected boolean createDirectoryIfNotExists(Path directory) throws IOException {
        try {
            Files.createDirectories(directory);
            return true;
        } catch (FileAlreadyExistsException e) {
            // the directory already exists.
            return false;
        }
    }

    @Override
    public MavenProject getProject() {
        return project;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public List<String> getOsgiImports() {
        return osgiImports;
    }

    protected String getSimpleArtifactId() {
        int lastIndex = artifactId.lastIndexOf(".");
        if (lastIndex > -1) {
            return artifactId.substring(lastIndex + 1);
        }
        return artifactId;
    }

    protected Path getUUFTempDirectory() {
        return Paths.get(outputDirectoryPath + "/uuf-temp");
    }

    protected Path getUUFOsgiConfigOutDirectory() {
        return getUUFTempDirectory();
    }
}