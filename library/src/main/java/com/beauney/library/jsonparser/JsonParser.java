package com.beauney.library.jsonparser;


/**
 * @author zengjiantao
 * @since 2020-08-18
 */
public class JsonParser {

    /**
     * 暴露API  给调用层调用,json字符串转model
     *
     * @param json
     * @param clazz
     * @return
     */
    public static Object parseObject(String json, Class clazz) {
        return Json2Model.json2Model(json, clazz);
    }


    /**
     * model转json字符串
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return Model2Json.model2Json(object);
    }
}
