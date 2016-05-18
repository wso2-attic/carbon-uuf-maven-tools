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

package org.wso2.carbon.maven.util;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class MojoUtils {

    public static class AppsFinder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final Path destination;
        private final Path source;
        private final Set<Path> applications = new HashSet<>();
        private Log log;

        public AppsFinder(String pattern, Path destination, Path source) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            this.destination = destination;
            this.source = source;
        }

        private boolean isRootComponent(Path file) {
            return matcher.matches(file);
        }

        /**
         * This will return all applications found.
         *
         * @return all applications
         */
        public Set<Path> getApplications() {
            return applications;
        }

        /**
         * This method will be called when visiting all *files* inside root component. This will copy files into
         * destination.
         *
         * @param file  visiting file
         * @param attrs file attributes
         * @return FileVisitResult
         * @throws IOException
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (isRootComponent(file)) {
                String relativePath = source.relativize(file).toString();
                addApplication(relativePath);
                try {
                    Files.copy(file, destination.resolve(getPathWithoutAppName(relativePath)));
                } catch (FileAlreadyExistsException e) {
                    getLog().warn("File Already Exists! Ignoring `" + file + "`");
                }
            }
            return FileVisitResult.CONTINUE;
        }

        /**
         * This method will be called when visiting all *folders* inside root component. This will create new folders in
         * destination relative to the root component. relative to the root component.
         *
         * @param dir   visiting directory
         * @param attrs directory attributes
         * @return FileVisitResult
         * @throws IOException
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (isRootComponent(dir)) {
                String relativePath = source.relativize(dir).toString();
                addApplication(relativePath);
                try {
                    Files.createDirectories(destination.resolve(getPathWithoutAppName(relativePath)));
                } catch (FileAlreadyExistsException e) {
                    //ignore
                }
            }
            return FileVisitResult.CONTINUE;
        }

        /**
         * Deletes all matched applications.
         */
        public void deleteMatchedApplications() {
            for (Path application : applications) {
                FileUtils.deleteQuietly(application.toFile());
            }
        }

        private void addApplication(String relativePath) {
            Path application = source.resolve(relativePath.substring(0, relativePath.indexOf(File.separator)));
            applications.add(application);
        }

        private String getPathWithoutAppName(String relativePath) {
            int thirdSlash = indexOfNthOccurrence(relativePath, File.separator, 3);
            return relativePath.substring(thirdSlash + 1);
        }

        private int indexOfNthOccurrence(String str, String toFind, int occurrence) {
            int index = str.indexOf(toFind);
            int found = 0;
            while (++found < occurrence && index > 0) {
                index = str.indexOf(toFind, index + 1);
            }
            return index;
        }

        private Log getLog() {
            if (this.log == null) {
                this.log = new SystemStreamLog();
            }
            return this.log;
        }
    }
}
