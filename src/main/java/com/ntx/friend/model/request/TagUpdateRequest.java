package com.ntx.friend.model.request;

import lombok.Data;

/**
 * @ClassName TagUpdateRequest
 * @Author ntx
 * @Description 标签修改请求类
 * @Date 2024/8/2 10:30
 */
@Data
public class TagUpdateRequest {
    private  Long id;
    private String tagName;
    private Long parentId;
}
