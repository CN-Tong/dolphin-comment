package com.tong.service;

import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.pojo.entity.User;

import java.util.List;

public interface IBlogService extends IService<Blog> {

    Long saveBlog(Blog blog);

    void likeBlogById(Long id);

    List<Blog> pageMyBlog(Integer pageNum);

    List<Blog> pageHotBlog(Integer pageNum);

    Blog queryBlogById(Long id);

    List<UserDTO> queryBlogLikes(Long id);

    List<Blog> pageUserBlogs(Long id, Integer current);
}
