# Slots

Slots，中文叫做「词条栏」。「词条栏」可以看作一个容器，里面存放着具体的东西，比如属性和技能。

「词条栏」这一概念贯穿于多个系统之间，包含但不仅限于物品的NBT结构、物品的生成、配置文件的结构。

从顶层设计来看，我们把词条栏分为两大类：`TemplateSlot` 与 `InstanceSlot`。这两都继承顶层接口 `Slot`。

## Slot

`Slot` 是一个顶层接口，代表一个词条栏。

## TemplateSlot

`TemplateSlot` 代表一个词条栏可能出现的所有状态。它用来代表物品的生成，以及物品的配置文件。例如它会出现在插件数据库里的“物品”上。打引号是因为数据库里的“物品”准确应该叫做「物品模板」。在特定的语境下，应该特别区分「物品」和「物品模板」的区别。

## InstanceSlot

`InstanceSlot` 代表一个状态已经确定了的词条栏。从接口之间的关系来说，它可以看成是 `TemplateSlot` 多个状态中的一个。从代码的使用角度来看，用户可以调用 `TemplateSlot` 的某个函数来让其产生一个 `InstanceSlot`。这有点像母鸡生蛋的感觉。目前 `InstanceSlot` 只会出现在世界里的实际物品上（注意区别于只存在于数据库里的「物品模板」）。

# Attributes

## AttributeModifier 的顶层设计哲学

关于一个对象所提供的 AttributeModifier 到底应该用什么 uuid 的问题，我做了如下思考和设计。

每个提供 AttributeModifier 的对象都有一个独特的 uuid。
我们规定，该对象提供的 AttributeModifier 的 uuid 将和该对象本身的 uuid 一致。
由于一个对象上不允许有多个同类属性（的不同值/相同值）， 因此对于玩家身上的任何一个 AttributeInstance 来说，一个对象所提供的
AttributeModifier 的 uuid 是肯定不与其他对象提供的 AttributeModifier 相同的。
这直接提高了添加/移除一个属性的 AttributeModifier 的效率，也刚好能发挥这个架构的全部潜力。
例如，未来如果还想要给玩家添加一个任意来源的 AttributeModifier，比如说来自特殊药剂的效果，
那么这个 AttributeModifier 的 uuid 就可以用药剂本身的 uuid。

# NBT Specifications of WakaItemStack

```
Compound('wakame')
  String('ns'): 'short_sword' // 物品的 namespace
  String('id'): 'demo' // 物品的 id
  Compound('meta')
    Byte('lvl'): 12 // 等级
    Byte('rarity'): 0 // 稀有度
    Byte('set'): 0 // 铭刻
    ByteArray('elem'): [1, 2] // 元素（可能根本不需要）
    Compound('skin') // 皮肤
      Byte('id'): 35 // 标识
      LongArray('owner'): [114L, 514L] // 所有者，储存为 [mostSigBits, leastSigBits]
  Compound('slots') // 词条栏
    Compound('a') // 词条栏的 id
      String('id'): 'attribute:attack_damage' // 词条的 id
      Boolean('reforgeable'): true
      Boolean('overridable'): false
      Compound('data') // 词条的元数据
        Short('min'): 10
        Short('max'): 15
    Compound('b')
      String('id'): 'attribute:element_attack_damage'
      Boolean('reforgeable'): false
      Boolean('overridable'): false
      Compound('data')
        Short('min'): 10
        Short('max'): 15
        Byte('element'): 2
    Compound('c')
      String('id'): 'attribute:attack_damage_rate'
      Boolean('reforgeable'): true
      Boolean('overridable'): false
      Compound('data'):
        Short('value'): 0.2
    Compound('d')
      String('id'): 'ability:dash'
      Boolean('reforgeable'): false
      Boolean('overridable'): false
      Compound('data')
        Byte('mana'): 12
        Byte('cooldown'): 4
        Byte('damage'): 8
    Compound('e')
      String('id'): 'ability:blink'
      Boolean('reforgeable'): false
      Boolean('overridable'): false
      Compound('data')
        Byte('mana'): 12
        Byte('cooldown'): 10
        Byte('distance'): 16
  Compound('conditions') // 词条栏使用条件
    String('b'): 'condition_a' // TODO 单纯一个字符串不足以定位条件
    String('c'): 'condition_b'
    String('d'): 'condition_c'
  Compound('stats') // 物品统计数据
    Compound('boss_kill_count')
      Boolean('visible'): 1
      Int('混沌骑士'): 1
      Int('gojira'): 2
      Int('wither'): 3
      Int('skeletonking'): 11
      Int('enderdragon'): 4 // 数量不为 0 才会储存在 NBT
      Int('sister'): 0 // 数量为 0 的计数实际不会储存
    Compound('creep_kill_count')
      Boolean('visible'): 0
      Int('zombie'): 5 // 数量不为 0 才会储存在 NBT
      Int('spider'): 2
    Compound('damage_contribution')
      Boolean('visible'): 0
      Int('neutral'): 122
      Int('fire'): 2344
      Int('wind'): 23
      Int('water'): 1120
    Compound('reforge_cost')
      Boolean('visible'): 0
      Int('cost'): 47283
```
