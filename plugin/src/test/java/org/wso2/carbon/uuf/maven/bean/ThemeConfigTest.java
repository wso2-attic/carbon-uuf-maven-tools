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

package org.wso2.carbon.uuf.maven.bean;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Test cases for theme's config bean.
 */
public class ThemeConfigTest {

    @Test
    public void testCssRelativePathValidations() {
        ThemeConfig themeConfig = new ThemeConfig();
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> themeConfig.setCss(Collections.singletonList("/css/style.css")));

        themeConfig.setCss(null);
        themeConfig.setCss(Collections.emptyList());
        themeConfig.setCss(Collections.singletonList("css/style.css"));
    }

    @Test
    public void testHeadJsRelativePathValidations() {
        ThemeConfig themeConfig = new ThemeConfig();
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> themeConfig.setHeadJs(Collections.singletonList("/js/test.js")));

        themeConfig.setHeadJs(null);
        themeConfig.setHeadJs(Collections.emptyList());
        themeConfig.setHeadJs(Collections.singletonList("js/test.js"));
    }

    @Test
    public void testJsRelativePathValidations() {
        ThemeConfig themeConfig = new ThemeConfig();
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> themeConfig.setJs(Collections.singletonList("/js/test.js")));

        themeConfig.setJs(null);
        themeConfig.setJs(Collections.emptyList());
        themeConfig.setJs(Collections.singletonList("js/test.js"));
    }
}
