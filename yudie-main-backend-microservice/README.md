
# 语蝶后端微服务项目

## 技术栈版本

| 技术组件                 | 版本         |
|----------------------|------------|
| Java                 | 17         |
| Spring Boot          | 3.5.3      |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos Server         | 2.3.2      |
| Dubbo                | 3.3.0      |
| Higress              | 2.1.6      |

## 模块说明

| 模块                            | 说明                   |
|-------------------------------|----------------------|
| yudie-main-backend-model      | 实体类、DTO、VO 等数据模型     |
| yudie-main-backend-client     | 内部服务接口定义             |
| yudie-main-backend-common     | 公共配置、工具类、Manager、Job |
| yudie-main-backend-user       | 用户服务                 |
| yudie-main-backend-space      | 空间服务                 |
| yudie-main-backend-picture    | 图片服务                 |
| yudie-main-backend-post       | 帖子服务                 |
| yudie-main-backend-message    | 消息服务                 |
| yudie-main-backend-extend     | 扩展服务（搜索、点赞、分享、AI 聊天） |
| yudie-main-backend-activity   | 活动服务                 |

## 编译打包

```bash
mvn clean package -DskipTests=true
```

## 模块依赖关系

```
model (最底层)
    ↑
client
    ↑
common
    ↑
其他业务模块 (user/space/picture/post/message/extend/activity)
```

