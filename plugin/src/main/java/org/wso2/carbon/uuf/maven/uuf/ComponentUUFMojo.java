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

package org.wso2.carbon.uuf.maven.uuf;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a UUF component artifact.
 */
@Mojo(name = "create-component", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.COMPILE,
      threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class ComponentUUFMojo extends AbstractUUFMojo {

    @Override
    protected Assembly getAssembly() throws MojoFailureException {
        return createComponentAssembly("make-component", "/" + getSimpleArtifactId());
    }

    private Assembly createComponentAssembly(String assemblyId, String baseDirectory) {
        Assembly assembly = new Assembly();
        assembly.setId(assemblyId);
        assembly.setBaseDirectory(baseDirectory);

        FileSet fileSet1 = createFileSet(getBasedir().getAbsolutePath(), "./");
        FileSet fileSet2 = createFileSet(getUUFTempDirectory().toString(), "./");
        assembly.setFileSets(createFileSetList(fileSet1, fileSet2));

        List<String> formatsList = new ArrayList<>();
        formatsList.add(UUF_COMPONENT_ASSEMBLY_FORMAT);
        assembly.setFormats(formatsList);
        return assembly;
    }
}
