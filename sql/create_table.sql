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

-- 用户签到记录表
CREATE TABLE user_sign_in_record
(
    id         bigint                             NOT NULL COMMENT 'id'
        PRIMARY KEY,
    userId     bigint                             NOT NULL COMMENT '用户id',
    year       int                                NOT NULL COMMENT '年份',
    signInData binary(46)                         NOT NULL COMMENT '签到数据位图(366天/8≈46字节)',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    CONSTRAINT uk_userId_year
        UNIQUE (userId, year) COMMENT '用户id和年份唯一索引'
)
    COMMENT '用户签到记录表' COLLATE = utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE picture
(
    id            bigint AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    url           varchar(512)                       NOT NULL COMMENT '图片 url',
    name          varchar(128)                       NOT NULL COMMENT '图片名称',
    introduction  varchar(512)                       NULL COMMENT '简介',
    category      varchar(64)                        NULL COMMENT '分类',
    tags          varchar(512)                       NULL COMMENT '标签（JSON 数组）',
    picSize       bigint                             NULL COMMENT '图片体积',
    picWidth      int                                NULL COMMENT '图片宽度',
    picHeight     int                                NULL COMMENT '图片高度',
    picScale      double                             NULL COMMENT '图片宽高比例',
    picFormat     varchar(32)                        NULL COMMENT '图片格式',
    userId        bigint                             NOT NULL COMMENT '创建用户 id',
    createTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime      datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete      tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    reviewStatus  int      DEFAULT 0                 NOT NULL COMMENT '审核状态：0 - 待审核; 1 - 通过; 2 - 拒绝',
    reviewMessage varchar(512)                       NULL COMMENT '审核信息',
    reviewerId    bigint                             NULL COMMENT '审核人 ID',
    reviewTime    datetime                           NULL COMMENT '审核时间',
    thumbnailUrl  varchar(512)                       NULL COMMENT '缩略图 url',
    spaceId       bigint                             NULL COMMENT '空间 id（为空表示公共空间）',
    picColor      varchar(16)                        NULL COMMENT '图片主色调',
    commentCount  bigint   DEFAULT 0                 NOT NULL COMMENT '评论数',
    likeCount     bigint   DEFAULT 0                 NOT NULL COMMENT '点赞数',
    shareCount    bigint   DEFAULT 0                 NOT NULL COMMENT '分享数',
    viewCount     bigint   DEFAULT 0                 NOT NULL COMMENT '浏览量'
)
    COMMENT '图片表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_category
    ON picture (category);

CREATE INDEX idx_introduction
    ON picture (introduction);

CREATE INDEX idx_name
    ON picture (name);

CREATE INDEX idx_reviewStatus
    ON picture (reviewStatus);

CREATE INDEX idx_spaceId
    ON picture (spaceId);

CREATE INDEX idx_tags
    ON picture (tags);

CREATE INDEX idx_userId
    ON picture (userId);

CREATE INDEX idx_viewCount
    ON picture (viewCount);

-- 分类表
CREATE TABLE category
(
    id           bigint AUTO_INCREMENT COMMENT '分类id'
        PRIMARY KEY,
    categoryName varchar(256)                       NOT NULL COMMENT '分类名称',
    type         tinyint  DEFAULT 0                 NOT NULL COMMENT '分类类型：0 - 图片分类 1 - 帖子分类',
    createTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime     datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '分类编辑时间',
    updateTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '分类更新时间',
    isDelete     tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '分类表' COLLATE = utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE tag
(
    id         bigint AUTO_INCREMENT COMMENT '标签 id'
        PRIMARY KEY,
    tagName    varchar(256)                       NOT NULL COMMENT '标签名称',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除'
)
    COMMENT '标签表' COLLATE = utf8mb4_unicode_ci;

-- 空间表
CREATE TABLE space
(
    id         bigint AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    spaceName  varchar(128)                       NULL COMMENT '空间名称',
    spaceLevel int      DEFAULT 0                 NULL COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   DEFAULT 0                 NULL COMMENT '空间图片的最大总大小',
    maxCount   bigint   DEFAULT 0                 NULL COMMENT '空间图片的最大数量',
    totalSize  bigint   DEFAULT 0                 NULL COMMENT '当前空间下图片的总大小',
    totalCount bigint   DEFAULT 0                 NULL COMMENT '当前空间下的图片数量',
    userId     bigint                             NOT NULL COMMENT '创建用户 id',
    createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    editTime   datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    spaceType  int      DEFAULT 0                 NOT NULL COMMENT '空间类型：0-私有 1-团队'
)
    COMMENT '空间表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_spaceLevel
    ON space (spaceLevel);

CREATE INDEX idx_spaceName
    ON space (spaceName);

CREATE INDEX idx_spaceType
    ON space (spaceType);

CREATE INDEX idx_userId
    ON space (userId);

-- 空间用户关联表
CREATE TABLE space_user
(
    id         bigint AUTO_INCREMENT COMMENT 'id'
        PRIMARY KEY,
    spaceId    bigint                                 NOT NULL COMMENT '空间 id',
    userId     bigint                                 NOT NULL COMMENT '用户 id',
    spaceRole  varchar(128) DEFAULT 'viewer'          NULL COMMENT '空间角色：viewer/editor/admin',
    status     tinyint      DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝',
    createTime datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_spaceId_userId
        UNIQUE (spaceId, userId)
)
    COMMENT '空间用户关联表' COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_spaceId
    ON space_user (spaceId);

CREATE INDEX idx_status
    ON space_user (status);

CREATE INDEX idx_userId
    ON space_user (userId);



















