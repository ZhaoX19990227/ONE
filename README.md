# ONE

> 吃什么、喝什么，一个就够了。

ONE 是一个服务单个微信群的微信小程序：帮助用户决定吃饭、奶茶和咖啡，记录实际消费、图片与评价，并把“有点甜”“还会再吃”等反馈沉淀为下一次推荐可解释的记忆；同时提供名为“鹿一下”的轻量个人习惯计数。

## 工程结构

```text
ONE/
├── miniprogram/       微信原生小程序（TypeScript）
├── server/            Java 21 + Spring Boot + MySQL
├── deploy/            Caddy HTTPS 与单机部署配置
├── docs/              产品、架构、数据模型与接口文档
└── docker-compose.yml 阿里云单机部署编排
```

## 核心闭环

```text
时间与场景 → 转盘/AI候选 → 用户选择 → 实际记录
→ 口味与金额反馈 → 形成可追溯记忆 → 下一次推荐调整
```

吃饭与饮品可以生成海报分享；`PRIVATE_HABIT` 不进入 AI、群统计或分享。

## 本地启动

```bash
cp .env.example .env
docker compose up -d mysql server
curl http://localhost:8080/api/health
```

微信开发者工具导入 `miniprogram`。默认开启本地演示数据，接入后端时修改 `miniprogram/config/index.ts`。

详细设计见 [产品范围](docs/PRODUCT.md)、[数据模型](docs/DATA_MODEL.md)、[技术架构](docs/ARCHITECTURE.md)、[接口清单](docs/API.md) 和 [部署手册](docs/DEPLOYMENT.md)。
