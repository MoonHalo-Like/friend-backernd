package com.ntx.friend.service.impl;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.contant.UserConstant;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.mapper.UserMapper;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.vo.UserVO;
import com.ntx.friend.service.UserService;
import com.ntx.friend.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ntx.friend.contant.UserConstant.ADMIN_ROLE;
import static com.ntx.friend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "kale";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "含有特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不一致");
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    /**
     * 根据标签查询数据
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTag(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        第一种
//        for (String tagName : tagNameList) {
//           wrapper = wrapper.like(User::getTags, tagName);
//        }
//        List<User> userList = userMapper.selectList(wrapper);
//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        //第二种
        //查询所有用户信息
        List<User> userList = userMapper.selectList(wrapper);
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            //获取用户的标签
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            //反序列化
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //管理员，可更新全部信息
        //不是，只能更新自己的信息
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int res = userMapper.updateById(user);
        if (res <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新失败");
        }
        return res;
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> selectByTeamUserId(Long id) {
        return id == null ? null : userMapper.selectByTeamUserId(id);
    }

    @Override
    public List<UserVO> matchUser(Long num, User loginUser) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getId, User::getTags);
        queryWrapper.isNotNull(User::getTags);
        //查询所有用户
        List<User> userList = this.list(queryWrapper);
        //获取当前用户标签
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        //json转list
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //用户 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //标签为空或当前用户为自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            //json转list
            List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagList, userTagsList);
            list.add(new Pair<>(user, distance));
        }
        //按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        for (Pair<User, Long> pair : topUserPairList) {
            System.out.println(pair.getKey().getId() + ": " + pair.getValue());
        }
        //原本顺序的userId列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIdList);
        Map<Long, List<UserVO>> uerIdUserListMap = this.list(queryWrapper)
                .stream()
                .map(user -> {
                    User safetyUser = getSafetyUser(user);
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(safetyUser, userVO);
                    return userVO;
                })
                .collect(Collectors.groupingBy(UserVO::getId));
        List<UserVO> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(uerIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    @Override
    public Boolean sign(HttpServletRequest request) {
        String key = getSignKey(request);
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //获取今天是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        //写入
        return redisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
    }

    @Override
    public Boolean isSign(HttpServletRequest request) {
        String key = getSignKey(request);
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //获取今天是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        //判断是否签到
        return redisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
    }

    @Override
    public Integer getSignSum(HttpServletRequest request) {
        String key = getSignKey(request);
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //获取今天是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        //获取本月到今天为止的签到记录
        List<Long> result = redisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (result == null || result.isEmpty()){
            return 0;
        }
        Long num = result.get(0);
        if (num == null || num == 0){
            return 0;
        }
        //遍历
        int count = 0;
        while (true){
            if ((num & 1)==0){
                break;
            }else {
                count++;
            }
            //右移一位，抛弃最低位
            num >>>= 1;
        }
        return count;
    }

    private String getSignKey(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        return UserConstant.USER_SIGN_KEY + loginUser.getId() + now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
    }

}

