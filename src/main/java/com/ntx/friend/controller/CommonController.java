package com.ntx.friend.controller;

import com.ntx.friend.common.BaseResponse;
import com.ntx.friend.common.ErrorCode;
import com.ntx.friend.common.ResultUtils;
import com.ntx.friend.model.vo.FileVO;
import com.ntx.friend.utils.QiNiuOSSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName CommonController
 * @Author ntx
 * @Description 文件接口
 * @Date 2024/7/31 11:26
 */
@RestController
@RequestMapping("/common")
@Slf4j
@Api(tags = "文件接口")
public class CommonController {
    @Autowired
    private QiNiuOSSUtils qiNiuOSSUtils;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public BaseResponse<FileVO> upload(MultipartFile file) {
        log.info("文件：{}",file);
        FileVO fileVO = new FileVO();
        try {
            String url = qiNiuOSSUtils.upload(file);
            fileVO.setUrl(url);
            return ResultUtils.success(fileVO);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }
        return ResultUtils.error(ErrorCode.UPLOAD_FAILED);
    }
}
