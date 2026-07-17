# 阿里云低成本部署

## 首版资源

- 1 台 2 核 4GB ECS；Docker Compose 同机运行 Server、MySQL 8.4、Caddy。
- 40GB 以上 ESSD，MySQL 与照片分别使用 Docker volume。
- 只开放安全组 22、80、443；不要把 3306 暴露公网。
- 图片首版落本机挂载盘，群规模稳定后再迁移到 OSS。`MediaStorage` 已隔离存储实现。

## 上线

```bash
cp .env.example .env
# 填写域名、微信、数据库和 DashScope 参数
docker compose build server
docker compose up -d
docker compose ps
curl https://你的域名/api/health
```

Caddy 自动申请 HTTPS 证书。随后把 `https://你的域名` 加入微信公众平台的 request/uploadFile/downloadFile 合法域名，并把 `miniprogram/config/index.ts` 的 API 地址改为正式域名。

## Qwen3.6

照片识别走阿里云百炼 OpenAI 兼容接口。必须提供：

- `DASHSCOPE_API_KEY`
- `DASHSCOPE_WORKSPACE_ID`，或直接填写 `QWEN_BASE_URL`
- `QWEN_VISION_MODEL`：默认 `qwen3.6-flash`；联调期可改为 `qwen3.6-plus`

未配置或调用失败时，识别任务会进入 `FAILED`，小程序自动转入手动品牌/品类/产品补录，不影响核心记录闭环。

## 备份

每日备份 MySQL，保留 7 个日备与 4 个周备；每周复制 `media-data` 到 OSS 归档桶。恢复演练至少每月一次。`.env`、数据库备份和用户照片不得进入 Git。

## 常见坑

- 运行要求 Java 21；本机 Java 8 无法编译本项目。
- 小程序正式环境不允许 HTTP 或未备案域名。
- `ONE_MEDIA_BASE_URL` 必须是公网 HTTPS 且指向 `/api/media/public`。
- 修改已经执行过的 Flyway V1 会产生校验冲突；首个正式环境上线后只能新增 V2、V3 迁移。
