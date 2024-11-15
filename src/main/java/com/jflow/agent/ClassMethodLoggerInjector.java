package com.jflow.agent;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassMethodLoggerInjector implements ClassFileTransformer {

    private final ClassPool classPool = ClassPool.getDefault();

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer)
            throws IllegalClassFormatException {

        // className can be null, ignoring such classes.
        if (className == null) {
            return null;
        }

        // Javassist uses "." as a separator in class/package names.
        final String classNameDots = className.replaceAll("/", ".");
        final CtClass ctClass = classPool.getOrNull(classNameDots);

        System.out.println("visiting " + className);

        // Won't find some classes from java.lang.invoke,
        // but we're not interested in them anyway.
        if (ctClass == null) {
            System.out.flush();
            return classfileBuffer;
        }

        // A frozen CtClass is a CtClass
        // that was already converted to Java class.
        if (ctClass.isFrozen()) {
            System.out.println("dumped " + className + "; frozen");
            // No longer need to keep the CtClass object in memory.
            ctClass.detach();
            System.out.flush();
            return classfileBuffer;
        }

        try {
            boolean anyMethodInstrumented = false;

            // Behaviors == methods and constructors.
            for (final CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
                System.out.println("\t" + behavior.getName());
            }

            if (anyMethodInstrumented) {
                System.out.flush();
                return ctClass.toBytecode();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            // No longer need to keep the CtClass object in memory.
            ctClass.detach();
        }

        System.out.flush();
        return classfileBuffer;
    }
}
