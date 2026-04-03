package com.scsdky.common.core.domain;

import java.io.Serializable;
import java.util.*;

import com.scsdky.common.constant.HttpStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("统一返回")
public class AjaxResult<T> implements Serializable {
    private static final long serialVersionUID = -2740653620215944417L;

    /**
     * 状态码
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象
     */
    public static final String DATA_TAG = "data";

    /**
     * 状态码，告诉swagger/knife4j返回对象有一个code属性
     */
    @ApiModelProperty("状态码")
    private int code;

    /**
     * 返回内容，告诉swagger/knife4j返回对象有一个msg属性
     */
    @ApiModelProperty("返回内容")
    private String msg;

    /**
     * 数据对象，告诉swagger/knife4j返回对象有一个泛型数据对象属性
     */
    @ApiModelProperty("数据对象")
    public T data;

    /**
     * 私有化唯一的构造器，是对象创建只能由create进行
     */
    public AjaxResult() {
    }

    /**
     * 唯一创建对象实例的方法，实际创建的是子类对象AjaxResultImpl
     *
     * @param code 状态码
     * @param msg  返回提示消息
     * @param data 返回是数据
     * @param <T>  数据类型
     * @return 返回AjaxResult，但实际类型为AjaxResult的子类AjaxResultImpl
     */
    public static <T> AjaxResult<T> create(int code, String msg, T data) {
        AjaxResultImpl<T> result = new AjaxResultImpl<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    /**
     * 成功
     *
     * @return r
     */
    public static AjaxResult<Void> success() {
        return AjaxResult.successMsg("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息，并携带泛型类型，便于swagger预览返回对象结构
     */
    public static <T> AjaxResult<T> successRT(T data) {
        return success(data);
    }

    public static <T> AjaxResult<T> success(T data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult<Void> successMsg(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult<T> success(String msg, T data) {
        return AjaxResult.create(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return r
     */
    public static AjaxResult<Void> error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static AjaxResult<Void> error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> AjaxResult<T> error(String msg, T data) {
        return AjaxResult.create(HttpStatus.ERROR, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static AjaxResult<Void> error(int code, String msg) {
        return AjaxResult.create(code, msg, null);
    }

    /**
     * 方便链式调用
     *
     * @param key   键
     * @param value 值
     * @return 数据对象
     */
    public AjaxResult<T> put(String key, Object value) {
        return this;
    }

    public static class AjaxResultImpl<T> extends AjaxResult<T> implements Map<String, Object> {
        private static final long serialVersionUID = -6041138310168295064L;

        /**
         * 定义实际保存数据的容器
         */
        private final Map<String, Object> dataMap;

        /**
         * 创建子类对象时创建数据容器
         */
        public AjaxResultImpl() {
            dataMap = new HashMap<>();
        }

        /**
         * 重写getCode，从数据容器中获取code
         *
         * @return r
         */
        @Override
        public int getCode() {
            return (Integer) dataMap.getOrDefault(CODE_TAG, 0);
        }

        /**
         * 重写getMsg，从数据容器中获取msg
         *
         * @return r
         */
        @Override
        public String getMsg() {
            return (String) dataMap.get(MSG_TAG);
        }

        /**
         * 重写getData，从数据容器中获取data
         *
         * @return r
         */
        @Override
        @SuppressWarnings("all")
        public T getData() {
            return (T) dataMap.get(data);
        }

        /**
         * 重写setCode，把code放到数据容器中
         */
        @Override
        public void setCode(int code) {
            dataMap.put(CODE_TAG, code);
        }

        /**
         * 重写setCode，把msg放到数据容器中
         */
        @Override
        public void setMsg(String msg) {
            dataMap.put(MSG_TAG, msg);
        }

        /**
         * 重写setData，把data放到数据容器中
         */
        @Override
        public void setData(T data) {
            dataMap.put(DATA_TAG, data);
        }

        /**
         * 重写put，直接使用数据容器的put方法，并返回this
         *
         * @return 返回AjaxResult类型的AjaxResultImpl实例对象
         */
        @Override
        public AjaxResult<T> put(String key, Object value) {
            dataMap.put(key, value);
            return this;
        }

        /**
         * 重写toString方法，直接使用数据容器的toString方法
         *
         * @return r
         */
        @Override
        public String toString() {
            return dataMap.toString();
        }

        //以下方法为Map接口需要实现的方法，直接使用数据容器的对应方法即可
        @Override
        public int size() {
            return dataMap.size();
        }

        @Override
        public boolean isEmpty() {
            return dataMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return dataMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return dataMap.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return dataMap.get(key);
        }

        @Override
        public Object remove(Object key) {
            return dataMap.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            dataMap.putAll(m);
        }

        @Override
        public void clear() {
            dataMap.clear();
        }

        @Override
        public Set<String> keySet() {
            return dataMap.keySet();
        }

        @Override
        public Collection<Object> values() {
            return dataMap.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return dataMap.entrySet();
        }
    }

}

