package com.jflow.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.stream.StreamSupport;

public class ClassMethodLoggerInjector implements ClassFileTransformer {

    private static final ClassPool classPool = ClassPool.getDefault();

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer)
            throws IllegalClassFormatException {
        StringBuilder log = new StringBuilder("");


        // className can be null, ignoring such classes.
        if (className == null || className.startsWith("java") || className.startsWith("sun") || className.startsWith("com/flow/agent") || className.startsWith("jdk")) {
            return null;
        }
        System.out.println(className);
        // Javassist uses "." as a separator in class/package names.
        final String classNameDots = className.replaceAll("/", ".");
        CtClass ctClass;
        try {
            if((ctClass = classPool.getOrNull(classNameDots)) == null) {
                ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return classfileBuffer;
        }

        System.out.println(ctClass.getName());
        if(ctClass.getName().equals("JFlowLogger")){
            return classfileBuffer;
        }

        //log.append("visiting " + className + "\n");

        // Won't find some classes from java.lang.invoke,
        // but we're not interested in them anyway.
        if (ctClass == null) {
            log.append("dumped " + className + "; class is null" + "\n");
            System.out.println(log.toString());
            System.out.flush();
            return classfileBuffer;
        }

        // A frozen CtClass is a CtClass
        // that was already converted to Java class.
        if (ctClass.isFrozen()) {
            log.append("dumped " + className + "; frozen\n");
            // No longer need to keep the CtClass object in memory.
            ctClass.detach();
            System.out.println(log.toString());
            System.out.flush();
            return classfileBuffer;
        }

        try {
            // Behaviors == methods and constructors.
            for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
                if(behavior.isEmpty()){
                    log.append("\tmethod " + behavior.getName() + " is empty\n");
                    continue;
                }
                //log.append("\t" + behavior.getName() + "\n");
                try {

                    addMethodLogger(behavior, ctClass);

                }catch (Exception e){
                    log.append("\tunable to modify " + behavior + "\n");
                    log.append("\t" + e + "\n");
                }
            }

            System.out.println(log.toString());
            System.out.flush();
            return ctClass.toBytecode();

        } catch (Exception e) {
            log.append(e.toString());
        } finally {
            // No longer need to keep the CtClass object in memory.
            ctClass.detach();
        }

        System.out.println(log.toString());
        System.out.flush();
        return classfileBuffer;
    }


    private static void addMethodLogger(CtBehavior behavior, CtClass ctClass) throws CannotCompileException, NotFoundException {
        behavior.insertBefore("com.jflow.agent.JFlowLogger.log(\"calling " + behavior.getName() + " in " + ctClass.getName() + "\");");
        for(CtClass type : behavior.getParameterTypes()){
            System.out.println(type);
        }
        behavior.insertAfter("com.jflow.agent.JFlowLogger.log(\"return from " + behavior.getName() + " in " + ctClass.getName() + "\");");
    }
}
