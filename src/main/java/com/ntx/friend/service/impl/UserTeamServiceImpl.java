package com.ntx.friend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ntx.friend.mapper.UserTeamMapper;
import com.ntx.friend.model.domain.UserTeam;
import com.ntx.friend.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author n1072
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-20 14:24:40
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




