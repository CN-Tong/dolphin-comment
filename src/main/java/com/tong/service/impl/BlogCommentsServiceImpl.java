package com.tong.service.impl;

import com.tong.pojo.entity.BlogComments;
import com.tong.mapper.BlogCommentsMapper;
import com.tong.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
