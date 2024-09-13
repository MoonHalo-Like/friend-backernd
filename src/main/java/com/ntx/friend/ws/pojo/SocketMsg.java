package com.ntx.friend.ws.pojo;

import lombok.Data;

/**
 * @ClassName SocketMsg
 * @Author ntx
 * @Description 消息接收实体类
 * @Date 2024/8/7 9:55
 */
//todo 聊天功能实现
@Data
public class SocketMsg {
    /**
     * 聊天类型 1：群聊 2：私聊
     */
    private int type;
    /**
     * 发送者用户id
     */
    private Long sendUserId;
    /**
     * 接收者用户id
     */
    private Long receiveUserId;
    /**
     * 消息
     */
    private String msg;
}
