# Slots

Slots，中文叫做「词条栏」。「词条栏」可以看作一个容器，里面存放着具体的东西，比如属性和技能。

「词条栏」这一概念贯穿于多个系统之间，包含但不仅限于物品的NBT结构、物品的生成、配置文件的结构。

从顶层设计来看，我们把词条栏分为两大类：`TemplateSlot` 与 `InstanceSlot`。这两都继承顶层接口 `Slot`。

## Slot

`Slot` 是一个顶层接口，代表一个词条栏。

## TemplateSlot

`TemplateSlot` 代表一个词条栏可能出现的所有状态。它用来代表物品的生成，以及物品的配置文件。例如它会出现在插件数据库里的“物品”上。打引号是因为数据库里的“物品”准确应该叫做「物品模板」。在特定的语境下，应该特别区分「物品」和「物品模板」的区别。

## InstanceSlot

`InstanceSlot` 代表一个状态已经确定了的词条栏。从接口之间的关系来说，它可以看成是 `TemplateSlot` 多个状态中的一个。从代码的使用角度来看，用户可以调用 `TemplateSlot` 的某个函数来让其产生一个 `InstanceSlot`。这有点像母鸡生蛋的感觉。
~~目前 `InstanceSlot` 只会出现在世界里的实际物品上（注意区别于只存在于数据库里的「物品模板」）~~。

# Attributes

## AttributeModifier 的顶层设计哲学

关于一个对象所提供的 AttributeModifier 到底应该用什么 UUID 的问题，我做了如下思考和设计。

首先，每个提供 AttributeModifier 的对象都有一个独特的 UUID。
同时我们规定，该对象提供的 AttributeModifier 的 UUID 将和该对象本身的 UUID 一致。
由于一个对象上不允许有多个同类属性（的不同值/相同值）， 因此对于玩家身上的任何一个 AttributeInstance 来说，一个对象所提供的
AttributeModifier 的 UUID 是肯定不与其他对象提供的 AttributeModifier 相同的。
这直接提高了添加/移除一个属性的 AttributeModifier 的效率，也刚好能发挥这个架构的全部潜力。
例如，未来如果还想要给玩家添加一个**任意来源**的 AttributeModifier，比如说来自特殊药剂的效果，
那么这个 AttributeModifier 的 UUID 就可以用药剂物品本身的 UUID。

# Item Composition

`WakameItemStack` 是游戏世界内的表现，

# NBT Specifications

`WakameItem` 是物品在模板中的抽象，而 `WakaItemStack` 是物品在游戏世界内的抽象。

## Namespace & Value

其中最基础的信息是「物品是什么」，也就是「物品对应哪一个配置文件」。这些信息存在于所有物品上。

| Path (wakame.) | Description                     |
|----------------|:--------------------------------|
| ns             | namespace, i.e. the folder name |
| id             | value, i.e., the file name      |

## Metadata

其次是物品的「元数据」，包含描述该物品的额外信息。这些信息并不存在于每个物品上。根据具体的物品种类，每种信息都可能不存在。

| Path (wakame.meta.) | Description  |
|---------------------|--------------|
| name                | display name |
| lore                | lore         |
| lvl                 | level        |
| rarity              | rarity       |
| kizami              | kizami       |
| elem                | elem         |
| skin                | skin         |
| skin_owner          | skin owner   |

## Slots

词条栏。

## Conditions

词条栏的激活条件。

## Statistics

物品的统计数据。

## Example

以下是一个 `WakameItemStack` 的 NBT 格式实例。请注意所有字符串都区分大小写。

