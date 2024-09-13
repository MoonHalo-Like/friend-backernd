package com.ntx.friend.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName QiNiuOSSProperties
 * @Author ntx
 * @Description 七牛云文件读取
 * @Date 2024/7/31 11:01
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss.qiniu")
public class QiNiuOSSProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
