package com.jflow.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst){
        System.out.println("");
        inst.addTransformer(new ClassMethodLoggerInjector());
        System.out.println("Agent Done!");
    }
}
