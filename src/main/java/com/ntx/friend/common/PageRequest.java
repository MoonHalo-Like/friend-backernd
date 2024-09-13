package com.ntx.friend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName PageRequest
 * @Author ntx
 * @Description 分页参数
 * @Date 2024/7/21 14:49
 */
@Data
public class PageRequest  {


    /**
     * 页面行数
     */
    protected long pageSize = 10;
    /**
     * 当前是第几页
     */
    protected long pageNum = 1;

}
