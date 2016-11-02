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

package org.wso2.carbon.uuf.maven;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Zip archive assembling Mojo that is used for both uuf component and theme archive creation.
 *
 * @since 1.0.0
 */
public abstract class AbstractZipMojo extends AbstractAssemblyMojo implements UUFMojo {

    private static final String FORMAT = "zip";
    private static final List<String> FORMATS = Collections.singletonList(FORMAT);

    private static final List<String> EXCLUDING_FILE_PATTERNS = new ArrayList<>(
            Arrays.asList(
                    "**/*.iml", "**/*.ipr", "**/*.iwr",
                    "**/*.eclipse", "**/target/**", "**/pom.xml",
                    "**/assembly.xml"));

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Packaging type of the project.
     */
    @Parameter(defaultValue = "${project.packaging}", readonly = true, required = true)
    private String packaging;

    /**
     * Source directory path for UUF Maven plugin.
     */
    @Parameter(defaultValue = EXPRESSION_SOURCE_DIRECTORY_PATH, readonly = true, required = true)
    private String sourceDirectoryPath;

    /**
     * The output directory for UUF Maven plugin.
     */
    @Parameter(defaultValue = "${project.build.directory}/uuf-temp/", readonly = true, required = true)
    protected String outputDirectoryPath;

    /**
     * The artifact ID of the project.
     */
    @Parameter(defaultValue = "${project.artifactId}", readonly = true, required = true)
    protected String artifactId;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Maven AssemblyArchiver.
     */
    @Component
    private AssemblyArchiver assemblyArchiver;

    @Override
    public MavenProject getProject() {
        return project;
    }

    /**
     * Returns the base directory of the resulting zip archive.
     *
     * @return base directory of the zip archive
     */
    abstract String getZipBaseDirectory();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // create assembly
        Assembly assembly = new Assembly();
        assembly.setId("make-" + packaging);
        assembly.setBaseDirectory(getZipBaseDirectory());
        List<FileSet> fileSets = new ArrayList<>();
        fileSets.add(createFileSet(sourceDirectoryPath, "./"));
        fileSets.add(createFileSet(outputDirectoryPath, "./"));
        assembly.setFileSets(fileSets);
        assembly.setFormats(FORMATS);

        setAppendAssemblyId(false);
        String distributionName = AssemblyFormatUtils.getDistributionName(assembly, this);

        try {
            File archive = assemblyArchiver.createArchive(assembly, distributionName, FORMAT, this, true);
            project.getArtifact().setFile(archive);
            projectHelper.attachArtifact(project, FORMAT, null, archive);
        } catch (ArchiveCreationException | AssemblyFormattingException e) {
            throw new MojoExecutionException("Failed to create assembly '" + distributionName + "'.", e);
        } catch (InvalidAssemblerConfigurationException e) {
            throw new MojoFailureException(assembly,
                                           "Assembly '" + distributionName + "'is incorrectly configured.",
                                           "Assembly '" + distributionName + "'is incorrectly configured: " +
                                                   e.getMessage());
        }
    }

    private FileSet createFileSet(String sourceDirectory, String destinationDirectory) {
        FileSet fileSet = new FileSet();
        fileSet.setDirectory(sourceDirectory);
        fileSet.setOutputDirectory(destinationDirectory);
        fileSet.setExcludes(EXCLUDING_FILE_PATTERNS);
        return fileSet;
    }
}
