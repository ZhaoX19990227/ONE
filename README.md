# ONE

> 让有趣的灵魂，同频相遇。

ONE 是一个以具体活动为中心的兴趣搭子微信小程序。仓库包含微信原生小程序、Java 后端、运营管理端以及适合单台阿里云 ECS 的低成本部署配置。

## 仓库结构

```text
ONE/
├── miniprogram/        微信原生小程序（TypeScript）
├── server/             Java 21 + Spring Boot 4.1 模块化单体
├── admin-web/          React + TypeScript 运营后台
├── deploy/             Caddy 与部署配置
├── docs/               产品、架构和接口说明
└── docker-compose.yml  单机部署编排
```

## 本地快速启动

### 1. 启动后端和 MySQL

```bash
cp .env.example .env
# 本地演示时将 .env 中 WECHAT_MOCK_ENABLED 与 ONE_DEMO_DATA_ENABLED 改为 true
docker compose up -d mysql server
curl http://localhost:8080/api/health
```

### 2. 预览小程序

使用微信开发者工具导入 `miniprogram` 目录。项目默认启用本地 Mock，可在没有 AppID 和后端时查看首页、详情、发布、行程、个人中心和签到动效。

接入后端时修改 `miniprogram/config/index.ts`：

```ts
export const USE_MOCK = false;
export const API_BASE_URL = 'https://your-domain.com/api';
```

### 3. 启动运营后台

```bash
cd admin-web
pnpm install
pnpm dev
```

## 生产部署

生产环境建议至少 2 核 4GB ECS。MySQL 不暴露公网端口，Caddy 自动申请 HTTPS 证书；域名完成备案并在微信公众平台配置为服务器域名后再接入小程序。

```bash
cp .env.example .env
# 填写生产配置
docker compose --profile production up -d --build
```

详细说明见 [架构文档](docs/ARCHITECTURE.md)、[产品范围](docs/PRODUCT.md)、[接口清单](docs/API.md) 和 [部署手册](docs/DEPLOYMENT.md)。

## 当前范围

- 已覆盖：微信登录骨架、活动发现、活动详情、结构化发布、报名并发控制、候补、签到、行程、个人兴趣护照、运营审核接口、规则型 AI 活动草稿解析与内容安全扩展口。
- 已预留：微信支付、订阅消息、模型供应商适配器、微信/企业微信活动群。
- 暂不开放：自由私聊、未成年人使用、平台代收代付、实时语音。
