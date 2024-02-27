package com.tong.controller;

import com.tong.constant.SystemConstants;
import com.tong.result.Result;
import com.tong.utils.HuaweiObsUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
@Api(tags = "上传文件相关接口")
public class UploadController {

    @Autowired
    private HuaweiObsUtil huaweiObsUtil;

    @PostMapping("blog")
    @ApiOperation("上传博客图片")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("上传博客图片：{}", file);
        try {
            // 原始文件名后缀
            String originalFilename = file.getOriginalFilename();
            // UUID重命名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID() + extension;
            // 保存文件
            file.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.info("图片上传成功：{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败", e);
        }
    }

    // public Result uploadImage(@RequestParam("file") MultipartFile file) {
    //     log.info("上传博客图片：{}", file);
    //     try {
    //         // 原始文件名后缀
    //         String originalFilename = file.getOriginalFilename();
    //         // UUID重命名
    //         String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    //         String objectName = UUID.randomUUID() + extension;
    //         String filePath = huaweiObsUtil.upload(objectName, file.getBytes());
    //         return Result.ok(filePath);
    //     } catch (IOException e) {
    //         log.error("图片上传失败：{}", e);
    //     }
    //     return Result.fail("图片上传失败");
    // }

    @GetMapping("/blog/delete")
    @ApiOperation("删除博客图片")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        huaweiObsUtil.delete(filename);
        return Result.ok();
    }
}
