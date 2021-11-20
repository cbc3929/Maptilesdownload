package com.hykj.maptilesdownload.entity;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongProject:Maptilesdownload
 * @BelongPackage:com.hykj.maptilesdownload.entity
 * @Author:Administrator
 * @CreateTime:2021-11-18-12-02
 * @Description:cc
 */
public class Bounds {
    private static final List<String[]> listbounds = new ArrayList<>();

    public List<String[]> getlistbounds() {
        return listbounds;
    }

    public Bounds(String tableName) {
        makelist(tableName);
    }

    public static void makelist(String tableName) {
        List<Entity> entities = new ArrayList<>();
        try {
            entities = Db.use("group_dbdyneetect").query("select st_astext(geom) as aaa  from hash." + tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Entity entity : entities) {
            String coordinates = entity.getStr("aaa").replace("POLYGON", "").replace("(", "").replace(")", "");
            String[] b = coordinates.split(",");
            listbounds.add(b);
        }
    }

}
