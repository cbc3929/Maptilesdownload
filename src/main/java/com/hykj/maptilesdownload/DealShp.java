package com.hykj.maptilesdownload;

import com.hykj.maptilesdownload.gts4vect.Geotools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @BelongProject:Maptilesdownload
 * @BelongPackage:com.hykj.maptilesdownload
 * @Author:Administrator
 * @CreateTime:2021-11-23-12-10
 * @Description:cc
 */
public class DealShp {
    private static BufferedInputStream inputStream;

    public static void shpimpg() throws IOException {
        Map map = GlobalConfig.globalConfig();
        Geotools geotools = new Geotools();
        geotools.shp2pgtable(map.get("ShpPath").toString(), "acctty");
    }
}
