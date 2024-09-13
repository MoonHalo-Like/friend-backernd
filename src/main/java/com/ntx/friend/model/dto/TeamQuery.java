package com.ntx.friend.model.dto;

import com.ntx.friend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @ClassName TeamQuery
 * @Author ntx
 * @Description 队伍查询封装类
 * @Date 2024/7/21 14:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * ids
     */
    private List<Long> ids;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开，1-私有，2-加密
     */
    private Integer status;


}
