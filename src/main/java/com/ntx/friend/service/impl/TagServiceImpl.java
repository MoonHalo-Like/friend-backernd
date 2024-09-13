package com.ntx.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.mapper.TagMapper;
import com.ntx.friend.model.domain.Tag;
import com.ntx.friend.model.domain.User;
import com.ntx.friend.model.request.TagAddRequest;
import com.ntx.friend.model.request.TagUpdateRequest;
import com.ntx.friend.model.vo.TagVO;
import com.ntx.friend.service.TagService;
import com.ntx.friend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author n1072
 * @description 针对表【tag(标签表)】的数据库操作Service实现
 * @createDate 2024-07-14 14:42:22
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {
    @Autowired
    private UserService userService;


    @Override
    public List<TagVO> getTagList() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getIsParent,1);
        //查询父标签
        List<Tag> parentTagList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(parentTagList)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //查询子标签
        List<Long> parentIds = parentTagList.stream().map(Tag::getId).collect(Collectors.toList());
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Tag::getParentId,parentIds);
        Map<Long, List<TagVO>> tagListMap = this.list(queryWrapper).stream().map(tag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVO.setText(tag.getTagName());
            return tagVO;
        }).collect(Collectors.groupingBy(TagVO::getParentId));
        return parentTagList.stream().map(parentTag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(parentTag, tagVO);
            tagVO.setText(parentTag.getTagName());
            tagVO.setChildren(tagListMap.get(tagVO.getId()));
            return tagVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean addTag(TagAddRequest tagAddRequest, HttpServletRequest request) {
        //检验数据
        if (tagAddRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(request);
        //判断是否为管理员
        if (!isAdmin && tagAddRequest.getIsParent() == 1){
            //不是管理员，不能添加父标签
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //添加标签
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddRequest,tag);
        tag.setUserId(loginUser.getId());
        return this.save(tag);
    }

    @Override
    public Boolean deleteTag(Long tagId, HttpServletRequest request) {
        //检验数据
        if (tagId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断是否是管理员
        boolean isAdmin = userService.isAdmin(request);
        if (!isAdmin){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Tag tag = this.getById(tagId);
        if (tag == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer isParent = tag.getIsParent();
        //判断是否为父标签
        if (isParent == 0){
            //子标签
           return this.removeById(tagId);
        }
        //父标签
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getParentId,tagId);
        List<Tag> tagList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(tagList)){
            //无子标签
            return this.removeById(tagId);
        }
        //有子标签
        List<Long> tagIds = tagList.stream().map(Tag::getId).collect(Collectors.toList());
        tagIds.add(tagId);
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Tag::getId,tagIds);
        //删除标签
        return this.remove(queryWrapper);
    }

    @Override
    public Boolean updateTag(TagUpdateRequest tagUpdateRequest,HttpServletRequest request){
        if (tagUpdateRequest==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin=userService.isAdmin(request);
        if (!isAdmin){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Tag tag = this.getById(tagUpdateRequest.getId());
        if (tag==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标签不存在");
        }
        Tag newTag =new Tag();
        BeanUtils.copyProperties(tagUpdateRequest,newTag);
        newTag.setUpdateTime(LocalDateTime.now());
        return this.updateById(newTag);
    }
}




