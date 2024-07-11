# Cells

Cells，中文叫做「词条栏」。「词条栏」可以看作一个容器，里面存放着具体的东西，比如属性和技能。

「词条栏」这一概念贯穿于多个系统之间，包含但不仅限于物品的NBT结构、物品的生成、配置文件的结构。

从顶层设计来看，我们把词条栏分为两大类：`SchemaCell` 与 `BinaryCell`。这两都继承顶层接口 `Cell`。

## Cell

`Cell` 是一个顶层接口，代表一个词条栏。

## SchemaCell

`SchemaCell` 代表一个词条栏可能出现的所有状态。它用来代表物品的生成，以及物品的配置文件。例如它会出现在插件数据库里的“物品”上。打引号是因为数据库里的“物品”准确应该叫做「物品模板」。在特定的语境下，应该特别区分「物品」和「物品模板」的区别。

## BinaryCell

`BinaryCell` 代表一个状态已经确定了的词条栏。从接口之间的关系来说，它可以看成是 `SchemaCell` 多个状态中的一个。从代码的使用角度来看，用户可以调用 `SchemaCell` 的某个函数来让其产生一个 `BinaryCell`。这有点像母鸡生蛋的感觉。
~~目前 `BinaryCell` 只会出现在世界里的实际物品上（注意区别于只存在于数据库里的「物品模板」）~~。

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

## Cells

词条栏。

## Curses

词条栏的诅咒，概念上等同于词条栏激活所需要满足的条件。

## Statistics

物品的统计数据。

## Example

以下是一个 `WakameItemStack` 的 NBT 格式实例。请注意所有字符串都区分大小写。

```
Compound('wakame')
    // 物品的命名空间
    String('namespace'): 'short_sword'
    
    // 物品的路径
    String('path'): 'demo'
    
    // 物品的变体
    String('variant'): 0
    
    // 物品的组件
    // 以下组件按照字母顺序排序
    Compound('components')
    
        // 提供属性加成?
        Compound('attributable')
            // 空

        // 弹药信息
        Compound('arrow')
            // 空
        
        Compound('bow')
            // 空
        
        // 可以释放技能?
        Compound('castable')

        // 词条栏
        Compound('cells')
            // 词条栏的 id
            Compound('a')
                 // 词条栏的内容（词条）
                Compound('core')
                    // 词条的 id
                    String('id'): 'attribute:attack_damage'
                    // 词条的具体设置
                    Short('min'): 10s
                    Short('max'): 15s
                    Byte('elem'): 0b
                    Byte('op'): 0b
                 // 重铸的元数据（可能会用到）
                Compound('reforge')
                    Byte('success'): 5b
                    Byte('failure'): 1b
                // 词条栏的解锁条件
                Compound('curse')
                    // 诅咒的 id
                    String('id'): 'entity_kills'
                    // 诅咒的具体设置
                    String('index'): 'demo_bosses_1'
                    Short('count'): 18s
            Compound('b')
                Compound('core')
                    String('id'): 'attribute:attack_damage'
                    Short('min'): 10s
                    Short('max'): 15s
                    Byte('elem'): 2b
                    Byte('op'): 0b
                Compound('reforge')
                    // 空, 或不存在, 都代表没有重铸数据
                Compound('curse')
                    // 空, 或不存在, 都代表无诅咒
            Compound('c')
                Compound('core'):
                    String('id'): 'attribute:attack_damage_rate'
                    Float('value'): 0.2f
                    Byte('elem'): 2b
                    Byte('op'): 0b
                Compound('reforge')
                Compound('curse')
            Compound('d')
                Compound('core')
                    String('id'): 'ability:dash'
                Compound('reforge')
                Compound('curse')
            Compound('e')
                Compound('core')
                     // 空, 或不存在, 都代表无核心
                Compound('reforge')
                Compound('curse')

        // 盲盒
        Compound('crate')
            String('key'): 'common:demo'
        
        // 自定义名字
        Compound('custom_name')
            String('raw'): '<aqua>The original "displayName"'

        // 物品元素
        Compound('elements')
            ByteArray('raw'): [1b, 2b]

        // 食物
        Compound('food')
            List('(String) skills')
                String(): 'melee:demo'
                String(): 'ranged:demo'

        // 物品名字
        Compound('item_name')
            String('raw'): '<aqua>The name that can't be changed by anvils' 

        // 物品铭刻
        Compound('kizamiz')
            ByteArray('raw'): [0b, 3b]

        // 提供铭刻加成?
        Compound('kizamiable')
            // 空

        // 物品等级
        Compound('level')
            Byte('raw'): 12b

        // 描述
        Compound('lore')
            List('(String) raw')
                String(): '<red>This is the red first line'
                String(): '<green>This is the green second line'
                String(): '<blue>This is the blue third line'

        // 物品稀有度
        Compound('rarity')
            Byte('raw'): 0b

        // 提供技能加成?
        Compound('skillful')
        
        // 物品皮肤
        Compound('skin')
            Short('raw'): 35s

        // 物品皮肤所有者
        Compound('skin_owner')
            IntArray('raw'): [0, 1, 2, 3] // 皮肤所有者的 UUID，储存为 4 个 Int

        // 物品统计数据
        Compound('stats')
            Compound('entity_kills')
                Short('minecraft:zombie'): 5s
                Short('minecraft:spider'): 2s
                Short('minecraft:wither'): 3s
                Short('minecraft:ender_dragon'): 4s // 数量不为 0 将储存在 NBT
                Short('mythicmobs:gojira'): 2s
                Short('mythicmobs:skeleton_king'): 11s
                Short('mythicmobs:sister'): 0s // 数量为 0 则实际不会储存在 NBT
            Compound('peak_damage')
                Short('neutral'): 55s
                Short('fire'): 5s
                Short('water'): 30s
                Short('wind'): 0s
                Short('earth'): 0s // 没有造成过伤害的元素，不需要写进 NBT，意为 0
                Short('thunder'): 12s
            Compound('reforge')
                Byte('count'): 38b // 重铸总次数
                Short('cost'): 32767s // 重铸总花费
         
        // 作为系统物品
        Compound('system_use')
```
