package com.hykj.maptilesdownload;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @BelongProject:Maptilesdownload
 * @BelongPackage:com.hykj.maptilesdownload
 * @Author:Administrator
 * @CreateTime:2021-11-18-15-03
 * @Description:cc
 */
@SpringBootTest
public class DownloadTest {
    @Test
    public static void main(String[] args) throws IOException {
        ThreadPoolExecutorDemo.download();
    }
}
