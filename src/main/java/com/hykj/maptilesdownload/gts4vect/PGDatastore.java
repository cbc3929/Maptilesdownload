package com.hykj.maptilesdownload.gts4vect;


import com.hykj.maptilesdownload.GlobalConfig;
import com.sun.istack.internal.NotNull;
import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStoreFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangkang
 * @email iwuang@qq.com
 * @date 2019/1/24 16:28
 */
public class PGDatastore {

    private static DataStore dataStore = null;
    private static Logger logger = Logger.getLogger(PGDatastore.class);
    private static BufferedInputStream inputStream;


    @NotNull
    private String host;
    @NotNull
    private String port;
    @NotNull
    private String dbname;
    @NotNull
    private String schema;
    @NotNull
    private String username;
    @NotNull
    private String password;

    public PGDatastore() {
    }


    /**
     * 只有get或者Create时才会实际连接数据库
     *
     * @param host
     * @param port
     * @param dbname
     * @param schema
     * @param username
     * @param password
     */
    public PGDatastore(String host, String port, String dbname, String schema, String username, String password) {
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.schema = schema;
        this.username = username;
        this.password = password;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        PGDatastore.logger = logger;
    }

    public static DataStore getDefeaultDatastore() throws IOException {
        Map map = GlobalConfig.globalConfig();
        if (dataStore == null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");
            params.put(PostgisNGDataStoreFactory.HOST.key, map.get("host"));
            params.put(PostgisNGDataStoreFactory.PORT.key, map.get("port"));
            params.put(PostgisNGDataStoreFactory.DATABASE.key, map.get("database"));
            params.put(PostgisNGDataStoreFactory.SCHEMA.key, "public");
            params.put(PostgisNGDataStoreFactory.USER.key, map.get("user"));
            params.put(PostgisNGDataStoreFactory.PASSWD.key, map.get("pass"));
            try {
                dataStore = DataStoreFinder.getDataStore(params);
            } catch (IOException e) {
                logger.error("默认Postgis数据库连接失败", e);
            }
        }
        return dataStore;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DataStore getDatastore() throws IOException {
        if (dataStore == null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(JDBCDataStoreFactory.DBTYPE.key, "postgis");
            params.put(JDBCDataStoreFactory.HOST.key, host);
            params.put(JDBCDataStoreFactory.PORT.key, new Integer(port));
            params.put(JDBCDataStoreFactory.DATABASE.key, dbname);
            params.put(JDBCDataStoreFactory.SCHEMA.key, schema);
            params.put(JDBCDataStoreFactory.USER.key, username);
            params.put(JDBCDataStoreFactory.PASSWD.key, password);
            try {
                dataStore = DataStoreFinder.getDataStore(params);
            } catch (IOException e) {
                logger.error("Postgis数据库连接失败", e);
            }
        }
        if (dataStore == null) {
            dataStore = getDefeaultDatastore();
        }
        return dataStore;
    }

    public void dispose() {
        dataStore.dispose();
        dataStore = null;
    }
}
