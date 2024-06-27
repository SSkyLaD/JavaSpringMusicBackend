package org.example.dbconnectdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

@Data
@AllArgsConstructor
public class ResponseData {
    private String msg;
    private Optional<Object> data;

    public ResponseData(String msg) {
        this.msg = msg;
        this.data = Optional.empty();
    }

    public ResponseData(String msg, Object data){
        this.msg = msg;
        this.data = Optional.ofNullable(data);
    }
}
