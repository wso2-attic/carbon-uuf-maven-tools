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

package org.wso2.carbon.uuf.maven.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean class that represents the configurations of an UUF APP or Component.
 *
 * @since 1.0.0
 */
public class Configuration {

    private final Map<String, Object> map = new HashMap<>();
    private final Map<String, Object> unmodifiableMap = Collections.unmodifiableMap(map);

    /**
     * Merges the specified new {@link Map} with the map of this Configuration.
     *
     * @param rawMap map to merge
     * @throws IllegalArgumentException if keys of the {@code rawMap} are not {@link String}s.
     */
    public void merge(Map<?, ?> rawMap) {
        for (Map.Entry<?, ?> newEntry : rawMap.entrySet()) {
            if (!(newEntry.getKey() instanceof String)) {
                throw new IllegalArgumentException(
                        "'rawMap' variable must be a Map<String, Object>. Instead found a '" +
                                newEntry.getKey().getClass().getName() + "' key.");
            }

            map.compute((String) newEntry.getKey(), (key, oldValue) -> {
                Object newValue = newEntry.getValue();
                if (oldValue == null) {
                    return newValue; // There is no old value, so just add the new value.
                }

                if (newValue instanceof Map && oldValue instanceof Map) {
                    // Both new value and old value are Maps, so merge new value Map to the old value Map..
                    return deepMergeMap((Map) oldValue, (Map) newValue);
                } else if (newValue instanceof List && oldValue instanceof List) {
                    // Both new value and old value are Lists, so merge new value List to the old value List.
                    return deepMergeList((List) oldValue, (List) newValue);
                } else {
                    // Cannot merge if not a Map nor a List, hence replace with the old value with new value.
                    return newValue;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Map deepMergeMap(Map oldMap, Map newMap) {
        for (Object key : newMap.keySet()) {
            Object newValueObj = newMap.get(key);
            Object oldValueObj = oldMap.get(key);
            if (oldValueObj instanceof Map && newValueObj instanceof Map) {
                oldMap.put(key, deepMergeMap((Map) oldValueObj, (Map) newValueObj));
            } else if (oldValueObj instanceof List && newValueObj instanceof List) {
                oldMap.put(key, deepMergeList((List) oldValueObj, (List) newValueObj));
            } else {
                oldMap.put(key, newValueObj);
            }
        }
        return oldMap;
    }

    @SuppressWarnings("unchecked")
    private List deepMergeList(List oldList, List newList) {
        for (Object newItemObj : newList) {
            int oldIndex = oldList.indexOf(newItemObj);
            if (oldIndex != -1) {
                Object oldItemObj = oldList.get(oldIndex);
                if (oldItemObj instanceof List && newItemObj instanceof List) {
                    oldList.set(oldIndex, deepMergeList((List) oldItemObj, (List) newItemObj));
                } else if (oldItemObj instanceof Map && newItemObj instanceof Map) {
                    oldList.set(oldIndex, deepMergeMap((Map) oldItemObj, (Map) newItemObj));
                } else {
                    oldList.set(oldIndex, newItemObj);
                }
            } else {
                oldList.add(newItemObj);
            }
        }
        return oldList;
    }

    /**
     * Returns this Configuration object as a Map.
     *
     * @return unmodifiable map
     */
    public Map<String, Object> asMap() {
        return unmodifiableMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return map.toString();
    }
}
