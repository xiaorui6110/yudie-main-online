# 语蝶图片管理系统后端

> 该项目是半年前参考 GitHub 上大佬的项目 https://github.com/humenglover/yuemu 进行二次开发学习的，由于一些原因 git 的提交记录没有完全保留（之前也没有意识到提交规范等等），目前部署网站的服务器和存储桶等已经过期，故没有示例界面了（是之前忘记截图了ε(┬┬﹏┬┬)3），现将其重新整理存放在 GitHub 上。

## 项目简介

语蝶图片管理系统是一个功能完善的图片管理与分享平台后端服务，采用前后端分离架构，提供完整的 RESTful API 接口。系统支持图片上传、智能处理、内容审核、社交互动、团队协作等丰富功能，并集成了 AI 扩图和以图搜图等智能服务。

## 核心技术栈

### 基础框架
- **Spring Boot 2.6.13** - 应用核心框架
- **Spring MVC** - Web 框架
- **Spring AOP** - 面向切面编程，用于权限校验、日志记录
- **Spring Session + Redis** - 分布式会话管理
- **Spring Task** - 定时任务调度

### 数据持久层
- **MyBatis Plus 3.5.9** - ORM 框架，增强 MyBatis 功能
- **MySQL 8.0** - 关系型数据库，存储业务数据
- **ShardingSphere 5.2.0** - 分库分表中间件，支持图片表按空间分片

### 缓存与分布式
- **Redis 6.x** - 分布式缓存、会话存储、计数器、限流
- **Redisson 3.21.0** - 分布式锁、限流器
- **Disruptor 3.4.2** - 高性能无锁队列，用于 WebSocket 消息处理

### 存储服务
- **腾讯云 COS 5.6.227** - 图片文件对象存储
- **本地文件系统** - 临时文件存储

### API 文档
- **Knife4j 4.4.0** - Swagger 增强 API 文档，提供在线调试

### 权限认证
- **Sa-Token 1.39.0** - 轻量级权限认证框架
- **Sa-Token Redis** - Redis 持久化支持

### 工具库
- **Hutool 5.8.26** - Java 工具类库
- **Jsoup 1.15.3** - HTML 解析
- **Apache POI 4.1.2** - Excel 导出
- **JavaMail 1.6.2** - 邮件发送

### AI 与搜索服务
- **阿里云 AI** - 图片智能扩图服务
- **百度以图搜图 API** - 相似图片搜索
- **360 以图搜图 API** - 多源图片搜索

## 系统架构

### 分层设计
```
controller/    # 控制层 - 处理 HTTP 请求
  service/      # 业务逻辑层 - 核心业务处理
    mapper/     # 数据访问层 - 数据库操作
      entity/   # 实体层 - 数据模型
```

### 核心模块
- **用户认证模块** - 注册、登录、权限管理
- **图片管理模块** - 上传、处理、审核、搜索
- **空间协作模块** - 团队空间、成员管理、权限控制
- **社交互动模块** - 点赞、收藏、评论、关注
- **消息通知模块** - 站内信、WebSocket 实时推送
- **内容审核模块** - AI 内容检测、人工审核
- **定时任务模块** - 缓存预热、推荐计算、数据同步

## 核心功能

### 1. 用户管理
- **用户注册** - 邮箱注册，密码加密存储
- **用户登录** - 邮箱账号登录，Redis 存储会话
- **个人信息** - 头像、昵称、简介等基本信息管理
- **权限控制** - 基于角色的权限管理（user/admin/ban）
- **签到系统** - 用户签到记录与统计

### 2. 图片管理
- **图片上传**
  - 支持单图/多图上传
  - 本地文件上传与 URL 链接上传
  - 自动提取图片元信息（尺寸、大小、格式、主色调）
  - 腾讯云 COS 对象存储
  - 自动生成缩略图
- **图片处理**
  - AI 智能扩图（阿里云服务）
  - 图片分类与标签管理
- **图片组织**
  - 分类体系（动漫、风景、美食等）
  - 标签系统，支持多标签
  - 相册功能（基于空间）
- **图片安全**
  - 内容审核（待审核/通过/拒绝）
  - 审核邮件通知
  - 防爬虫检测与限流
  - 下载权限控制

