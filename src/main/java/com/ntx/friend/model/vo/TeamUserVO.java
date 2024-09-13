package com.ntx.friend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName TeamUserVO
 * @Author ntx
 * @Description 队伍和用户信息封装类（脱敏）
 * @Date 2024/7/22 14:12
 */
@Data
public class TeamUserVO {


    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     *  队员人数
     */
    private Integer joinUserNum;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 队员列表
     */
    List<UserVO> userVOList;
}
