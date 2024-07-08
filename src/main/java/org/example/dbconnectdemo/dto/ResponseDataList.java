package org.example.dbconnectdemo.dto;

import lombok.Data;

@Data
public class ResponseDataList{
    private String msg;
    private int records;
    private Object data;

    public ResponseDataList(String msg , int dataSize, Object data) {
        this.msg = msg;
        this.records = dataSize;
        this.data = data;
    }
}
