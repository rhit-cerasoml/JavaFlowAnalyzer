package com.jflow.agent;

import javassist.*;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst){
        System.out.println("");
        try {
            generateJFlowLogger();
            Class<?> loggerClass = Class.forName("com.jflow.agent.JFlowLogger");
            Method logMethod = loggerClass.getDeclaredMethod("initLogger", String.class);
            logMethod.invoke(null, "log.txt");
        }catch (Exception e){
            System.out.println("Failed to inject JFlowLogger!");
            e.printStackTrace();
        }
        inst.addTransformer(new ClassMethodLoggerInjector());
        System.out.println("Agent Done!");
    }

    private static void generateJFlowLogger() throws Exception {
        // Create a new class called "JFlowLogger"
        ClassPool classPool = ClassPool.getDefault();
        CtClass loggerClass = classPool.makeClass("com.jflow.agent.JFlowLogger");

        CtField fileField = new CtField(classPool.get("java.io.PrintWriter"), "logFileWriter", loggerClass);
        fileField.setModifiers(Modifier.STATIC);
        loggerClass.addField(fileField);

        // Add a public static log method to write to the file
        CtMethod initMethod = CtNewMethod.make(
                "public static void initLogger(String path) { " +
                            "try { " +
                            "    logFileWriter = new java.io.PrintWriter(new java.io.FileWriter(path, true)); " +
                            "} catch (java.io.IOException e) { " +
                            "    e.printStackTrace(); " +
                            "}" +
                        "}",
                loggerClass
        );
        loggerClass.addMethod(initMethod);


        System.out.println(loggerClass.getFields()[0].getName());

        // Add a public static log method to write to the file
        CtMethod logMethod = CtNewMethod.make(
                "public static void log(String msg) { " +
                        "logFileWriter.println(msg);\n" +
                        "logFileWriter.flush();\n" +
                        "}",
                loggerClass
        );
        loggerClass.addMethod(logMethod);

        defineClassWithLookup(loggerClass.toBytecode());

        Class.forName("com.jflow.agent.JFlowLogger");
    }

    private static void defineClassWithLookup(byte[] classBytes) throws Exception {
        // Get a Lookup object with private access
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Use reflection to access the private defineClass method in MethodHandles.Lookup
        Method defineClassMethod = MethodHandles.Lookup.class.getDeclaredMethod("defineClass", byte[].class);
        defineClassMethod.setAccessible(true);

        // Define the class using the method
        defineClassMethod.invoke(lookup, (Object) classBytes);
    }


}
