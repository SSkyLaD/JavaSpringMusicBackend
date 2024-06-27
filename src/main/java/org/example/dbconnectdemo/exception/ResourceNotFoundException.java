package org.example.dbconnectdemo.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String exception){
        super(exception);
    }
}
