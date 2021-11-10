package com.hykj.maptilesdownload;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * @BelongProject:gassplatform
 * @BelongPackage:com.xhgis
 * @Author:Administrator
 * @CreateTime:2021-10-28-10-40
 * @Description:cc
 */
public class MyRunnable implements Runnable{
    private String command;
    private Integer x;
    private Integer y;
    private String address = "https://mt1.google.com/vt/lyrs=s&x=a&y=b&z=";
    private Integer z;

    public MyRunnable(Integer x,Integer y,Integer z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void run() {
        downloadtiles();
    }

    public  void downloadtiles() {
        String imgUrl = "https://mt1.google.com/vt/lyrs=s&x=" + x + "&y=" + y + "&z=" + z;
        String filepath = "f:\\ditu\\dazhou\\" + z + "\\" + x + "\\";
        File Dir = new File(filepath);
        InputStream inStream;
        if (!Dir.exists()) {
            Dir.mkdirs();
        }
        File file = new File(filepath + y + ".png");

            try {
                URL url = new URL(imgUrl);
                URLConnection con = url.openConnection();
                inStream = con.getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();//中转站，现将图片数据放到outStream中
                byte[] buf = new byte[1024];
                int len;
                while ((len = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, len);
                }
                inStream.close();
                outStream.close();
                FileOutputStream op = new FileOutputStream(file);//图片下载的位置
                op.write(outStream.toByteArray());
                op.close();
                System.out.println(Thread.currentThread().getName() + "finish 下载成功 :" + imgUrl + "  and  time:" + new Date());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("老子又他妈被封了");
            }



    }


    @Override
    public String toString() {
        return this.command;
    }
}
