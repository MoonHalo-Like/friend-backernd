package com.ntx.friend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TeamQuitRequest
 * @Author ntx
 * @Description 退出队伍
 * @Date 2024/7/21 16:29
 */
@Data
public class TeamQuitRequest  {



    /**
     * TeamId
     */
    private Long teamId;

}
