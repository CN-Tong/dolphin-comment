package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.Blog;
import com.tong.pojo.entity.Follow;
import com.tong.pojo.entity.User;
import com.tong.mapper.BlogMapper;
import com.tong.result.ScrollResult;
import com.tong.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.SystemConstants;
import com.tong.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tong.constant.RedisConstants.BLOG_LIKED_KEY;

@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        boolean isSuccess = save(blog);
        if(!isSuccess){
            throw new BusinessException("保存笔记失败");
        }
        // 查询博客作者的所有粉丝
        List<Follow> followList = Db.lambdaQuery(Follow.class).eq(Follow::getFollowUserId, user.getId()).list();
        // 推送博客id给所有粉丝
        for (Follow follow : followList) {
            // 获取粉丝id
            Long fansId = follow.getUserId();
            // 推送，每个粉丝都有一个收件箱，所以key使用粉丝id，value使用笔记id，score使用时间戳
            String key = RedisConstants.FEED_KEY + fansId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        // 返回id
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

    @Override
    public ScrollResult pageBlogOfFollow(Long lastMinTime, Integer offset) {
        ScrollResult result = new ScrollResult();
        // 1.查询当前用户的收件箱
        Long currentUserId = UserHolder.getUser().getId();
        String key = RedisConstants.FEED_KEY + currentUserId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, lastMinTime, offset, 2);
        if(CollUtil.isEmpty(typedTuples)){
            result.setList(Collections.emptyList());
            return result;
        }
        // 2.解析数据 blogId，minTime，offset
        //
        List<Long> blogIds = new ArrayList<>(typedTuples.size());
        // 本次查询的最小时间戳
        long newMinTime = 0;
        // 下次查询的偏移量(本次查询最小时间戳的重复次数)
        int newOffset = 1;
        // 解析数据
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            // Redis中查到的数据的value即为blogId，添加至blogIds集合中
            String blogIdStr = typedTuple.getValue();
            blogIds.add(Long.valueOf(blogIdStr));
            // Redis中查到的数据的score即为时间戳
            long blogTime = typedTuple.getScore().longValue();
            // 判断该blog的时间戳与上一个blog的时间戳是否一致
            if(blogTime == newMinTime){
                // 若一致则偏移量newOffset+1
                newOffset++;
            }else{
                // 不一致则将该blog时间戳替换本次查询的最小时间戳，并重置偏移量newOffset
                newMinTime = blogTime;
                newOffset = 1;
            }
        }
        // 3.根据id查询blog(不能用MySQL的in，需要保留id原始的顺序)
        String blogIdsStr = StrUtil.join(",", blogIds);
        List<Blog> blogList = Db.query(Blog.class)
                .in("id", blogIds)
                .last("order by field(id, " + blogIdsStr + ")") //手动拼接sql语句
                .list();
        // 所有查blog的都要补充blog点赞用户的信息，并查询该blog是否被当前用户点赞
        for (Blog blog : blogList) {
            // 查询blog有关的用户
            queryBlogUser(blog);
            // 查询blog是否被点赞，并保存至blog的isLike属性
            isBlogLiked(blog);
        }
        // 封装并返回
        log.info("newMinTime：{}，newOffset：{}", newMinTime, newOffset);
        result.setList(blogList);
        result.setMinTime(newMinTime);
        result.setOffset(newOffset);
        return result;
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
