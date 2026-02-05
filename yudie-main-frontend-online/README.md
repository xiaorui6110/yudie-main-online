# 语蝶图库前端项目

一个基于 Vue 3 + TypeScript 开发的现代化图片分享社区前端项目。支持图片上传、分享、社交互动等功能。


生产环境下将 vite.config.ts 文件中的your_server_url替换为实际服务器地址
```angular2html
proxy: {
        '/api': {
          target: isProd ? 'your server url' : 'http://localhost:8812',
          changeOrigin: true,
          ws: true
        }
      },
```
将.env.production文件中的VITE_WS_URL替换为实际WS服务器地址
## 技术栈

- **核心框架**: Vue 3
- **开发语言**: TypeScript
- **状态管理**: Pinia
- **UI 框架**:
    - Ant Design Vue (PC端)
    - Vant (移动端)
- **路由**: Vue Router
- **HTTP 请求**: Axios
- **构建工具**: Vite
- **代码规范**: ESLint + Prettier
- **CSS 预处理器**: SCSS/LESS

## 主要功能

- 用户系统
    - 登录/注册
    - 邮箱验证
    - 个人信息管理
    - 用户关注
- 图片管理
    - 图片上传
    - 图片编辑
    - 图片分类
    - 图片搜索
- 空间系统
    - 空间创建
    - 成员管理
    - 空间分析
- 社交功能
    - 评论互动
    - 点赞分享
    - 私信聊天
- 内容发现
    - 个性化推荐
    - 热门排行
    - 关注动态
- 消息通知
    - 互动提醒
    - 系统通知
    - 未读消息

## 项目结构


## 开发环境搭建

1. 安装依赖 npm install
2. 启动开发服务器 npm run dev
3. 构建生产版本 npm run build
