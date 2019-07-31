package com.tfit.BdBiProcSrvYgwcSchOmc.service.hdfs.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.BaseTests;
import com.tfit.BdBiProcSrvYgwcSchOmc.client.HdfsRWClient;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.hdfs.HdfsRWService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.HdfsRWUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @Descritpion：
 * @author: yanzhao_xu
 * @date: 2019/1/14 11:17
 */
public class HdfsRWServiceImplTest extends BaseTests {

    @Autowired
    private HdfsRWService hdfsRWService;

    @Test
    public void readFile() throws IOException {

        String result = hdfsRWService.readFile("/18-07-10/00/");

        assertNotNull(result);
    }

    @Test
    public void getHdfsFileList() throws IOException {
        //String hdfs = HdfsRWUtil.HDFS_DOMAIN + "/18-07-10/00/";
        String hdfs = "/18-07-10/00/";
        String result;

        boolean flag = HdfsRWClient.isExistHdfs(hdfs);
        if (flag) {
            HdfsRWClient.ReadFile(hdfs);
            result = hdfsRWService.readFile(hdfs);
        } else {
            boolean downloaded = HdfsRWClient.hdfsFileDownload(hdfs, "d:/18-07-10/00/");
            if(downloaded){
                HdfsRWClient.ReadFile(hdfs);
                result = hdfsRWService.readFile(hdfs);
            }
        }


    }
}