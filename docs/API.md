# ONE MVP 接口清单

所有接口统一以 `/api` 开头。除健康检查、微信登录、管理员登录、活动发现和活动详情外，业务接口均要求：

```http
Authorization: Bearer <token>
```

## 身份与资料

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/auth/wechat` | 使用 `wx.login` 的 code 换取 ONE 会话；本地 Mock 可直接传任意非空 code |
| POST | `/auth/admin` | 使用服务端环境变量配置的管理员账号登录 |
| GET | `/me` | 获取兴趣护照与履约统计 |
| PATCH | `/me` | 更新昵称、头像、城市、简介与兴趣标签 |

## 活动闭环

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/activities?cityCode=&page=0&size=20` | 发现页；稳定分页响应为 `content/page/size/totalElements/totalPages` |
| GET | `/activities/{id}` | 活动详情；仅发起人和已确认报名者可见精确位置 |
| POST | `/activities` | 发布线上或线下活动，费用与鸽子金均使用“分” |
| POST | `/activities/{id}/enrollments` | 报名；支持直接确认、待审核和满员候补 |
| DELETE | `/activities/{id}/enrollments/me` | 取消报名并自动递补最早候补用户 |
| POST | `/activities/{id}/enrollments/{enrollmentId}/decision` | 发起人审批报名 |
| POST | `/activities/{id}/check-in` | 活动开始前 2 小时至结束期间签到 |
| GET | `/me/enrollments` | 我的活动与当前报名状态 |

报名和取消通过活动行悲观锁串行修改席位，数据库同时以 `(activity_id, user_id)` 唯一索引兜底，避免并发超卖与重复报名。

## AI 与安全

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/ai/activity-draft` | 将自然语言整理为类型、模式、人数、鸽子金、标签和结构化属性 |
| POST | `/reports` | 举报用户或活动 |
| GET | `/admin/overview` | 运营概览（ADMIN） |
| GET | `/admin/activities` | 活动管理列表（ADMIN） |
| GET | `/admin/reports` | 待处理举报（ADMIN） |
| POST | `/admin/reports/{id}/resolve` | 标记举报已处理（ADMIN） |

当前 AI 默认实现为本地规则引擎，零调用成本且返回结果可解释；`ActivityDraftAssistant` 已作为稳定扩展口，后续可接入通义千问等模型，并保留规则回退。活动发布前会经过 `ContentSafetyGateway`，当前为本地敏感标记兜底，上线前应接入微信内容安全能力。

## 错误结构

```json
{
  "code": "ACTIVITY_FULL",
  "message": "活动名额已满",
  "timestamp": "2026-07-16T12:00:00Z"
}
```

前端应依据 `code` 处理业务分支，不要匹配中文 `message`。
