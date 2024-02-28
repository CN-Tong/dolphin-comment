package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tong.constant.RedisConstants.BLOG_LIKED_KEY;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        save(blog);
        return blog.getId();
    }

    @Override
    public void likeBlogById(Long id) {
        // 1.判断当前登录用户是否已经点赞
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + id;
        // Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 2.如果未点赞，可以点赞，数据库点赞数+1，保存到Redis
            boolean isSuccess = update().setSql("liked = liked + 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                // stringRedisTemplate.opsForSet().add(key, userId.toString());
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 3.如果已点赞，取消点赞，数据库点赞数-1，从Redis中删除
            boolean isSuccess = update().setSql("liked = liked - 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                // stringRedisTemplate.opsForSet().remove(key, userId.toString());
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
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
        // 补充冗余字段 icon name isLike
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return records;
    }

    @Override
    public Blog queryBlogById(Long id) {
        // 查询blog
        Blog blog = getById(id);
        if (blog == null) {
            throw new RuntimeException("评论不存在!");
        }
        // 查询blog有关的用户
        queryBlogUser(blog);
        // 查询blog是否被点赞，并保存至blog的isLike属性
        isBlogLiked(blog);
        return blog;
    }

    @Override
    public List<UserDTO> queryBlogLikes(Long id) {
        // 1.查询TOP5的点赞分数
        String key = BLOG_LIKED_KEY + id;
        Set<String> topIds = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(topIds == null){
            return Collections.emptyList();
        }
        // 2.解析用户id
        List<Long> ids = topIds.stream().map(Long::valueOf).collect(Collectors.toList());
        // 3.根据用户id查询用户
        // List<User> userList = Db.listByIds(ids, User.class);
        // where id in (1012, 1) order by field(id, 1012, 1)
        String idsStr = StrUtil.join(",", ids); // 将数组拼接成字符串，用StringJoiner也行
        List<User> userList = Db.query(User.class)
                .in("id", ids)
                .last("order by field(id, " + idsStr + ")") //手动拼接sql语句
                .list();
        List<UserDTO> userDTOList = BeanUtil.copyToList(userList, UserDTO.class);
        // 4.返回结果
        return userDTOList;
    }

    @Override
    public List<Blog> pageUserBlogs(Long id, Integer current) {
        Page<Blog> p = lambdaQuery()
                .eq(Blog::getUserId, id)
                .page(new Page<Blog>(current, SystemConstants.MAX_PAGE_SIZE));
        return p.getRecords();
    }

    /**
     * 查询blog是否被点赞，并保存至blog的isLike属性
     */
    private void isBlogLiked(Blog blog) {
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + blog.getId();
        // Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        // blog.setIsLike(BooleanUtil.isTrue(isMember));
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    /**
     * 补充冗余字段 icon name
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = Db.lambdaQuery(User.class)
                .eq(User::getId, userId)
                .one();
        blog.setIcon(user.getIcon());
        blog.setName(user.getNickName());
    }
}
