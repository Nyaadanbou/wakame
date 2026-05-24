# Mechanic / Condition PR 书写规范

本文件描述 MythicMobs Mechanic 和 Condition 的 PR 参数表格式。总体结构见 `SKILL.md`，Placeholder 和 ItemDrop 格式见 `references/placeholder-itemdrop.md`。

---

## 改名节

当批量重命名 mechanic/condition/drop 时，使用列表格式：

```markdown
## Mechanic 改名

为平滑迁移，原名字依然能使用，但尽快迁移到新名字以避免未来可能存在的冲突

- `old_name` -> `new_name`
- `old_name2` -> `new_name2`
```

---

## Mechanic / Condition 参数表

每个 Mechanic / Condition 的参数**必须**用 Markdown 表格描述。表格列：

|列|说明|
|---|---|
|名字|参数的主名字（MythicMobs 配置中使用的 key）|
|别名|参数的简写别名|
|类型|参数类型（见 `references/type-glossary.md`）|
|默认|默认值|
|说明|参数的功能描述|

### 模板

````markdown
## 全新 Mechanic: `koish_example`

简要描述该 mechanic 的用途。

### 参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`param_name`|`pn`|占位符双精度浮点数|`1.0`|参数的功能描述|
|`flag`|`f`|布尔值|`false`|是否启用某功能|

### 使用示例

```yaml
Skills:
- koish_example{pn=0.5;f=true} @target ~onAttack
```
````

### 调整已有功能的模板

当为已有 Mechanic / Condition 增加新参数时，将参数分为"新增参数"和"已有参数"两个表格：

````markdown
## 调整 Mechanic: `koish_example`

简要描述变更内容。

### 新增参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`new_param`|`np`|枚举 (`A`, `B`)|`A`|新增参数的说明|

### 已有参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`old_param`|`op`|布尔值|`false`|已有参数的说明|
````

---

## 设计说明节（可选）

当 PR 涉及较复杂的设计决策时，在参数表格之后添加 `### 设计说明` 小节，用要点列表描述关键设计：

```markdown
### 设计说明

- **伤害归属不变**：DamageSource 始终基于 caster
- **递归查找**：通过 MythicBukkit API 沿 parent 链递归解析
- **错误处理**：链中断时输出精确到层级的 warn 日志
```
