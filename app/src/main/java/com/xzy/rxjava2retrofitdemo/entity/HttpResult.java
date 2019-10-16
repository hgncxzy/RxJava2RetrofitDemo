package com.xzy.rxjava2retrofitdemo.entity;

/**
 * 接口返回数据封装。
 */
public class HttpResult<T> {

    private String msg;
    private int code;
    //用来模仿Data
    private T subjects;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getSubjects() {
        return subjects;
    }

    public void setSubjects(T subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", subjects=" + subjects +
                '}';
    }
}
