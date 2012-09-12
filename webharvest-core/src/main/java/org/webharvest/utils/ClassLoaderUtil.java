package org.webharvest.utils;

import org.webharvest.exception.PluginException;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class loading utility - used for loading JDBC driver classes and plugin classes.
 */
public class ClassLoaderUtil {

    // class loader that insludes all JAR libraries in the working folder of the application.
    private static URLClassLoader rootClassLoader = null;

    /**
     * Lists all JARs in the working folder (folder of WebHarvest executable)
     */
    private static void defineRootLoader() {
        java.util.List urls = new ArrayList();
        String rootDirPath = new File("").getAbsolutePath();

        try {
            urls.add(new File("").toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // add all JAR files from the root folder to the class path
        File[] entries = new File(rootDirPath).listFiles();
        if (entries != null) {
            for (int f = 0; f < entries.length; f++) {
                File entry = entries[f];
                if (entry != null && !entry.isDirectory() && entry.getName().toLowerCase().endsWith(".jar")) {
                    try {
                        String jarAbsolutePath = entry.getAbsolutePath();
                        urls.add(new URL("jar:file:/" + jarAbsolutePath.replace('\\', '/') + "!/"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        URL urlsArray[] = new URL[urls.size()];
        for (int i = 0; i < urls.size(); i++) {
            urlsArray[i] = (URL) urls.get(i);
        }

        rootClassLoader = new URLClassLoader(urlsArray);
    }

    public static void registerJDBCDriver(final String driverClassName)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        if (rootClassLoader == null) {
            defineRootLoader();
        }

        DriverManager.registerDriver((Driver) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class<?>[]{Driver.class},
                new InvocationHandler() {
                    final Driver driver = (Driver) Class.forName(driverClassName, true, rootClassLoader).newInstance();
                    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(driver, args);
                    }
                }));
    }

    public static Class getPluginClass(String fullClassName) throws PluginException {
        if (rootClassLoader == null) {
            defineRootLoader();
        }
        try {
            return Class.forName(fullClassName, true, rootClassLoader);
        } catch (ClassNotFoundException e) {
            throw new PluginException("Error finding plugin class \"" + fullClassName + "\": " + e.getMessage(), e);
        } catch (NoClassDefFoundError e) {
            throw new PluginException("Error finding plugin class \"" + fullClassName + "\": " + e.getMessage(), e);
        }
    }

}