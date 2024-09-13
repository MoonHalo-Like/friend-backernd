package com.ntx.friend.utils;

import com.google.gson.Gson;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @ClassName QiNiuOSSUtils
 * @Author ntx
 * @Description 七牛云工具类
 * @Date 2024/7/31 11:04
 */
@Data
@AllArgsConstructor
@Slf4j
public class QiNiuOSSUtils {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

    public String upload(MultipartFile file) throws IOException {
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        //获取上传文件的输入流
        InputStream inputStream = file.getInputStream();
        //避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        //开始上传
        Response response = uploadManager.put(inputStream, fileName, upToken, null, null);
        //解析上传成功的结果
        DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
//        System.out.println(putRet.key);
//        System.out.println(putRet.hash);
        //文件访问路径
        String url = endpoint + "/" + fileName;
        return url;
    }
}
