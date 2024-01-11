package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.Blog;
import com.tong.pojo.entity.User;
import com.tong.mapper.BlogMapper;
import com.tong.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.SystemConstants;
import com.tong.utils.UserHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Override
    public Long saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        save(blog);
        return blog.getId();
    }

    @Override
    public void likeBlogById(Long id) {
        update().setSql("liked = liked + 1")
                .eq("id", id)
                .update();
    }

    @Override
    public List<Blog> pageMyBlog(Integer pageNum) {
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = Page.of(pageNum, SystemConstants.MAX_PAGE_SIZE);
        Page<Blog> p = lambdaQuery()
                .eq(Blog::getUserId, user.getId())
                .page(page);
        return p.getRecords();
    }

    @Override
    public List<Blog> pageHotBlog(Integer pageNum) {
        Page<Blog> page = Page.of(pageNum, SystemConstants.MAX_PAGE_SIZE);
        Page<Blog> p = lambdaQuery()
                .orderByDesc(Blog::getLiked)
                .page(page);
        List<Blog> records = p.getRecords();
        // 补充冗余字段 icon name
        records.forEach(blog -> {
            Long userId = blog.getUserId();
            User user = Db.lambdaQuery(User.class)
                    .eq(User::getId, userId)
                    .one();
            blog.setIcon(user.getIcon());
            blog.setName(user.getNickName());
        });
        return records;
    }
}
