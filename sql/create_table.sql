-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id           bigint AUTO_INCREMENT COMMENT 'id'	PRIMARY KEY,
    userAccount  varchar(256)                           NOT NULL COMMENT '用户账号',
    userPassword varchar(512)                           NOT NULL COMMENT '用户密码',
    userEmail    varchar(256)                           NULL COMMENT '用户邮箱',
    userName     varchar(256)                           NULL COMMENT '用户昵称',
    userAvatar   varchar(1024)                          NULL COMMENT '用户头像',
    userProfile  varchar(512)                           NULL COMMENT '用户简介',
    userRole     varchar(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/ban',
    editTime     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    createTime   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete     tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT uk_userEmail
    UNIQUE (userEmail),
    CONSTRAINT uk_userAccount
    UNIQUE (userAccount)
    )
    COMMENT '用户表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_userEmail
    ON user (userEmail);

CREATE INDEX idx_userName
    ON user (userName);





