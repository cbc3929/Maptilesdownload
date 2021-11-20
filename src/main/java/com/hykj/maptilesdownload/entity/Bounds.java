package com.hykj.maptilesdownload.entity;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.hykj.maptilesdownload.DealShp;

import java.io.IOException;
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

    public Bounds() throws IOException {
        makelist();
    }

    public static void makelist() throws IOException {
        String tableName = "acctty";
        DealShp.shpimpg();
        try {
            Db.use().execute("create or replace function cc_makeboxgeom(tablename varchar) returns void language plpgsql as\n" +
                    "$$\n" +
                    "declare\n" +
                    "    sql varchar;\n" +
                    "begin\n" +
                    "    sql = 'create table mulidiv_30 as select st_subdivide(the_geom,30) as geom from '||tablename;\n" +
                    "    execute sql;\n" +
                    "    create table boxmulidiv as select geometry(box2d(geom)) as geom  from mulidiv_30;\n" +
                    "    drop table mulidiv_30;\n" +
                    "end;\n" +
                    "$$;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Db.use().execute("select * from cc_makeboxgeom('" + tableName + "')");
        } catch (Exception e) {
            if (e.getMessage().equals("传回预期之外的结果。")) {
                System.out.println("未知bug，不影响");
            } else e.printStackTrace();

        }
        List<Entity> entities = new ArrayList<>();
        try {
            entities = Db.use().query("select st_astext(geom) as aaa  from boxmulidiv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Entity entity : entities) {
            String coordinates = entity.getStr("aaa").replace("POLYGON", "").replace("(", "").replace(")", "");
            String[] b = coordinates.split(",");
            listbounds.add(b);
        }
        try {
            Db.use().execute("drop table boxmulidiv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Db.use().execute("drop function cc_makeboxgeom(tablename varchar)");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Db.use().execute("drop table acctty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
