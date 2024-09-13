package com.ntx.friend.once;

import com.ntx.friend.mapper.UserMapper;
import com.ntx.friend.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @ClassName InsertUsers
 * @Author ntx
 * @Description 批量插入数据
 * @Date 2024/7/18 11:04
 */
//@Component
public class InsertUsers {
    @Autowired
    private UserMapper userMapper;


    /**
     * 批量插入数据
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        final int INSERT_NUM = 1000;
        for (int i = 1; i <= INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("haha"+i);
            user.setUserAccount("kale"+i);
            user.setAvatarUrl("https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg");
            user.setGender(i%2);
            user.setProfile("niha"+i);
            user.setUserPassword("60ce8635fd4d7c39c49b0721f7cf4199");
            user.setPhone("1"+i);
            user.setEmail(i+"@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userMapper.insert(user);
        }
    }
}
