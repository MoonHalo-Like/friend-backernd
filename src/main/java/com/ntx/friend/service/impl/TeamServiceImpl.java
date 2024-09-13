package com.ntx.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.mapper.TeamMapper;
import com.ntx.friend.model.domain.Team;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.domain.UserTeam;
import com.ntx.friend.model.dto.TeamQuery;
import com.ntx.friend.model.enums.TeamStatusEnum;
import com.ntx.friend.model.request.TeamDeleteRequest;
import com.ntx.friend.model.request.TeamJoinRequest;
import com.ntx.friend.model.request.TeamQuitRequest;
import com.ntx.friend.model.request.TeamUpdateRequest;
import com.ntx.friend.model.vo.TeamUserVO;
import com.ntx.friend.model.vo.UserVO;
import com.ntx.friend.service.TeamService;
import com.ntx.friend.service.UserService;
import com.ntx.friend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author n1072
 * @description 针对表【team(队伍表)】的数据库操作Service实现
 * @createDate 2024-07-20 14:24:28
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private UserService userService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //检验参数
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //检验信息
        //队伍人数>1且<20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //队伍标题<=20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不符合要求");
        }
        //描述<=512
        String teamDescription = team.getDescription();
        if (StringUtils.isNotBlank(teamDescription) && teamDescription.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //是否公开
        int teamStatus = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //如果是加密，必须有密码，且密码<=20
        String teamPassword = team.getPassword();
        if (statusEnum.equals(TeamStatusEnum.SECRET) && (StringUtils.isBlank(teamPassword) && teamPassword.length() >= 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        //过期时间>当前时间
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间<当前时间");
        }
        //当前用户最多创建5个队伍 TODO 连续点击可能创建多个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        long createTeamNum = this.count(queryWrapper);
        if (createTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户创建队伍已达上线");
        }
        //插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean result = this.save(team);
        if (!result || team.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //插入用户到队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(LocalDateTime.now());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        if (teamQuery != null) {
            queryWrapper
                    //根据id查找
                    .eq(teamQuery.getId() != null && teamQuery.getId() > 0, Team::getId, teamQuery.getId())
                    //根据ids查找
                    .in(!CollectionUtils.isEmpty(teamQuery.getIds()),Team::getId, teamQuery.getIds())
                    //队伍名称查找
                    .like(StringUtils.isNotBlank(teamQuery.getName()), Team::getName, teamQuery.getName())
                    //队伍描述查找
                    .like(StringUtils.isNotBlank(teamQuery.getDescription()), Team::getDescription, teamQuery.getDescription())
                    //最大人数查找
                    .eq(teamQuery.getMaxNum() != null && teamQuery.getMaxNum() > 0, Team::getMaxNum, teamQuery.getMaxNum())
                    //根据创建人查找
                    .eq(teamQuery.getUserId() != null && teamQuery.getUserId() > 0, Team::getUserId, teamQuery.getUserId());
            //根据状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null && !isAdmin) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (statusEnum != null){
                queryWrapper.eq(Team::getStatus, statusEnum.getValue());
            }
        }
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //如果不是管理员和自己本人,则排除已过期的队伍
        if (!isAdmin){
            teamList = teamList.stream().filter(team -> {
                return team.getExpireTime().isAfter(LocalDateTime.now());
            }).collect(Collectors.toList());
        }
        //封装返回信息
        ArrayList<TeamUserVO> teamUserVOS = new ArrayList<>();
        for (Team team : teamList) {
            List<User> users = userService.selectByTeamUserId(team.getId());
            if (CollectionUtils.isEmpty(users)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            List<UserVO> userVOS = users.stream().map(user -> {
                User safetyUser = userService.getSafetyUser(user);
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(safetyUser, userVO);
                return userVO;
            }).collect(Collectors.toList());
            //获取队员人数
            Integer joinUserNum = userVOS.size();
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            teamUserVO.setUserVOList(userVOS);
            teamUserVO.setJoinUserNum(joinUserNum);
            teamUserVOS.add(teamUserVO);
        }
        return teamUserVOS;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword()) && StringUtils.isBlank(oldTeam.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码");
            }
        }
        //拷贝
        TeamUpdateRequest newTeamUpdateRequest = new TeamUpdateRequest();
        BeanUtils.copyProperties(oldTeam, newTeamUpdateRequest);
        //判读数据是否相等
        if (!teamUpdateRequest.equals(newTeamUpdateRequest)) {
            Team team = new Team();
            BeanUtils.copyProperties(teamUpdateRequest, team);
            return this.updateById(team);
        }
        return false;
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        LocalDateTime expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //判断用户加入和创建队伍的个数
        Long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId, userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if (hasJoinNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
        }
        //判读是否已加入队伍
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        queryWrapper.eq(UserTeam::getUserId, userId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入队伍");
        }

        //判读是否已满
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已达上线");
        }
        //更新关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //判断是否是加入
        Long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId, userId);
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        long isJoin = userTeamService.count(queryWrapper);
        if (isJoin == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入该队伍");
        }
        //获取队员数
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        //队伍只剩一人
        if (teamHasJoinNum == 1) {
            //解散队伍
            this.removeById(teamId);
            return userTeamService.remove(queryWrapper);
        } else {
            //是否是队长
            if (!Objects.equals(team.getUserId(), userId)) {
                queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserTeam::getTeamId, teamId);
                queryWrapper.eq(UserTeam::getUserId, userId);
                return userTeamService.remove(queryWrapper);
            }
            //是队长
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getTeamId, teamId);
            queryWrapper.last("order by id asc limit 2");
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            Long nextTeamUserId = userTeamList.get(1).getUserId();
            Team updateTeam = new Team();
            updateTeam.setId(teamId);
            updateTeam.setUserId(nextTeamUserId);
            boolean updateSuccess = this.updateById(updateTeam);
            if (!updateSuccess){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新队长失败");
            }
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getTeamId, teamId);
            queryWrapper.eq(UserTeam::getUserId, userId);
            boolean remove = userTeamService.remove(queryWrapper);
            if (!remove){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"退出失败");
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser) {
        if (teamDeleteRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamDeleteRequest.getTeamId();
        Team team = this.getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        if (!Objects.equals(team.getUserId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean rUserTeam = userTeamService.remove(queryWrapper);
        if (!rUserTeam){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return this.removeById(teamId);
    }
}




