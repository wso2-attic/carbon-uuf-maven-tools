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

package org.wso2.carbon.uuf.maven.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility for zip archive associated operations.
 *
 * @since 1.0.0
 */
public class ZipCreator {

    /**
     * Archive format.
     */
    public static final String ARCHIVE_FORMAT = "zip";
    /**
     * Archive extension.
     */
    public static final String ARCHIVE_EXTENSION = "." + ARCHIVE_FORMAT;

    /**
     * Creates a zip archive.
     *
     * @param sourceDirectoryPaths path of directories those have the files and sub-directories to be added to the zip
     * @param baseDirectoryName    base directory of the creating zip archive, pass {@code null} for no base directory
     * @param outputDirectoryPath  path to the directory where the zip archive is created
     * @param archiveFileName      filename of the creating zip archive without the ".zip" extension
     * @return created zip archive file
     * @throws MojoExecutionException if an error occurred when creating the zip archive
     */
    public static File createArchive(List<String> sourceDirectoryPaths, String baseDirectoryName,
                                     String outputDirectoryPath,
                                     String archiveFileName) throws MojoExecutionException {
        String correctedBaseDirectory;
        if (baseDirectoryName == null) {
            correctedBaseDirectory = null;
        } else {
            correctedBaseDirectory = baseDirectoryName.endsWith("/") ? baseDirectoryName : (baseDirectoryName + "/");
        }

        Archiver zipArchiver = new ZipArchiver();
        for (String sourceDirectoryPath : sourceDirectoryPaths) {
            DefaultFileSet fileSet = new DefaultFileSet(new File(sourceDirectoryPath));
            fileSet.setPrefix(correctedBaseDirectory);
            zipArchiver.addFileSet(fileSet);
        }

        File outputZipFile = new File(outputDirectoryPath, (archiveFileName + ARCHIVE_EXTENSION));
        zipArchiver.setDestFile(outputZipFile);
        try {
            zipArchiver.createArchive();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create zip archive '" + outputZipFile.getPath() +
                                                     "' from directories " + sourceDirectoryPaths + ".", e);
        }
        return outputZipFile;
    }
}
