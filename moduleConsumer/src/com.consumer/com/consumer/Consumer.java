package com.consumer;

import java.util.logging.Logger;
import java.util.ServiceLoader;

import com.service.MyService;

public class Consumer {

    private final static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

    public static void main(String[] args) {
    
        Consumer c = new Consumer();
        c.doSomething();
        LOGGER.warning("In constructor");
    }
    
    public void doSomething(){
        System.out.println("consumer in doSomething()");
        
        ServiceLoader<MyService> myServiceLoader = ServiceLoader.load(MyService.class);
        MyService myService = myServiceLoader.iterator().next();
        myService.printMessage();
        //com.implementer.MySeviceImplementer implementer = (com.implementer.MySeviceImplementer)myService;
    }

    
}