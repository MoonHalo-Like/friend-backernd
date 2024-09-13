package com.ntx.friend.config;

import com.ntx.friend.common.properties.QiNiuOSSProperties;
import com.ntx.friend.utils.QiNiuOSSUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName QiNiuConfig
 * @Author ntx
 * @Description 七牛云配置
 * @Date 2024/7/31 11:02
 */
@Configuration
public class QiNiuConfig {
    @Bean
    @ConditionalOnMissingBean
    public QiNiuOSSUtils qiNiuOSSUtils(QiNiuOSSProperties qiNiuOSSUtils){
        return new QiNiuOSSUtils(qiNiuOSSUtils.getEndpoint(),
                qiNiuOSSUtils.getAccessKey(),
                qiNiuOSSUtils.getSecretKey(),
                qiNiuOSSUtils.getBucket());
    }
}
