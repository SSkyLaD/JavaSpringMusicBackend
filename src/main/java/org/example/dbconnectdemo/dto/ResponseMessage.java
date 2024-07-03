package org.example.dbconnectdemo.dto;

import lombok.Data;

@Data
public class ResponseMessage {
    private String msg;

    public ResponseMessage(String msg) {
        this.msg = msg;
    }
}
