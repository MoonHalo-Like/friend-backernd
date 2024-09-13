package com.ntx.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ntx.friend.common.BaseResponse;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.common.ResultUtils;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.request.UserLoginRequest;
import com.ntx.friend.model.request.UserRegisterRequest;
import com.ntx.friend.model.vo.UserVO;
import com.ntx.friend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ntx.friend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("用户注销")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    @ApiOperation("获取当前用户")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 查询用户
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    @ApiOperation("查询用户")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 删除用户
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation("删除用户")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 根据标签查询
     *
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    @ApiOperation("根据标签查询")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUsersByTag(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 修改信息
     *
     * @param user
     * @return
     */
    @PostMapping("/update")
    @ApiOperation("修改信息")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //校验权限,触发更新
        Integer res = userService.updateUser(user, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 推荐用户
     *
     * @param request
     * @return
     */
    //todo: 推荐多个，未实现
    @GetMapping("/recommend")
    @ApiOperation("推荐用户")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("friend:user:recommend:%s", loginUser.getId());

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        Page<User> userList = (Page<User>) valueOperations.get(redisKey);
        if (userList != null) {
            return ResultUtils.success(userList);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

        try {
            valueOperations.set(redisKey, userList,60000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userList);
    }

    @GetMapping("/match")
    @ApiOperation("根据标签匹配")
    public BaseResponse<List<UserVO>> matchUsers(Long num,HttpServletRequest request){
        if (num<= 0 || num>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUser(num,loginUser));
    }

    @PostMapping("/sign")
    @ApiOperation("签到")
    public BaseResponse<Boolean> sign(HttpServletRequest request){
        return ResultUtils.success(userService.sign(request));
    }

    @PostMapping("/issign")
    @ApiOperation("是否签到")
    public BaseResponse<Boolean> isSign(HttpServletRequest request){
        return ResultUtils.success(userService.isSign(request));
    }

    @PostMapping("/sign/count")
    @ApiOperation("获取连续签到天数")
    public BaseResponse<Integer> getSignSum(HttpServletRequest request){
        return ResultUtils.success(userService.getSignSum(request));
    }

}
