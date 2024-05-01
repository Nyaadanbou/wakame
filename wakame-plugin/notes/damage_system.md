未完成

# 概述

对于任意一次伤害，分为“攻击”和“防御”两个阶段，阶段的区分仅取决于伤害修饰的主体

攻击（Damage）阶段：
计算**产生**本次伤害者所有对伤害的修饰，修饰可以是增加伤害也可以是减少伤害
如：武器有火元素伤害强化属性，伤害增加；武器有土元素伤害弱化属性，伤害减少

防御（Defense）阶段：
计算**受到**本次伤害影响者所有对伤害的修饰，这里的修饰同样可增可减
如：玩家有火元素防御属性，受到伤害减少；玩家有“易伤”debuff，伤害增加

# 事件

## WakameDamageEvent

完整的伤害事件，提供：
直接获得最终伤害的方法 #getFinalDamage
伤害的元数据信息 DamageMetaData

### DamageMetaData 伤害元数据(接口)

接口成员 damageValue 表示**攻击阶段**计算所有修饰后的伤害终值
该接口的实现类可能会附加额外信息

# 原版玩家伤害的产生、传递、修饰

## Player#attack(Entity target)

1. 通过玩家的Attributes.ATTACK_DAMAGE获取基础近战攻击力，设置为伤害的初值
2. 计算攻击冷却完成度，修正基础近战攻击力和魔咒攻击力 
   设攻击冷却完成度为 p，则基础近战攻击力会变为原先的 0.2+0.8*p^2 倍，魔咒攻击力会变为原先的 p 倍
   此时伤害值为修正后的基础近战攻击力和魔咒攻击力之和
3. 判定玩家是否会心一击（原版跳劈），若是，伤害值增加对应的百分比（purpur有这项数值的配置项）
4. 传递到接收伤害的实体上，调用对应实体的 hurt(DamageSource source, float amount)方法，amount传入伤害值
   （后续以LivingEntity#hurt为例，hurt方法不同实体的实现不一样，但是只要是LivingEntity的实体应该都会用到LivingEntity#hurt）

## LivingEntity#hurt(DamageSource source, float amount)
1. if (source.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) { amount *= 5.0F; }
   若伤害类型在标签IS_FREEZING下（原版细雪），且受伤的实体类型在标签FREEZE_HURTS_EXTRA_TYPES（原版烈焰人、岩浆怪、炽足兽）下，则伤害值翻5倍
   （疑似是被遗漏的伤害修饰，其他伤害修饰均被移动到LivingEntity#damageEntity0了）
2. 传递到方法LivingEntity#damageEntity0(final DamageSource damagesource, float f)，f传入伤害值

## LivingEntity#damageEntity0(final DamageSource damagesource, float f)
1. 将f的值保留到变量originalDamage中
2. 创建头盔修饰器，若伤害类型在标签DAMAGES_HELMET下，且受伤实体有头盔，修饰伤害值为-f*25%，f=f+修饰伤害值
   （伤害修饰值：浮点数，不是百分比的意思，就是具体加减了伤害多少的值）
3. 创建格挡修饰器，若伤害成功被格挡，修饰伤害值为-f，否则为0.0，f=f+修饰伤害值
4. 创建盔甲修饰器，根据CombatRules中的方法计算，修饰伤害值为x，f=f+修饰伤害值
   部分伤害类型该修饰为0.0，如溺水、魔法、监守者音爆
5. 创建抗性修饰器，设受伤实体的抗性提升药水等级为 k，修饰伤害值为-f*k'*20%，f=f+修饰伤害值
   部分伤害类型该修饰为0.0，如饥饿、虚空、/kill
6. 创建魔法修饰器，根据CombatRules中的方法和实体所穿盔甲上的各种类型的保护附魔的等级计算，修饰伤害值为y，f=f+修饰伤害值
   部分伤害类型该修饰为0.0，如监守者音爆、饥饿
   特别的是，Witch（女巫）重写了这一方法，算完盔甲之后，若伤害类型在标签WITCH_RESISTANT_TO下，则修饰伤害值再减少85%
7. 创建伤害吸收修饰器，根据实体拥有的AbsorptionAmount计算，修饰伤害值为z
8. 将originalDamage和上面所有修饰器的*修饰函数*和*修饰伤害值*组装成EntityDamageEvent，originalDamage将以Base修饰器的形式存在
   EntityDamageEvent#getDamage() 获取的是Base修饰器的修饰伤害值，也就是originalDamage的值
   EntityDamageEvent#getFinalDamage() 获取的是所有修饰器的修饰伤害值之和，包括Base修饰器（显然Base修饰器是正的，其他都是负的）
   EntityDamageEvent#getDamage(@NotNull DamageModifier type) 获取的是特定修饰器的修饰伤害值
   EntityDamageEvent#setDamage(@NotNull DamageModifier type, double damage) 修改各修饰器的修饰伤害值，注意是负值
   EntityDamageEvent#setDamage(double damage) 会修改Base修饰器的修饰伤害值，同时会根据damage缩放原有其他修饰器的修饰伤害值
   组装的过程中还会通过很杂乱的代码计算出意义不明的DamageCause
9. 分发EntityDamageEvent，监听该事件以修改伤害
10. 调用EntityDamageEvent#getFinalDamage()，覆盖掉f的值
11. 进行扣除头盔耐久、扣除盾牌耐久、扣除盔甲耐久、统计信息、进度、扣除黄心等操作
12. 调用LivingEntity#setHealth扣除实体生命值