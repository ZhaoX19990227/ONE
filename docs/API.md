# API 清单

统一前缀 `/api`，除健康检查、登录和公开目录外均需 Bearer Token。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/auth/wechat` | 微信 code 登录 |
| GET/PUT | `/me` | 资料、口味和可见性 |
| GET | `/catalog/categories` | 分类目录 |
| GET | `/catalog/brands` | 品牌目录 |
| GET | `/catalog/items` | 单品目录 |
| POST | `/recommendations` | 生成三个候选和记忆提示 |
| POST | `/recommendations/{id}/candidates/{candidateId}/choose` | 记录用户选择 |
| POST | `/media/images` | 上传待识别照片 |
| POST | `/records/meals` | 新增吃饭记录并生成记忆 |
| POST | `/records/drinks` | 新增奶茶/咖啡记录并生成记忆 |
| POST | `/recognitions` | 上传完成后创建图片识别任务 |
| POST | `/recognitions/{id}/confirm` | 确认/修正品牌、品类和产品 |
| POST | `/records/deer` | 新增鹿一下记录 |
| GET | `/records/today` | 今日时间线 |
| GET | `/records?date=YYYY-MM-DD` | 某日详情 |
| GET | `/calendar/month?month=YYYY-MM` | 月历聚合 |
| GET | `/analytics/summary?from=&to=` | 日/周/月汇总数据 |

错误统一返回稳定 `code`，客户端不得匹配中文错误文案。

推荐确认和自主录入最终使用相同的记录创建服务，保证日历、统计与记忆的数据口径一致。
