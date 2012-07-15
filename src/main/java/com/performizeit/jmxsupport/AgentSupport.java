/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.jmxsupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author yadidh
 */
public class AgentSupport {

    public static Class addToolsJar() {
        try {
            return com.sun.tools.attach.VirtualMachine.class;
        } catch (Throwable t) {
            System.out.println("tools.jar not in class path ");
            File toolsJar = new File(System.getProperty("java.home") + "/lib/tools.jar"); //when jdk
            System.out.println("try:" + toolsJar);
            if (toolsJar.exists()) {
                addURL(toolsJar);
                System.out.println(toolsJar);
            } else {
                toolsJar = new File(System.getProperty("java.home") + "/../lib/tools.jar"); // when jre part of jdk
                System.out.println("try:" + toolsJar);
                if (toolsJar.exists()) {
                    addURL(toolsJar);
                    System.out.println(toolsJar);
                } else {
                    System.out.println("Unable to locate tools.jar pls add it to classpath");
                }
            }
        }
        return com.sun.tools.attach.VirtualMachine.class;


    }

    public static void addURL(File file) throws RuntimeException {
        try {
            URL url = file.toURL();
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class clazz = URLClassLoader.class;

            // Use reflection
            Method method = clazz.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(classLoader, new Object[]{url});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void loadAgent(int pid, String agentPath,String params) throws Exception {
                
        com.sun.tools.attach.VirtualMachine vm =
                com.sun.tools.attach.VirtualMachine.attach(pid +"");
    
        vm.loadAgent(agentPath,params);

                
    }
}
