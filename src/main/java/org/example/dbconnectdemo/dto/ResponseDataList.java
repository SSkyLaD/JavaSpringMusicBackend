package org.example.dbconnectdemo.dto;

import lombok.Data;

@Data
public class ResponseDataList{
    private String msg;
    private int dataSize;
    private Object data;

    public ResponseDataList(String msg , int dataSize, Object data) {
        this.msg = msg;
        this.dataSize = dataSize;
        this.data = data;
    }
}
