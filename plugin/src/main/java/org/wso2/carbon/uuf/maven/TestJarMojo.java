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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        compileTestSrc();
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
}
