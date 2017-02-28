/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

@Mojo(name = "test-jar", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class TestJarMojo extends AbstractUUFMojo {

    /**
     * The Maven session associated with this Mojo.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Plugin manager to execute other Maven plugins.
     */
    @Component(hint = "default")
    private BuildPluginManager pluginManager;

    /**
     * Maven Compiler Plugin version to use.
     */
    @Parameter(defaultValue = "3.6.1", readonly = true, required = false)
    private String compilerPluginVersion;

    // Parameters used to build the test-jar file
    /**
     * Set this to 'true' to bypass compilation of test sources.
     * Its use is NOT RECOMMENDED, but quite convenient on occasion.
     */
    @Parameter(property = "maven.test.skip")
    private boolean skipTest;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Name of the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;

    /**
     * The Jar archiver.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    /**
     * Directory containing the test classes and resource files that should be packaged into the JAR.
     */
    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true)
    private File testClassesDirectory;

    /**
     * Classifier to used for {@code test-jar}.
     */
    @Parameter(defaultValue = "tests")
    private String classifier;

    /**
     * The {@link MavenProjectHelper}.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The archive configuration to use. See <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
     * Archiver Reference</a>.
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Denotes if the implementation details should be added to the manifest file. See
     * <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver Reference</a>.
     */
    @Parameter(defaultValue = "true", readonly = true, required = false)
    private boolean addDefaultImplementationEntries;

    /**
     * Denotes if the specification details should be added to the manifest file. See
     * <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven Archiver Reference</a>.
     */
    @Parameter(defaultValue = "true", readonly = true, required = false)
    private boolean addDefaultSpecificationEntries;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skipTest) {
            compileTestSrc();
            createTestJar();
        }
    }

    private void compileTestSrc() throws MojoExecutionException {
        try {
            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-compiler-plugin"),
                            version(compilerPluginVersion)
                    ),
                    goal("testCompile"),
                    configuration(),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new MojoExecutionException("Error when compiling test source for '" + artifactId + "'.", e);
        }
    }

    private void createTestJar() throws MojoExecutionException {
        // construct the test jar name
        String fileName = finalName + "-" + classifier + ".jar";

        // create the test jar file
        File jarFile = new File(outputDirectory, fileName);
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);
        archiver.getArchiver().addDirectory(testClassesDirectory, null, null);

        try {
            archive.getManifest().setAddDefaultImplementationEntries(addDefaultImplementationEntries);
            archive.getManifest().setAddDefaultSpecificationEntries(addDefaultSpecificationEntries);
            archiver.createArchive(session, project, archive);
            projectHelper.attachArtifact(project, "test-jar", classifier, jarFile);
        } catch (Exception e) {
            throw new MojoExecutionException("Error occurred while creating test jar for '" + artifactId + "'.", e);
        }
    }
}
