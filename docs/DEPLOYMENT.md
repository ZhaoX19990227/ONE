# 阿里云低成本部署

## 建议配置

- 初期：2 核 4GB、60GB ESSD、固定带宽 3～5Mbps。
- 同机运行 MySQL 时不要选择 2GB 内存；Java 最大堆建议 768MB。
- 数据库、Actuator 和管理端接口均不直接暴露公网。
- 图片量增长后迁移到阿里云 OSS，数据库继续只存 URL。

## 部署步骤

1. 完成域名备案并将域名解析到 ECS。
2. 安装 Docker Engine 与 Compose 插件。
3. 将 `.env.example` 复制为 `.env` 并替换全部密码和微信配置。
4. 确认安全组只开放 22、80、443。
5. 执行 `docker compose --profile production up -d --build`。
6. 访问 `https://域名/api/health` 验证健康状态。
7. 在微信公众平台配置 `https://域名` 为 request 合法域名。

生产环境必须为 `ONE_TOKEN_SECRET` 和 `ONE_ADMIN_PASSWORD` 使用独立的高强度随机值。管理端账号由 `ONE_ADMIN_USERNAME`、`ONE_ADMIN_PASSWORD` 提供，不写入前端构建产物；建议再通过阿里云安全组、VPN 或 Caddy IP 白名单限制 `/admin` 的访问来源。

部署后执行一次最小验收：

```bash
curl https://你的域名/api/health
curl -X POST https://你的域名/api/auth/admin \
  -H 'Content-Type: application/json' \
  -d '{"username":"你的管理员账号","password":"你的管理员密码"}'
```

## 备份

- 每天使用 `mysqldump --single-transaction` 备份至加密目录。
- 每周将备份同步至 OSS 低频存储。
- 至少每月执行一次恢复演练。
- 上线支付前必须增加订单与退款对账任务。
