# Attribute

Attribute - 最顶层接口

玩家身上

NumberAttribute 的设计
- 每个玩家身上必须拥有所有的 NumberAttribute（NA） 且为全局默认值
- 物品或其他来源仅为玩家提供 NumberAttributeModifier（NAM）
- 玩家身上的每个 NA 都有其对应的一个或多个 NAM，也可以没有 NAM
- NAM 会被周期性的计算以计算出玩家每个 NA 最终的值
- NAM 会随着玩家的特定操作而变化
