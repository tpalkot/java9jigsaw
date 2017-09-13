package com.service;

public interface SecretService {

    void message();

    public static SecretService getService(){
        return new com.service.impl.SecretServiceImpl();
    }
}