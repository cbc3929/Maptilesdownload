package com.hykj.maptilesdownload;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @BelongProject:Maptilesdownload
 * @BelongPackage:com.hykj.maptilesdownload
 * @Author:Administrator
 * @CreateTime:2021-11-23-14-46
 * @Description:cc
 */
public class GlobalConfig {
    private static BufferedInputStream inputStream;

    public static Map<String, String> globalConfig() throws IOException {

        inputStream = new BufferedInputStream(new FileInputStream("src/main/resources/db.setting"));
        Map<String, String> map = new HashMap();
        Properties props = new Properties();
        props.load(inputStream);
        map.put("url", props.getProperty("url"));
        map.put("database", props.getProperty("database"));
        map.put("host", props.getProperty("host"));
        map.put("port", props.getProperty("port"));
        map.put("user", props.getProperty("user"));
        map.put("pass", props.getProperty("pass"));
        map.put("driver", props.getProperty("driver"));
        map.put("ShpPath", props.getProperty("ShpPath"));
        map.put("outPath", props.getProperty("outPath"));
        map.put("minZoom", props.getProperty("minZoom"));
        map.put("MaxZoom", props.getProperty("MaxZoom"));
        map.put("Process_num", props.getProperty("Process_num"));
        map.put("proxyHost", props.getProperty("proxyHost"));
        map.put("proxyPort", props.getProperty("proxyPort"));
        return map;
    }
}
