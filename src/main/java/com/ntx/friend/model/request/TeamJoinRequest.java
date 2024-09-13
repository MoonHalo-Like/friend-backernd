package com.ntx.friend.model.request;

import lombok.Data;

/**
 * @ClassName TeamJoinRequest
 * @Author ntx
 * @Description 加入队伍
 * @Date 2024/7/21 16:29
 */
@Data
public class TeamJoinRequest {


    /**
     * TeamId
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
