package com.tong.controller;


import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.User;
import com.tong.result.Result;
import com.tong.pojo.entity.Blog;
import com.tong.result.ScrollResult;
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

    @GetMapping("/likes/{id}")
    @ApiOperation(("查询点赞排行榜"))
    public Result queryBlogLikes(@PathVariable("id") Long id){
        log.info("查询点赞排行榜，id：{}", id);
        List<UserDTO> userDTOList = blogService.queryBlogLikes(id);
        return Result.ok(userDTOList);
    }

    @GetMapping("/of/user")
    @ApiOperation("分页查询用户的博客")
    public Result pageUserBlogs(@RequestParam("id") Long id,
                                 @RequestParam(value = "current", defaultValue = "1") Integer current){
        log.info("分页查询用户的博客");
        List<Blog> blogList = blogService.pageUserBlogs(id, current);
        return Result.ok(blogList);
    }

    @GetMapping("/of/follow")
    @ApiOperation("滚动分页查询关注的人的笔记")
    public Result pageBlogOfFollow(@RequestParam("lastId") Long lastMinTime,
                                   @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        log.info("滚动分页查询关注的人的笔记，上一次查询的最小时间戳：{}，" +
                "本次查询的偏移量(上次查询最小时间戳的重复次数)：{}", lastMinTime, offset);
        ScrollResult result = blogService.pageBlogOfFollow(lastMinTime, offset);
        return Result.ok(result);
    }
}
