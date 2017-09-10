package com.consumer;

import java.util.logging.Logger;

public class Consumer {

    private final static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

    public static void main(String[] args) {
    
        Consumer c = new Consumer();
        c.doSomething();
        LOGGER.warning("In constructor");
    }
    
    public void doSomething(){
        System.out.println("consumer in doSomething()");
    }

    
}