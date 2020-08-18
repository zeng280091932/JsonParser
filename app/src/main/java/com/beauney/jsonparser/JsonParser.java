package com.beauney.jsonparser;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-08-18
 */
public class JsonParser {
    public static String toJson(Object object) {
        StringBuffer jsonBuffer = new StringBuffer();
        if (object instanceof List<?>) {
            addListToBuffer(jsonBuffer, object);
        } else {
            addObjectToJson(jsonBuffer, object);
        }
        return jsonBuffer.toString();
    }

    /**
     * 解析单独的JSONObject类型
     * 递归准备
     *
     * @param jsonBuffer
     * @param object
     */
    private static void addObjectToJson(StringBuffer jsonBuffer, Object object) {
        jsonBuffer.append("{");

        List<Field> fields = new ArrayList<>();
        getAllFields(object.getClass(), fields);
        for (int i = 0; i < fields.size(); i++) {
            Method method = null;
            Field field = fields.get(i);
            Object fieldVale = null;

            String fieldName = field.getName();
            String methodName = "get" + ((char) (fieldName.charAt(0) - 32) + fieldName.substring(1));

            try {
                method = object.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {
                //没有找到已get开头的方法，考虑boolean类型时是以is开头
                if (!fieldName.startsWith("is")) {
                    methodName = "is" + ((char) (fieldName.charAt(0) - 32) + fieldName.substring(1));
                } else {
                    methodName = fieldName;
                }

                try {
                    method = object.getClass().getMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    //跳过该字段
                    continue;
                }
            }

            try {
                fieldVale = method.invoke(object);
            } catch (Exception e) {
                continue;
            }

            if (fieldVale != null) {
                jsonBuffer.append("\"");
                jsonBuffer.append(fieldName);
                jsonBuffer.append("\":");

                if (fieldVale instanceof Integer
                        || fieldVale instanceof Long
                        || fieldVale instanceof Float
                        || fieldVale instanceof Double
                        || fieldVale instanceof Boolean) {
                    jsonBuffer.append(fieldVale);
                } else if (fieldVale instanceof String) {
                    jsonBuffer.append("\"");
                    jsonBuffer.append(fieldVale);
                    jsonBuffer.append("\"");
                } else if (fieldVale instanceof List<?>) {
                    addListToBuffer(jsonBuffer, fieldVale);
                } else if (fieldVale instanceof Map) {
                    //TODO 暂不实现
                } else {
                    addObjectToJson(jsonBuffer, fieldVale);
                }
                if (i < fields.size() - 1) {
                    jsonBuffer.append(",");
                }
            }
        }
        if (jsonBuffer.charAt(jsonBuffer.length() - 1) == ',') {
            //删除最后一个逗号
            jsonBuffer.deleteCharAt(jsonBuffer.length() - 1);
        }
        jsonBuffer.append("}");
    }

    private static void addListToBuffer(StringBuffer jsonBuffer, Object fieldVale) {
        List<?> list = (List<?>) fieldVale;
        jsonBuffer.append("[");
        for (int i = 0; i < list.size(); i++) {
            addObjectToJson(jsonBuffer, list.get(i));
            if (i < list.size() - 1) {
                jsonBuffer.append(",");
            }
        }
        jsonBuffer.append("]");
    }

    /**
     * 获取当前Class  所有的成员变量 Field
     * 父类的Class  成员变量
     * Object 类型
     * final 修饰的成员变量
     * 递归方法
     *
     * @param aClass
     * @param fields
     */
    private static void getAllFields(Class<?> aClass, List<Field> fields) {
        if (fields == null) {
            fields = new ArrayList<>();
        }

        //排除Object类型
        if (aClass.getSuperclass() != null) {
            Field[] fieldsSelf = aClass.getDeclaredFields();
            for (Field field : fieldsSelf) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    fields.add(field);
                }
            }
            getAllFields(aClass.getSuperclass(), fields);
        }
    }

}
