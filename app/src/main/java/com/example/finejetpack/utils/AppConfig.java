package com.example.finejetpack.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.finejetpack.model.BottomBar;
import com.example.finejetpack.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/*
* 解析JSON文件
* */
public class AppConfig {

    private static HashMap<String, Destination> sDestConfig;
    private static BottomBar sBottombar;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null){
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>(){}.getType());
        }

        return sDestConfig;
    }

    public static BottomBar getBottombar() {
        if (sBottombar == null){
            String content = parseFile("main_tabs_config.json");
            sBottombar = JSON.parseObject(content, BottomBar.class);
        }
        System.out.println(sBottombar.toString());
        return sBottombar;
    }

    private static String parseFile(String fileName){
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();
        InputStream is = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            is = assets.open(fileName);
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.toString();
    }
}
