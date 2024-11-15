package com.jflow.test;

import com.jflow.agent.Agent;

public class DemoMain {
    public static void main(String[] args) throws InterruptedException {
        System.out.flush();
        Agent a;
        DemoMain main = new DemoMain();
        main.UHOHFUNCTION();
    }

    public void UHOHFUNCTION(){
        System.out.println("visited UHOHFUNC");
    }

    public void function(int a, int b){
        // COOL LOGGING CODE INJECTED HERE
        a += b;
        b -= a;
        a -= b;
        System.out.println(b * a);
    }
}
