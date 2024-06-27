package org.example.dbconnectdemo.exception;

import org.aspectj.bridge.IMessage;

public class UsernameAlreadyExistException extends RuntimeException{
    public UsernameAlreadyExistException(String message){
        super(message);
    }
}
