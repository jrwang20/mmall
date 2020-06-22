package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 按住control + 空格可以检查注解可以添加的属性
 */
/**
 * 该注解@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 * 可以规定当某一字段为空时不出现在序列化后的JSON数据当中
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
/**
 * 通用化响应消息对象
 */
public class ServerResponse<T> implements Serializable {

    private Integer status;
    private String msg;
    private T data;

    private ServerResponse(Integer status) {
        this.status = status;
    }

    private ServerResponse(Integer status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    /**
     * 对于以上两个构造函数，当传入String参数时默认使用的是传给msg的方法、而非data的方法
     */

    private ServerResponse(Integer status, String msg, T data) {
        this.status = status;
        this.data = data;
        this.msg = msg;
    }

    /**
     * 检查当前响应对象是否为成功响应
     * 该字段不应该出现JSON序列化数据当中，因此标注@JsonIgnore
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /**
     * 当响应成功时获取通用响应对象
     */
    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }
    /**
     * 对外暴露两个可以分别传入data参数和msg参数的API，内部分别调用了传递msg和传递data的构造函数
     * 解决了当data泛型类型为String时错误调用msg构造函数的问题
     */
    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }
    public static <T> ServerResponse<T> createBySuccessData(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    /**
     * 当响应失败时获取通用响应对象
     */
    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }
    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
    }
    public static <T> ServerResponse<T> createByErrorCodeMessage(Integer errorCode, String errorMessage) {
        return new ServerResponse<T>(errorCode, errorMessage);
    }

}
