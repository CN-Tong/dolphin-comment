package com.tong.controller;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog-comments")
@Api(tags = "博客评论相关接口")
@Slf4j
public class BlogCommentsController {

}
