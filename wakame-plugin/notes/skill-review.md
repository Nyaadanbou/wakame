# SkillTrigger

comment 1

`SkillTrigger` 中的 `companion object` 里面定义的两个 `list` 的意义不明确（虽然我知道它是做什么的）。

第一眼看上去这两个 list 记录了有哪些“可用”的 SkillTrigger 的实例，但这应该在 `SkillRegistry` 初始化的时候进行定义，而不是 `SkillTrigger` 这个类型内进行定义。

comment 2

另外，将 `SkillTrigger` 标记为 `sealed interface` 更适合现在的情况。这还能允许 `when` 自动枚举所有的子类。

comment 3

property `id` 的类型应该为 `Byte`，并且在文档里写明该数值的要求（必须为0-9的数字）

# SkillMap


