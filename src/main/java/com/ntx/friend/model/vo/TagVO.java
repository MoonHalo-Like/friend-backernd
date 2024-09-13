package com.ntx.friend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName TagVO
 * @Author ntx
 * @Description 标签返回类
 * @Date 2024/7/31 16:54
 */
@Data
public class TagVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String text;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 子标签
     */
    private List<TagVO> children;
}
