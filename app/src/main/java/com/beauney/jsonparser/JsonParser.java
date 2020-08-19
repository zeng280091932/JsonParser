package com.beauney.jsonparser;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zengjiantao
 * @since 2020-08-18
 */
public class JsonParser {

    private static final int JSON_ARRAY = 1;

    private static final int JSON_OBJECT = 2;

    private static final int JSON_ERROR = 3;

    /**
     * 暴露API  给调用层调用,json字符串转model
     *
     * @param json
     * @param clazz
     * @return
     */
    public static Object parseObject(String json, Class clazz) {
        Object object = null;
        if (json.charAt(0) == '[') {
            try {
                object = toList(json, clazz);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (json.charAt(0) == '{') {
            try {
                JSONObject jsonObject = new JSONObject(json);
                object = clazz.newInstance();

                Iterator<?> iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    Object fieldValue = null;
                    //得到当前clazz类型的所有成员变量
                    List<Field> fields = getAllFields(clazz, null);
                    for (Field field : fields) {
                        if (field.getName().equalsIgnoreCase(key)) {
                            field.setAccessible(true);
                            //得到 key所对应的值   值 可以基本类型  类类型
                            fieldValue = getFieldValue(field, jsonObject, key);
                            if (fieldValue != null) {
                                field.set(object, fieldValue);
                            }
                            field.setAccessible(false);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 得到当前的value值
     *
     * @param field
     * @param jsonObject
     * @param key
     * @return
     */
    private static Object getFieldValue(Field field, JSONObject jsonObject, String key) throws JSONException {
        Object fieldValue = null;
        Class<?> fieldClass = field.getType();
        if (fieldClass.getSimpleName().equals("int")
                || fieldClass.getSimpleName().equals("Integer")) {
            fieldValue = jsonObject.getInt(key);
        } else if (fieldClass.getSimpleName().equals("double")
                || fieldClass.getSimpleName().equals("Double")) {
            fieldValue = jsonObject.getDouble(key);
        } else if (fieldClass.getSimpleName().equals("long")
                || fieldClass.getSimpleName().equals("Long")) {
            fieldValue = jsonObject.getLong(key);
        } else if (fieldClass.getSimpleName().equals("boolean")
                || fieldClass.getSimpleName().equals("Boolean")) {
            fieldValue = jsonObject.getBoolean(key);
        } else if (fieldClass.getSimpleName().equals("String")) {
            fieldValue = jsonObject.getString(key);
        } else {
            //判断集合类型 和对象类型 jsonValue 代表完整的json字符串  里面一层
            String jsonValue = jsonObject.getString(key);
            switch (getJsonType(jsonValue)) {
                case JSON_ARRAY:
                    Type fieldType = field.getGenericType();
                    if (fieldType instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) fieldType;
                        Type[] types = parameterizedType.getActualTypeArguments();
                        for (Type type : types) {
                            Class<?> fieldArgClass = (Class<?>) type;
                            fieldValue = toList(jsonValue, fieldArgClass);
                        }
                    }
                    break;
                case JSON_OBJECT:
                    fieldValue = parseObject(jsonValue, fieldClass);
                    break;
                case JSON_ERROR:
                    break;
            }
        }
        return fieldValue;
    }

    /**
     * 获取当前json字符串的类型
     *
     * @param jsonValue
     * @return
     */
    private static int getJsonType(String jsonValue) {
        char firstChar = jsonValue.charAt(0);
        if (firstChar == '{') {
            return JSON_OBJECT;
        } else if (firstChar == '[') {
            return JSON_ARRAY;
        } else {
            return JSON_ERROR;
        }
    }

    /**
     * 解析JsonArray数组
     *
     * @param json
     * @param clazz
     * @return
     */
    private static Object toList(String json, Class clazz) throws JSONException {
        List<Object> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            String jsonValue = jsonArray.getJSONObject(i).toString();
            switch (getJsonType(jsonValue)) {
                case JSON_ARRAY:
                    List<?> infoList = (List<?>) toList(jsonValue, clazz);
                    list.add(infoList);
                    break;
                case JSON_OBJECT:
                    list.add(parseObject(jsonValue, clazz));
                    break;
                case JSON_ERROR:
                    break;
            }
        }
        return list;
    }

    /**
     * =======================================================================================
     */

    /**
     * model转json字符串
     *
     * @param object
     * @return
     */
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
    private static List<Field> getAllFields(Class<?> aClass, List<Field> fields) {
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
        return fields;
    }
}
