---
name: mythicmobs-hook-pr
description: >
  Guide for writing pull request descriptions when adding or modifying
  MythicMobs Mechanic, Condition, ItemDrop, or Placeholder in the
  wakame-hook-mythicmobs module. Use this skill when creating PRs for
  the mythicmobs hook, or when asked to document MythicMobs integration
  changes, mechanic parameters, condition parameters, drops, or placeholders.
---

# MythicMobs Hook PR 书写规范

本技能规范了在 `wakame-hooks/wakame-hook-mythicmobs` 模块中添加或修改 Mechanic、Condition、ItemDrop、Placeholder 时，PR 描述应遵循的格式。

---

## 1. 总体结构

PR 描述按变更类型分节，每种类型使用 `##` 标题。节的排列顺序：

1. 改名（如有）
2. 调整已有功能（如有）
3. 全新功能

每节标题格式：

|场景|标题格式|
|---|---|
|批量改名|`## Mechanic 改名` / `## Condition 改名` / `## ItemDrop 改名`|
|调整已有功能|`## 调整 Mechanic: \`koish_xxx\`` / `## 调整 Condition: \`koish_xxx\``|
|全新功能|`## 全新 Mechanic: \`koish_xxx\`` / `## 全新 Condition: \`koish_xxx\``|

---

## 2. 改名节

当批量重命名 mechanic/condition/drop 时，使用列表格式：

```markdown
## Mechanic 改名

为平滑迁移，原名字依然能使用，但尽快迁移到新名字以避免未来可能存在的冲突

- `old_name` -> `new_name`
- `old_name2` -> `new_name2`
```

---

## 3. Mechanic / Condition 参数表

每个 Mechanic / Condition 的参数**必须**用 Markdown 表格描述。表格列：

|列|说明|
|---|---|
|名字|参数的主名字（MythicMobs 配置中使用的 key）|
|别名|参数的简写别名|
|类型|参数类型（见下方类型词汇表）|
|默认|默认值|
|说明|参数的功能描述|

### 3.1 模板

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

### 3.2 调整已有功能的模板

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

## 4. Placeholder 节

Placeholder 不使用参数表格，而是描述其功能和用法：

````markdown
## 全新占位符: `<koish.xxx>`

返回某个数值的描述。

### 参数

|名字|类型|说明|
|---|---|---|
|`arg1`|字符串|参数说明|

### 示例

```yaml
- message{m="Value: <koish.xxx.arg1>"} @self
```
````

---

## 5. ItemDrop 节

````markdown
## 全新 ItemDrop: `koish_xxx`

简要描述该 drop 的用途。

### 参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`param`|`p`|字符串|无|参数说明|
````

---

## 6. 设计说明节（可选）

当 PR 涉及较复杂的设计决策时，在参数表格之后添加 `### 设计说明` 小节，用要点列表描述关键设计：

```markdown
### 设计说明

- **伤害归属不变**：DamageSource 始终基于 caster
- **递归查找**：通过 MythicBukkit API 沿 parent 链递归解析
- **错误处理**：链中断时输出精确到层级的 warn 日志
```

---

## 7. 类型词汇表

在参数表的"类型"列中，使用以下统一术语：

|术语|对应 MythicMobs API|
|---|---|
|布尔值|`mlc.getBoolean`|
|整数|`mlc.getInteger`|
|浮点数|`mlc.getFloat`|
|双精度浮点数|`mlc.getDouble`|
|字符串|`mlc.getString`|
|占位符整数|`mlc.getPlaceholderInteger` / `PlaceholderInt`|
|占位符双精度浮点数|`mlc.getPlaceholderDouble` / `PlaceholderDouble`|
|枚举 (`值1`, `值2`, ...)|`mlc.getEnum`|
|范围双精度浮点数|`RangedDouble`|
|字符串列表|`mlc.getStringList`|

---

## 8. 完整示例

以下是一个完整的 PR 描述示例：

````markdown
## 调整 Mechanic: `koish_damage_attribute_map`

新增属性来源配置，支持从施法者的 parent（召唤者）链获取属性来计算伤害。

### 新增参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`source`|`src`|枚举 (`CASTER`, `PARENT`)|`CASTER`|属性来源。`CASTER` 使用施法者自身属性，`PARENT` 使用施法者的 parent 属性|
|`source_depth`|`sd`|整数 (1~8)|`1`|当 `source=PARENT` 时，沿 parent 链向上查找的层数|

### 已有参数

|名字|别名|类型|默认|说明|
|---|---|---|---|---|
|`percent`|`p`|占位符双精度浮点数|`1.0`|最终伤害数值占面板数值的百分比|
|`ignore_blocking`|`ib`|布尔值|`false`|是否无视格挡|
|`ignore_resistance`|`ir`|布尔值|`false`|是否无视抗性提升|
|`ignore_absorption`|`ia`|布尔值|`false`|是否无视伤害吸收|
|`knockback`|`kb`|布尔值|`true`|是否造成击退效果|

### 使用示例

```yaml
# 使用施法者自身属性 (默认)
Skills:
- koish_damage_attribute_map{p=0.8} @target ~onAttack

# 使用施法者的 parent 的属性
Skills:
- koish_damage_attribute_map{source=PARENT;p=1.0} @target ~onAttack

# 使用往上2层 parent 的属性
Skills:
- koish_damage_attribute_map{src=PARENT;sd=2} @target ~onAttack
```

### 设计说明

- **伤害归属不变**：DamageSource 始终基于 caster（施法者），只有属性来源可切换
- **递归查找 parent 链**：通过 `MythicBukkit.inst().mobManager.getActiveMob()` 递归解析
- **错误处理**：链中任何一层断裂都会输出精确到层级的 warn 日志
````

---

## 9. 注意事项

1. **表格用最小格式**：不要加多余空格对齐列（遵循项目 Markdown 规范）
2. **MythicMobs 配置中的名字用 snake_case**：新增的 mechanic/condition 统一使用 `koish_` 前缀
3. **使用示例必须包含 YAML 代码块**：展示实际的 MythicMobs 技能配置
4. **改名兼容性说明**：如果旧名字仍然可用，需要在改名节中明确说明
5. **注册点**：新增的 mechanic/condition/drop 需要在 `ConfigListener.kt` 的对应 `when` 分支中注册

