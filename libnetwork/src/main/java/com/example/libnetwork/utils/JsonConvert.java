package com.example.libnetwork.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

/*
* 皮皮虾交互返回结构体    Response {
*                       "status":200
*                       "message":成功
*                       "data":{
*                           // 根据不同的接口返回JsonObject或者JsonArray
*                           "data":{ }
*                           // "data":[],
*                   }
*               }
* */
public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null){
            Object data1 = data.get("data");
            JSON.parseObject(data1.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null){
            Object data1 = data.get("data");
            JSON.parseObject(data1.toString(), clazz);
        }
        return null;
    }
}
