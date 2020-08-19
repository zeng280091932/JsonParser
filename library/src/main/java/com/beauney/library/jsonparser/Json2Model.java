package com.beauney.library.jsonparser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zengjiantao
 * @since 2020-08-19
 */
class Json2Model {
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
    static Object json2Model(String json, Class clazz) {
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
                    List<Field> fields = FieldsUtil.getAllFields(clazz, null);
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
                    fieldValue = json2Model(jsonValue, fieldClass);
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
                    list.add(json2Model(jsonValue, clazz));
                    break;
                case JSON_ERROR:
                    break;
            }
        }
        return list;
    }
}
