package com.tong.controller;


import com.tong.result.Result;
import com.tong.pojo.entity.Blog;
import com.tong.service.IBlogService;
import com.tong.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blog")
@Api(tags = "评论相关接口")
@Slf4j
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @PostMapping
    @ApiOperation("发布探店评论")
    public Result saveBlog(@RequestBody Blog blog) {
        log.info("新增博客，blog：{}", blog);
        Long blogId = blogService.saveBlog(blog);
        return Result.ok(blogId);
    }

    @PutMapping("/like/{id}")
    @ApiOperation("点赞探店评论")
    public Result likeBlog(@PathVariable("id") Long id) {
        log.info("点赞博客，id：{}", id);
        blogService.likeBlogById(id);
        return Result.ok();
    }

    @GetMapping("/of/me")
    @ApiOperation("分页查询我的探店评论")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer pageNum) {
        log.info("分页查询我的博客，pageNum：{}", pageNum);
        List<Blog> records = blogService.pageMyBlog(pageNum);
        return Result.ok(records);
    }

    @GetMapping("/hot")
    @ApiOperation("分页查询热点探店评论")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer pageNum) {
        log.info("分页查询热点博客，pageNum：{}", pageNum);
        List<Blog> records = blogService.pageHotBlog(pageNum);
        return Result.ok(records);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询探店评论详情")
    public Result queryBlogById(@PathVariable("id") Long id){
        log.info("根据id查询探店评论详情，id：{}", id);
        Blog blog = blogService.queryBlogById(id);
        return Result.ok(blog);
    }
}