```
Compound('wakame')
  String('ns'): 'short_sword' // 物品的 namespace
  String('id'): 'demo' // 物品的 id
  Compound('meta')
    String('name'): '<JSON text>'
    List('(String) lore'):
      String(None): '<JSON text>'
      String(None): '<JSON text>'
      String(None): '<JSON text>'
    Byte('lvl'): 12b // 等级
    Byte('rarity'): 0b // 稀有度
    ByteArray('kizami'): [0b, 3b] // 铭刻
    ByteArray('elem'): [1b, 2b] // 元素（可能根本不需要）
    Short('skin'): 35s // 皮肤 ID
    IntArray('skin_owner'): [0, 1, 2, 3] // 皮肤所有者的 UUID，储存为 4 个 Int
  Compound('slots') // 词条栏
    Compound('a') // 词条栏的 id
      Boolean('can_reforge'): true // 词条栏是否可重铸
      Boolean('can_override'): false // 词条栏是否可由玩家覆盖
      Compound('tang') // 词条栏的内容（词条）
        String('id'): 'attribute:attack_damage' // 词条的 id
        Short('min'): 10s // 词条的元数据，下面两个也是
        Short('max'): 15s
        Byte('elem'): 0b
        Byte('op'): 0b // 属性修饰符的操作模式，可以不存在，默认 0b
      Compound('reforge') // 重铸的元数据（可能会用到）
        Byte('success'): 5b // 重铸成功的次数
        Byte('failure'): 1b // 重铸失败的次数
      Compound('condition') // 词条栏的解锁条件
        String('id'): 'condition:entity_kills' // 条件的种类，这里是实体击杀
        String('index'): 'demo_bosses_1' // 要求的击杀种类
        Short('count'): 18s // 要求的击杀数量
    Compound('b')
      Boolean('can_reforge'): false
      Boolean('can_override'): false
      Compound('tang')
        String('id'): 'attribute:attack_damage'
        Short('min'): 10s
        Short('max'): 15s
        Byte('elem'): 2b
        Byte('op'): 0b
      Compound('reforge')
        // 空的，意为 0
      Compound('condition')
        // 空的，意为没有条件限制
    Compound('c')
      Boolean('can_reforge'): true
      Boolean('can_override'): false
      Compound('tang'):
        String('id'): 'attribute:attack_damage_rate'
        Float('value'): 0.2f
        Byte('elem'): 2b
        Byte('op'): 0b
      Compound('reforge')
        // 空的，意为 0
      Compound('condition')
        // 空的，意为没有条件限制
    Compound('d')
      Boolean('can_reforge'): false
      Boolean('can_override'): false
      Compound('tang')
        String('id'): 'ability:dash'
        Byte('mana'): 12b
        Byte('cooldown'): 4b
        Byte('damage'): 8b
      Compound('reforge')
      Compound('condition')
    Compound('e')
      Boolean('can_reforge'): false
      Boolean('can_override'): false
      Compound('tang')
        String('id'): 'ability:blink'
        Byte('mana'): 12b
        Byte('cooldown'): 10b
        Byte('distance'): 16b
      Compound('reforge')
      Compound('condition')
    Compound('f')
      Boolean('can_reforge'): false
      Boolean('can_override'): false
      Compound('tang') // 表示一个空槽
      Compound('reforge')
      Compound('condition')
  Compound('stats') // 物品统计数据
    Compound('entity_kills')
      Short('minecraft:zombie'): 5s
      Short('minecraft:spider'): 2s
      Short('minecraft:wither'): 3s
      Short('minecraft:ender_dragon'): 4s // 数量不为 0 将储存在 NBT
      Short('mythicmobs:gojira'): 2s
      Short('mythicmobs:skeleton_king'): 11s
      Short('mythicmobs:sister'): 0s // 数量为 0 则实际不会储存在 NBT
    Compound('peak_damage')
      Short('fire'): 12s
      Short('metal'): 5s
      Short('water'): 30s
      Short('earth'): 0s // 没有造成过伤害的元素，不需要写进 NBT，意为 0
      Short('wood'): 0s
    Compound('reforge')
      Byte('count'): 38b // 重铸总次数
      Short('cost'): 32767s // 重铸总花费
```
