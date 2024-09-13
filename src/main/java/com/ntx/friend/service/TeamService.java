package com.ntx.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ntx.friend.model.domain.Team;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.dto.TeamQuery;
import com.ntx.friend.model.request.TeamDeleteRequest;
import com.ntx.friend.model.request.TeamJoinRequest;
import com.ntx.friend.model.request.TeamQuitRequest;
import com.ntx.friend.model.request.TeamUpdateRequest;
import com.ntx.friend.model.vo.TeamUserVO;

import java.util.List;


/**
* @author n1072
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-07-20 14:24:28
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param teamDeleteRequest
     * @param loginUser
     * @return
     */
    boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, User loginUser);
}
