package com.ntx.friend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName PreCacheJob
 * @Author ntx
 * @Description 缓存预热
 * @Date 2024/7/18 17:51
 */
@Component
@Slf4j
public class PreCacheJob {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L);

    //每天执行，缓存预热
    @Scheduled(cron = "0 20 16 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("friend:precachejob:docache:lock");
        try {
            if (lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock" + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userList = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("friend:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey, userList, 60000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
