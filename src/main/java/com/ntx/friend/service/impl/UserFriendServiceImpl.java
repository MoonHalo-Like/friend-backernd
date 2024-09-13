package com.ntx.friend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ntx.friend.mapper.UserFriendMapper;
import com.ntx.friend.model.domain.UserFriend;
import com.ntx.friend.service.UserFriendService;
import org.springframework.stereotype.Service;

/**
* @author n1072
* @description 针对表【user_friend(好友关系表)】的数据库操作Service实现
* @createDate 2024-08-13 14:27:25
*/
@Service
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend>
    implements UserFriendService {

}




