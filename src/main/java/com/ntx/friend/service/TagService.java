package com.ntx.friend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ntx.friend.model.domain.Tag;
import com.ntx.friend.model.request.TagAddRequest;
import com.ntx.friend.model.request.TagUpdateRequest;
import com.ntx.friend.model.vo.TagVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author n1072
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2024-07-14 14:42:22
*/
public interface TagService extends IService<Tag> {
    /**
     * 获取标签列表
     * @return
     */
    List<TagVO> getTagList();

    /**
     * 添加标签
     * @param tagAddRequest
     * @param request
     * @return
     */
    Boolean addTag(TagAddRequest tagAddRequest, HttpServletRequest request);

    /**
     * 删除标签
     * @param tagId
     * @param request
     * @return
     */
    Boolean deleteTag(Long tagId, HttpServletRequest request);

    /**
     * 修改标签
     * @param tagUpdateRequest
     * @param request
     * @return
     */
    Boolean updateTag(TagUpdateRequest tagUpdateRequest,HttpServletRequest request);
}
