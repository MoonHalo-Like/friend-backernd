package com.ntx.friend.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

/**
 * @ClassName RedissonTest
 * @Author ntx
 * @Description redisson测试类
 * @Date 2024/7/19 15:47
 */
@SpringBootTest
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;

    @Test
    void test() {
        ArrayList<String> list = new ArrayList<>();
        list.add("nihao");
        System.out.println("list: " + list.get(0));
        list.remove(0);

        RList<String> rList = redissonClient.getList("test-list");

        rList.add("nihao");
        System.out.println("rList: " + rList.get(0));
        rList.remove(0);
    }

}
