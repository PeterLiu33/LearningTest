package com.peterliu.lt.java.lambda;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by liujun on 2018/4/18.
 */
@Data
@AllArgsConstructor
public class Apple {
    private String name;
    private String color;

    @Override
    public String toString(){
        return name + ":" + color;
    }
}
