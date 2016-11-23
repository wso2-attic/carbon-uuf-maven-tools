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

package org.wso2.carbon.uuf.maven.serializer;

import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.wso2.carbon.uuf.maven.model.Configuration;
import org.yaml.snakeyaml.Yaml;

/**
 * YAML serializer for {@link Configuration} model.
 *
 * @since 1.0.0
 */
public class ConfigurationSerializer {

    private final Yaml yaml = new Yaml();

    /**
     * Serializes the specified config into a YAML.
     *
     * @param configuration config to serialize
     * @return YAML representation of the config
     * @throws SerializationException if an error occurred during serialization
     */
    public String serialize(Configuration configuration) throws SerializationException {
        try {
            return yaml.dumpAsMap(configuration.asMap());
        } catch (Exception e) {
            throw new SerializationException("Cannot serialize config " + configuration + ".", e);
        }
    }
}
