package io.github.timemachinelab.reflectx.util;

import io.github.timemachinelab.reflectx.exception.ReflectionException;

import java.util.Locale;

/**
 * 描述: 属性命名工具类
 * <p>负责解析方法名并提取属性名
 * @author suifeng
 * 日期: 2026/1/26
 */
public final class PropertyNamer {

    private PropertyNamer() {
    }

    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new ReflectionException("Error parsing property name '" + name + "'. Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.isEmpty()) {
            return "";
        }

        // Java Bean 规范特殊处理：
        // 1. 如果只有一个字母，转小写。
        // 2. 如果大于1个字母，且第2个字母是小写，首字母转小写。
        // 3. 如果大于1个字母，且第2个字母是大写，保持原样（如 getURL -> URL）。
        if (name.length() == 1 || !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

    public static boolean isProperty(String name) {
        return isGetter(name) || isSetter(name);
    }

    public static boolean isGetter(String name) {
        return (name.startsWith("get") && name.length() > 3) || (name.startsWith("is") && name.length() > 2);
    }

    public static boolean isSetter(String name) {
        return name.startsWith("set") && name.length() > 3;
    }
}