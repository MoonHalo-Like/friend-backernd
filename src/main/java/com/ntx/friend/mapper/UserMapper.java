package com.ntx.friend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ntx.friend.model.domain.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 *
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.* FROM user u JOIN user_team ut ON u.id = ut.userId JOIN team t ON t.id = ut.teamId WHERE t.id = #{id} AND ut.isDelete = 0")
    List<User> selectByTeamUserId(Long id);
}


