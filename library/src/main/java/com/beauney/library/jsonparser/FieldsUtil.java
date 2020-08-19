package com.beauney.library.jsonparser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取当前Class  所有的成员变量 Field
 *
 * @author zengjiantao
 * @since 2020-08-19
 */
class FieldsUtil {
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
    static List<Field> getAllFields(Class<?> aClass, List<Field> fields) {
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
