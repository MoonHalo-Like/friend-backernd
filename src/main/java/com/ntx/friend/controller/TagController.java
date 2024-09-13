package com.ntx.friend.controller;

import com.ntx.friend.common.BaseResponse;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.common.ResultUtils;
import com.ntx.friend.exception.BusinessException;
import com.ntx.friend.model.request.TagAddRequest;
import com.ntx.friend.model.request.TagUpdateRequest;
import com.ntx.friend.model.vo.TagVO;
import com.ntx.friend.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @ClassName TagController
 * @Author ntx
 * @Description 标签控制器
 * @Date 2024/7/31 16:51
 */
@RestController
@RequestMapping("/tag")
@Api(tags = "标签接口")
public class TagController {
    @Autowired
    private TagService tagService;

    @PostMapping("/list")
    @ApiOperation("获取标签列表")
    public BaseResponse<List<TagVO>> getTagList() {
        List<TagVO> tagVOList = tagService.getTagList();
        return ResultUtils.success(tagVOList);
    }

    @PostMapping("/add")
    @ApiOperation("新增标签列表")
    public BaseResponse<Boolean> addTag(@RequestBody TagAddRequest tagAddRequest, HttpServletRequest request) {
        if (tagAddRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean result = tagService.addTag(tagAddRequest, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/delete")
    @ApiOperation("删除标签")
    public BaseResponse<Boolean> deleteTag(@RequestParam Long tagId, HttpServletRequest request) {
        if (tagId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean res = tagService.deleteTag(tagId, request);
        return ResultUtils.success(res);
    }

    @PostMapping("/update")
    @ApiOperation("修改标签")
    public BaseResponse<Boolean> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest, HttpServletRequest request){
        if (tagUpdateRequest==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean res = tagService.updateTag(tagUpdateRequest,request);
        return ResultUtils.success(res);
    }

}
