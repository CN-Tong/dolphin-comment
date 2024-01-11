package com.tong.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tong.constant.AutoFillConstants;
import com.tong.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class AutoFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充：INSERT...");
        this.strictInsertFill(metaObject, AutoFillConstants.CREATE_TIME, LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, AutoFillConstants.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充：UPDATE...");
        this.strictUpdateFill(metaObject, AutoFillConstants.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
