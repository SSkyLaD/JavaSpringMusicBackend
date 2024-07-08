package org.example.dbconnectdemo.exception;

public class NotAuthorizeException extends RuntimeException{
    public NotAuthorizeException(){
        super("You not have right to access this endpoint");
    }
}