### 3. 空间协作
- **空间类型** - 公共空间、个人空间、团队空间
- **空间等级**
  - 普通版（50 张图片，100MB）
  - 专业版（100 张图片，250MB）
  - 旗舰版（250 张图片，500MB）
- **成员管理**
  - 成员邀请与添加
  - 角色分配（所有者/管理员/编辑者/查看者）
  - 基于角色的权限控制（RBAC）
- **空间分析**
  - 空间容量使用统计
  - 图片数据可视化分析

### 4. 搜索系统
- **综合搜索** - 支持图片、用户、帖子、空间的全文检索
- **热门搜索** - 热门关键词排行
- **以图搜图** - 集成百度和 360 的图片搜索 API
- **搜索建议** - 热门搜索缓存

### 5. 社交互动
- **点赞收藏** - 支持点赞、取消点赞，收藏管理
- **评论系统**
  - 支持多级评论
  - 评论类型区分（图片/帖子）
- **关注系统** - 用户关注、粉丝管理
- **分享功能** - 图片分享记录统计
- **帖子功能** - 发布帖子、附件管理、帖子浏览

### 6. 消息通知
- **站内信** - 系统通知、互动消息
- **实时推送** - WebSocket 实时消息推送
- **私信功能** - 用户间私信聊天
- **消息中心** - 消息分类与未读计数

### 7. AI 智能服务
- **AI 聊天** - 集成 DeepSeek AI 模型，提供智能对话
- **图片扩图** - 阿里云 AI 智能扩图服务
- **内容审核** - 自动内容识别与审核

### 8. 定时任务
- **图片推荐分数计算** - 基于浏览、点赞、评论、分享、时间等多维度权重计算
- **缓存预热** - 热门数据缓存预热（图片、帖子、聊天）
- **热门搜索同步** - 搜索关键词统计与同步
- **内容审核检查** - 定时检查待审核内容，发送邮件通知
- **活动过期处理** - 自动处理过期活动
- **用户签到同步** - 用户签到数据同步

## 系统特性

### 高可用设计
- 服务无状态，支持水平扩展
- Redis 分布式锁保证并发安全
- 分布式会话管理

### 高性能
- **多级缓存**
  - 本地缓存（应用内存）
  - Redis 分布式缓存
- **异步处理**
  - Spring 异步任务处理
  - Disruptor 高性能消息队列（WebSocket 消息）
- **数据库优化**
  - ShardingSphere 分库分表
  - MyBatis Plus 分页插件
  - 合理的索引设计

### 安全性
- **数据安全**
  - 敏感数据加密存储
  - 基于 Sa-Token 的权限认证
  - 逻辑删除防止数据误删
- **系统安全**
  - SQL 注入防护（MyBatis 预编译）
  - 防爬虫检测与限流
  - 操作日志记录
- **接口安全**
  - 统一异常处理
  - 参数校验

### 可扩展性
- 模块化设计，职责清晰
- 接口标准化
- 配置外部化
- 支持插件扩展

## 环境要求

- **JDK** 1.8+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Redis** 6.x+
- **Node.js** 14+（仅前端开发需要）

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/yudie-main-backend.git
cd yudie-main-backend
```

### 2. 数据库初始化
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE yudie_picture DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入 SQL 脚本
mysql -u root -p yudie_picture < sql/create_table.sql
```

### 3. 配置修改
编辑 `src/main/resources/application-dev.yml`，修改以下配置等等：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yudie_picture
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
    database: 10
  mail:
    from: your_email@qq.com
    password: your_email_authorization_code
    admin: admin_email@qq.com

cos:
  client:
    secretId: your_tencent_cloud_secret_id
    secretKey: your_tencent_cloud_secret_key
    bucket: your_bucket_name
    region: ap-nanjing

aliYunAi:
  apiKey: your_aliyun_ai_api_key

spring.ai.deepseek:
  apiKey: your_deepseek_api_key
