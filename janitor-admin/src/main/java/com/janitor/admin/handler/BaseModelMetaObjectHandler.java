package com.janitor.admin.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.janitor.admin.base.BaseModel.CREATE_TIME_FIELD_NAME;
import static com.janitor.admin.base.BaseModel.UPDATE_TIME_FIELD_NAME;

/**
 * ClassName BaseModelMetaObjectHandler
 * Description
 *
 * @author 曦逆
 * Date 2022/5/24 17:22
 */
@Slf4j
@Component
public class BaseModelMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, CREATE_TIME_FIELD_NAME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATE_TIME_FIELD_NAME, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName( UPDATE_TIME_FIELD_NAME, LocalDateTime.now(), metaObject);
    }
}
