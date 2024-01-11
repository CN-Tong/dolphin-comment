package com.tong.service;

import com.tong.pojo.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IBlogService extends IService<Blog> {

    Long saveBlog(Blog blog);

    void likeBlogById(Long id);

    List<Blog> pageMyBlog(Integer pageNum);

    List<Blog> pageHotBlog(Integer pageNum);
}
