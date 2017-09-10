package com.implementer;
import com.service.MyService;

public class MyServiceImplementer implements MyService{

    public void printMessage(){
        System.out.println("MyServiceImplementer says Hello World");
    }
    public void secretPublicMethod(){
        System.out.println("You found my secret");
    }
}