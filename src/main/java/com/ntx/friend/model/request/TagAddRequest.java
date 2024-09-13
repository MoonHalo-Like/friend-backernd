package com.ntx.friend.model.request;

import lombok.Data;

/**
 * @ClassName TagAddRequest
 * @Author ntx
 * @Description 标签添加页
 * @Date 2024/7/31 18:57
 */
@Data
public class TagAddRequest {
    /**
     * 标签id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 0：不是 1：父标签
     */
    private Integer isParent;
}
