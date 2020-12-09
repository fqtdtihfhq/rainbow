package com.tests.beans;

import java.io.Serializable;

/**
 * @author Mr.赵
 * created on 2020/12/5
 */
public class ReturnData implements Serializable {
    private String name;

    public ReturnData() {
    }

    public ReturnData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ReturnData{" +
                "name='" + name + '\'' +
                '}';
    }
}
