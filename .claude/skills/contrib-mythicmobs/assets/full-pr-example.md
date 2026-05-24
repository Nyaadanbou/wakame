# 完整 PR 示例

以下是一个完整的 PR 描述示例，展示了 Mechanic 调整（含新增参数 + 已有参数 + 使用示例 + 设计说明）的标准格式：

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
