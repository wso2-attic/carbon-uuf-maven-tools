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
 * Test cases for app's config bean.
 */
public class AppConfigTest {

    @Test
    public void testContextPathValidations() {
        AppConfig appConfig = createAppConfig();
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setContextPath(""));
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setContextPath("abc"));
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setContextPath("a/bc"));

        appConfig.setContextPath(null);
        appConfig.setContextPath("/abc");
    }

    @Test
    public void testThemeValidations() {
        AppConfig appConfig = createAppConfig();
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setTheme(""));

        appConfig.setTheme(null);
        appConfig.setTheme("org.wso2.carbon.uuf.sample.theme.default");
    }

    @Test
    public void testLoginPageUriValidations() {
        AppConfig appConfig = createAppConfig();
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setLoginPageUri(""));
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setLoginPageUri("login"));
        Assert.assertThrows(IllegalArgumentException.class, () -> appConfig.setLoginPageUri("l/ogin"));

        appConfig.setLoginPageUri(null);
        appConfig.setLoginPageUri("/sso/login");
    }

    @Test
    public void testErrorPagesValidations() {
        AppConfig appConfig = createAppConfig();
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> appConfig.setErrorPages(Collections.singletonMap("abc", "/foundation/error/404")));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> appConfig.setErrorPages(Collections.singletonMap("99", "/foundation/error/404")));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> appConfig.setErrorPages(Collections.singletonMap("600", "/foundation/error/404")));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> appConfig.setErrorPages(Collections.singletonMap("404", "")));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> appConfig.setErrorPages(Collections.singletonMap("404", "foundation/error/404")));

        appConfig.setErrorPages(null);
        appConfig.setErrorPages(Collections.emptyMap());
        appConfig.setErrorPages(Collections.singletonMap("404", "/foundation/error/404"));
        appConfig.setErrorPages(Collections.singletonMap("default", "/foundation/error/default"));
    }

    @Test
    public void testMenuValidations() {
        AppConfig.Menu menu = new AppConfig.Menu();
        Assert.assertThrows(IllegalArgumentException.class, () -> menu.setName(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> menu.setName(""));
        Assert.assertThrows(IllegalArgumentException.class, () -> menu.setItems(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> menu.setItems(Collections.emptyList()));

        menu.setName("main");
        menu.setItems(Collections.singletonList(new AppConfig.MenuItem()));

        AppConfig.MenuItem menuItem = new AppConfig.MenuItem();
        Assert.assertThrows(IllegalArgumentException.class, () -> menuItem.setText(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> menuItem.setLink(null));

        menuItem.setText("Home");
        menuItem.setLink("#");
        menuItem.setIcon(null);
        menuItem.setIcon("fw fw-user");
        menuItem.setSubmenus(null);
        menuItem.setSubmenus(Collections.emptyList());
        menuItem.setSubmenus(Collections.singletonList(new AppConfig.MenuItem()));
    }

    @Test
    public void testSecurityConfigValidations() {
        AppConfig.SecurityConfig securityConfig = new AppConfig.SecurityConfig();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> securityConfig.setCsrfIgnoreUris(Collections.singletonList("")));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> securityConfig.setCsrfIgnoreUris(Collections.singletonList("some/uri")));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> securityConfig.setXssIgnoreUris(Collections.singletonList("")));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> securityConfig.setXssIgnoreUris(Collections.singletonList("some/uri")));

        securityConfig.setCsrfIgnoreUris(Collections.singletonList("/valid/uri"));
        securityConfig.setXssIgnoreUris(Collections.singletonList("/valid/uri"));
        securityConfig.setCsrfIgnoreUris(null);
        securityConfig.setXssIgnoreUris(null);
        securityConfig.setCsrfIgnoreUris(Collections.singletonList(null));
        securityConfig.setXssIgnoreUris(Collections.singletonList(null));
    }

    private static AppConfig createAppConfig() {
        return new AppConfig();
    }
}
