package io.github.timemachinelab.util;

import com.alibaba.fastjson2.JSON;

/**
 * JSON工具类，基于fastjson2封装
 * 
 * <p>
 * 提供对象与JSON字符串之间的相互转换功能
 * </p>
 * 
 * <pre>
 * JsonUtil.toJson(null)         = "null"
 * JsonUtil.toJson(new User())   = "{\"name\":\"test\",\"age\":20}"
 * JsonUtil.fromJson("{\"name\":\"test\"}", User.class) = User对象
 * </pre>
 * 
 * @author TimeMachineLab
 * @since 1.0
 */
public class JsonUtil {

    /**
     * <p>
     * 将对象转换为JSON字符串
     * </p>
     *
     * <p>
     * 支持Java对象、集合、数组等各种类型的序列化
     * </p>
     *
     * <pre>
     * JsonUtil.toJson(null)         = "null"
     * JsonUtil.toJson("")            = "\"\""
     * JsonUtil.toJson("abc")         = "\"abc\""
     * JsonUtil.toJson(new int[]{1,2,3}) = "[1,2,3]"
     * JsonUtil.toJson(new User("test", 20)) = "{\"name\":\"test\",\"age\":20}"
     * </pre>
     *
     * @param object 要转换的对象，可以为null
     * @return JSON字符串，如果对象为null返回"null"
     * @since 1.0
     */
    public static String toJson(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * <p>
     * 将JSON字符串转换为指定类型的对象
     * </p>
     *
     * <p>
     * 支持Java对象、集合、数组等各种类型的反序列化
     * </p>
     *
     * <pre>
     * JsonUtil.fromJson("null", String.class)              = null
     * JsonUtil.fromJson("\"abc\"", String.class)           = "abc"
     * JsonUtil.fromJson("[1,2,3]", int[].class)           = [1,2,3]
     * JsonUtil.fromJson("{\"name\":\"test\"}", User.class) = User对象
     * </pre>
     *
     * @param json  JSON字符串，可以为null
     * @param clazz 目标类型，不能为null
     * @param <T>   目标类型
     * @return 转换后的对象，如果JSON为null或"null"返回null
     * @throws com.alibaba.fastjson2.JSONException 如果JSON格式错误或无法转换
     * @since 1.0
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
}