```

### 4. 启动项目
```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者打包后启动
mvn clean package
java -jar target/yudie-main-backend-0.0.1-SNAPSHOT.jar
```

### 5. 访问接口文档
启动成功后，访问 Knife4j 接口文档：
```
http://localhost:8812/api/doc.html
```

## 项目结构

```
yudie-main-backend/
├── src/main/java/com/yudie/yudiemainbackend/
│   ├── annotation/          # 自定义注解
│   │   └── AuthCheck.java   # 权限校验注解
│   ├── aop/                 # AOP 切面
│   │   └── AuthInterceptor.java
│   ├── api/                 # 第三方 API 封装
│   │   ├── aliyunai/        # 阿里云 AI 扩图
│   │   └── imagesearch/     # 以图搜图（百度/360）
│   ├── common/              # 公共类
│   │   ├── BaseResponse.java
│   │   ├── DeleteRequest.java
│   │   ├── PageRequest.java
│   │   └── ResultUtils.java
│   ├── config/              # 配置类
│   │   ├── AsyncConfig.java
│   │   ├── CorsConfig.java
│   │   ├── CosClientConfig.java
│   │   ├── DeepSeekConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   └── RedissonConfig.java
│   ├── constant/            # 常量定义
│   ├── controller/          # 控制器层
│   ├── exception/           # 异常处理
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ThrowUtils.java
│   ├── job/                 # 定时任务
│   │   ├── PictureRecommendScoreJob.java
│   │   ├── CacheWarmUpJob.java
│   │   ├── HotSearchSyncJob.java
│   │   └── ContentReviewJob.java
│   ├── manager/             # 业务管理器
│   │   ├── auth/            # 权限管理
│   │   ├── sharding/        # 分片管理
│   │   ├── upload/          # 上传管理
│   │   ├── websocket/       # WebSocket 管理
│   │   ├── CosManager.java
│   │   ├── CounterManager.java
│   │   └── CrawlerManager.java
│   ├── mapper/              # MyBatis Mapper
│   ├── model/               # 数据模型
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── enums/           # 枚举类
│   │   └── vo/              # 视图对象
│   ├── service/             # 业务逻辑层
│   └── utils/               # 工具类
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   ├── application-dev.yml  # 开发环境配置
│   ├── application-prod.yml # 生产环境配置
│   ├── biz/                 # 业务配置
│   ├── html/                # HTML 模板（邮件）
│   └── mapper/              # MyBatis XML 映射
├── sql/
│   └── create_table.sql     # 数据库建表脚本
├── pom.xml                  # Maven 配置
└── README.md                # 项目文档
```

## API 接口

### 基础路径
```
http://localhost:8812/api
```

### 主要接口
| 模块  | 路径               | 说明             |
|-----|------------------|----------------|
| 用户  | `/user/*`        | 用户注册、登录、信息管理   |
| 图片  | `/picture/*`     | 图片上传、查询、编辑、删除  |
| 空间  | `/space/*`       | 空间管理、成员管理、空间分析 |
| 分类  | `/category/*`    | 分类管理           |
| 标签  | `/tag/*`         | 标签管理           |
| 搜索  | `/search/*`      | 综合搜索、热门搜索      |
| 帖子  | `/post/*`        | 帖子发布、查询        |
| 评论  | `/comments/*`    | 评论管理           |
| 点赞  | `/like/*`        | 点赞记录           |
| 收藏  | `/share/*`       | 分享记录           |
| 关注  | `/userFollows/*` | 用户关注           |
| 消息  | `/message/*`     | 消息中心           |
| 私信  | `/privateChat/*` | 私信聊天           |
| 活动  | `/activity/*`    | 活动管理           |
| AI  | `/deepseek/*`    | AI 聊天          |
| 文件  | `/file/*`        | 文件上传           |

详细接口文档请访问：`http://localhost:8812/api/doc.html`

## 部署说明

### 生产环境配置
修改 `spring.profiles.active` 为 `prod`：

```yaml
spring:
  profiles:
    active: prod
```

### Docker 部署（可选）
```dockerfile
FROM openjdk:8-jre-alpine
COPY target/yudie-main-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8812
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Nginx 反向代理配置
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location /api/ {
        proxy_pass http://localhost:8812/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 常见问题

### 1. Redis 连接失败
检查 Redis 是否启动，配置是否正确。

### 2. 文件上传失败
检查腾讯云 COS 配置是否正确，存储桶权限是否正确设置。

### 3. AI 扩图失败
检查阿里云 AI API Key 是否正确，余额是否充足。
