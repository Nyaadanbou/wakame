# 现有附魔效果一览

|效果 ID|Effect 类|Component 类|System 类|类型|说明|
|---|---|---|---|---|---|
|`koish:attributes`|`EnchantmentAttributeEffect`|—|`EnchantmentAttributeSystem`|Special|提供自定义属性修饰器|
|`koish:smelter`|`EnchantmentSmelterEffect`|`Smelter`|`EnchantmentSmelterSystem`|Listener|挖掘方块后自动熔炼掉落物|
|`koish:blast_mining`|`EnchantmentBlastMiningEffect`|`BlastMining`|`EnchantmentBlastMiningSystem`|Listener|挖掘产生爆炸，破坏范围内方块|
|`koish:fragile`|`EnchantmentFragileEffect`|`Fragile`|`EnchantmentFragileSystem`|Listener|物品耐久度消耗倍率增加|
|`koish:veinminer`|`EnchantmentVeinminerEffect`|`Veinminer` + `VeinminerChild`|`EnchantmentVeinminerSystem`|Listener|BFS 连锁采矿同种矿物|
|`koish:antigrav_shot`|`EnchantmentAntigravShotEffect`|`AntigravShot`|`EnchantmentAntigravShotSystem`|Listener|射出的弹射物不受重力|
|`koish:void_escape`|`EnchantmentVoidEscapeEffect`|`VoidEscape`|`EnchantmentVoidEscapeSystem`|Listener|虚空伤害时随机传送到安全位置|
|`koish:range_mining`|`EnchantmentRangeMiningEffect`|`RangeMining` + `RangeMiningChild`|`EnchantmentRangeMiningSystem`|Listener|范围挖掘 (宽×高×深)|
|`koish:auto_replant`|`EnchantmentAutoReplantEffect`|`AutoReplant`|`EnchantmentAutoReplantSystem`|Listener|右键成熟作物自动收获并补种|
