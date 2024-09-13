package com.ntx.friend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName TeamDeleteRequest
 * @Author ntx
 * @Description 删除队伍
 * @Date 2024/7/21 16:29
 */
@Data
public class TeamDeleteRequest{



    /**
     * TeamId
     */
    private Long teamId;

}
