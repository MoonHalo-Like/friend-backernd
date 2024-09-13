package com.ntx.friend.service;


import com.ntx.friend.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @ClassName InsertUsers
 * @Author ntx
 * @Description 批量插入数据
 * @Date 2024/7/18 11:04
 */
@SpringBootTest
public class InsertUsers {
    @Autowired
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(60,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入数据
     */
    @Test
    public void doInsertUsers() {
        StopWatch sw = new StopWatch();
        sw.start();
        final int INSERT_NUM = 2000000;
        List<User> userList = new ArrayList<>();
        for (int i = 1000000; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("haha" + i);
            user.setUserAccount("kale" + i);
            user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
            user.setGender(i % 2);
            user.setProfile("niha" + i);
            user.setUserPassword("60ce8635fd4d7c39c49b0721f7cf4199");
            user.setPhone("1" + i);
            user.setEmail(i + "@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 100000);
        sw.stop();
        System.out.println(sw.getLastTaskTimeMillis());
    }

    /**
     * 并发
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch sw = new StopWatch();
        sw.start();

        int batchSize = 5000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            ArrayList<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("haha" + i);
                user.setUserAccount("kale" + i);
                user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
                user.setGender(i % 2);
                user.setProfile("niha" + i);
                user.setUserPassword("60ce8635fd4d7c39c49b0721f7cf4199");
                user.setPhone("1" + i);
                user.setEmail(i + "@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        sw.stop();
        System.out.println(sw.getLastTaskTimeMillis());
    }
}
