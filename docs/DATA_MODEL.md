# 数据模型

## 目录

- `catalog_category`：早餐、米饭、粉面、汉堡、火锅、奶茶、轻乳茶、美式、拿铁等。
- `catalog_brand`：麦当劳、肯德基、海底捞、喜茶、星巴克、Manner 等。
- `catalog_item`：具体单品及默认价格、口味和可配置属性。
- `brand_alias`：OCR、用户输入和历史名称归一化。

## 推荐闭环

- `decision_session`：一次转盘/AI推荐上下文和最终选择。
- `decision_candidate`：当次三个候选、得分、理由和建议配置。
- `recommendation_feedback`：有期限的“不想吃”弱反馈，不等同于真实生活记录。
- `preference_memory`：从历史记录产生的喜欢、甜咸辣、分量、价格和回购记忆。

`preference_memory.source_record_id` 与 `occurred_at` 让所有推荐提示可解释、可撤回，而不是只保存一个不可追溯的“偏好分”。

## 记录

- `life_record`：所有时间线记录的公共头，包含类型、时间、图片和金额。
- `meal_record_detail`：用餐方式、菜系、口味、饱腹感和回购反馈。
- `drink_record_detail`：奶茶/咖啡类型、糖度、冰量、杯型、小料和回购反馈。
- `private_habit_record`：鹿一下的可选身体感受；公共头没有金额、图片和分享能力。

金额统一使用分：原价、优惠、实付。统计以实付金额为准。

`life_record.record_status` 区分 `DRAFT/CONFIRMED/DELETED`。日历、统计和记忆只读取 `CONFIRMED`，推荐选择产生的草稿不会被重复计算。品牌与产品同时保存外键和名称快照，目录以后改名不会改变历史记录。

## 图片识别与目录归一化

- `media_asset`：原图、缩略图、内容类型和存储键。
- `recognition_task`：识别维度、候选 JSON、置信度、用户确认结果和失败原因。
- `catalog_custom_entry`：AI 未命中后用户输入的品牌/单品，后续可由管理员合并到正式目录。

`catalog_custom_entry` 归一后保存目标品牌/产品 ID；相同品牌名+产品名的后续手动录入会自动使用标准产品。管理员合并时可同时创建 `brand_alias`，让图片识别也逐步变准。

## 群选择

- `group_decision_room`：分享码、发起人、分类、有效期和最终赢家。
- `group_room_candidate`：2—8 个标准目录候选。
- `group_room_vote`：每个用户每个房间唯一一票。

客户端永远不接收 `group_room_vote.user_id` 列表，只接收候选聚合票数和当前用户自己的票。

识别候选不能直接写入正式记录，必须由用户确认。确认既可以命中正式枚举，也可以保留自定义快照。

## 用户

属性使用字符串枚举 `ONE/ZERO/VERS/SIDE`，数据库可空，不使用小数 `0.5`。身高使用厘米，体重使用克，避免浮点误差。
