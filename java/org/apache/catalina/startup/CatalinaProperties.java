/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.startup;

import org.apache.catalina.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;


/**
 * Utility class to read the bootstrap Catalina configuration.
 *
 * @author Remy Maucherat
 */

public class CatalinaProperties {


    // ------------------------------------------------------- Static Variables

    private static final org.apache.juli.logging.Log log=org.apache.juli.logging.LogFactory.getLog( CatalinaProperties.class );

    private static Properties properties = null;


    static {
        loadProperties();
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Return specified property value.
     */
    public static String getProperty(String name) {
        return properties.getProperty(name);
    }


    /**
     * Return specified property value.
     *
     * @deprecated  Unused - will be removed in 8.0.x
     */
    @Deprecated
    public static String getProperty(String name, String defaultValue) {

        return properties.getProperty(name, defaultValue);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Load properties.
     * 加载 conf 下的catalina.properties 文件，该文件里配置了 common.loader，值为：
     * common.loader=${catalina.base}/lib,${catalina.base}/lib/*.jar,${catalina.home}/lib,${catalina.home}/lib/*.jar
     *
     */
    private static void loadProperties() {
        InputStream is = null;
        Throwable error = null;

        try {
            String configUrl = getConfigUrl();
            if (configUrl != null) {
                is = (new URL(configUrl)).openStream();
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }

        /**
         * 获取 catalina.properties 文件，将里面的属性设置到System中。
         * 最开始System可能没有这些属性。
         */
        if (is == null) {
            try {
                File home = new File(getCatalinaBase()); // C:\CodeRepository\tomcat7
                File conf = new File(home, "conf"); // C:\CodeRepository\tomcat7\conf
                File propsFile = new File(conf, "catalina.properties"); // C:\CodeRepository\tomcat7\conf\catalina.properties
                is = new FileInputStream(propsFile);
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        if (is == null) {
            try {
                is = CatalinaProperties.class.getResourceAsStream("/org/apache/catalina/startup/catalina.properties");
            } catch (Throwable t) {
                handleThrowable(t);
            }
        }

        if (is != null) {
            try {
                properties = new Properties();
                properties.load(is);
            } catch (Throwable t) {
                handleThrowable(t);
                error = t;
            }finally{
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.warn("Could not close catalina.properties", ioe);
                }
            }
        }

        if ((is == null) || (error != null)) {
            // Do something
            log.warn("Failed to load catalina.properties", error);
            // That's fine - we have reasonable defaults.
            properties=new Properties();
        }

        /**
         * Register the properties as system properties
         * properties.propertyNames() --> 返回属性列表中所有键的枚举，如果在主属性列表中未找到同名的键，则包括默认属性列表中不同的键。
         * 将 catalina.properties 中配置的键值对写到System的属性中。
         */
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = properties.getProperty(name);
            if (value != null) {
                System.out.println("加载catalina.properties中配置的键值对:name = "+name+"; value = "+value);
                System.setProperty(name, value);
            }
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     * 用户的主目录 user.dir 工程所在的目录。 C:\CodeRepository\tomcat7
     *   System.getProperty(String key,String def) 获取用指定键描述的系统属性。
     *   key - 系统属性的名称。def - 默认值。 系统属性的字符串值，如果没有带有此键的属性，则返回默认值。
     */
    private static String getCatalinaHome() {
        String userDir=System.getProperty("user.dir");
        log.info("user.dir  ======"+userDir);
        String catalinaHome=System.getProperty(Globals.CATALINA_HOME_PROP,userDir);
        log.info("catalina.home  ======"+catalinaHome);
        return System.getProperty(Globals.CATALINA_HOME_PROP,userDir);
    }
    
    
    /**
     * Get the value of the catalina.base environment variable.
     */
    private static String getCatalinaBase() {
        return System.getProperty(Globals.CATALINA_BASE_PROP, getCatalinaHome());
    }


    /**
     * Get the value of the configuration URL.
     */
    private static String getConfigUrl() {
        return System.getProperty("catalina.config");
    }

    // Copied from ExceptionUtils since that class is not visible during start
    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

}
