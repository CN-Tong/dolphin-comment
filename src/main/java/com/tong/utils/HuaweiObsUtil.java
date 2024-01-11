package com.tong.utils;

import com.obs.services.ObsClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Data
@AllArgsConstructor
public class HuaweiObsUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(String objectName, byte[] bytes) {
        ObsClient client = null;
        try {
            client = new ObsClient(accessKeyId, accessKeySecret, endpoint);
            client.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            log.info("上传文件异常：{}", e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        StringBuilder stringBuilder = new StringBuilder("https://")
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);
        log.info("文件上传到：{}", stringBuilder);
        return stringBuilder.toString();
    }

    public void delete(String objectName){
        ObsClient client = null;
        try {
            client = new ObsClient(accessKeyId, accessKeySecret, endpoint);
            client.deleteObject(bucketName, objectName);
            log.info("删除文件成功");
        } catch (Exception e) {
            log.info("删除文件异常：{}", e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
