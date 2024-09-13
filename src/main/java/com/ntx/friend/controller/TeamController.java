package com.ntx.friend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ntx.friend.common.BaseResponse;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.common.ResultUtils;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.model.domain.Team;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.domain.UserTeam;
import com.ntx.friend.model.dto.TeamQuery;
import com.ntx.friend.model.request.*;
import com.ntx.friend.model.vo.TeamUserVO;
import com.ntx.friend.service.TeamService;
import com.ntx.friend.service.UserService;
import com.ntx.friend.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TeamController
 * @Author ntx
 * @Description 队伍控制器
 * @Date 2024/7/20 14:35
 */
@RestController
@RequestMapping("/team")
@Slf4j
@Api(tags = "队伍接口")
public class TeamController {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;
    @Autowired
    private UserTeamService userTeamService;


    @PostMapping("/add")
    @ApiOperation("新增队伍")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team, loginUser);

        return ResultUtils.success(teamId);
    }




    @PostMapping("/update")
    @ApiOperation("修改队伍")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean update = teamService.updateTeam(teamUpdateRequest,loginUser);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @ApiOperation("查询")
    public BaseResponse<Team> getTeamById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    @ApiOperation("查询全部队伍")
    public BaseResponse<List<TeamUserVO>> listTeam(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> list = teamService.listTeams(teamQuery,isAdmin);
        return ResultUtils.success(list);
    }


    //todo 分页查询
    @GetMapping("/list/page")
    @ApiOperation("分页查询队伍")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> teamPage = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = teamService.page(teamPage, queryWrapper);
        return ResultUtils.success(page);
    }

    @PostMapping("/join")
    @ApiOperation("加入队伍")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }


    @PostMapping("/quit")
    @ApiOperation("退出队伍")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    @ApiOperation("删除队伍")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (teamDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean remove = teamService.deleteTeam(teamDeleteRequest,loginUser);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }



    @GetMapping("/list/myCreateTeam")
    @ApiOperation("获取我创建的队伍")
    public BaseResponse<List<TeamUserVO>> getMyCreateTeam(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> list = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(list);
    }


    @GetMapping("/list/myJoinTeam")
    @ApiOperation("获取我加入的队伍")
    public BaseResponse<List<TeamUserVO>> getMyJoinTeam(TeamQuery teamQuery,HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId,loginUser.getId());
        List<UserTeam> userTeams = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> ids = new ArrayList<>(listMap.keySet());
        teamQuery.setIds(ids);
        List<TeamUserVO> list = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(list);
    }

}
