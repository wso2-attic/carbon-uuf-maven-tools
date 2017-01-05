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

import org.wso2.carbon.uuf.maven.bean.Configuration;
import org.wso2.carbon.uuf.maven.exception.SerializationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * YAML serializer for {@link Configuration} model.
 *
 * @since 1.0.0
 */
public class ConfigurationSerializer {

    /**
     * Serializes the specified config into a YAML.
     *
     * @param data config data to serialize
     * @return YAML representation of the config
     * @throws SerializationException if an error occurred during serialization
     */
    public static String serialize(Object data) throws SerializationException {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setAllowReadOnlyProperties(true);
        Yaml yaml = new Yaml(dumperOptions);
        try {
            return yaml.dumpAs(data, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
        } catch (Exception e) {
            throw new SerializationException("Cannot serialize config " + data + ".", e);
        }
    }
}