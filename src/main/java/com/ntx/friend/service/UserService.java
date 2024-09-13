package com.ntx.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签查询数据
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTag(List<String> tagNameList);

    /**
     * 修改用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 根据队伍id查询用户数据
     * @param id
     * @return
     */
    List<User> selectByTeamUserId(Long id);

    /**
     * 根据标签匹配
     * @param num
     * @param loginUser
     * @return
     */
    List<UserVO> matchUser(Long num, User loginUser);

    /**
     * 签到
     * @param request
     * @return
     */
    Boolean sign(HttpServletRequest request);

    /**
     * 判断是否签到
     * @param request
     * @return
     */
    Boolean isSign(HttpServletRequest request);

    /**
     * 获取连续签到天数
     * @param request
     * @return
     */
    Integer getSignSum(HttpServletRequest request);
}
