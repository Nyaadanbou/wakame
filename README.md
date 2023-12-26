# Attributes

## AttributeModifier 的顶层设计哲学

关于一个对象所提供的 AttributeModifier 到底应该用什么 uuid 的问题，我做了如下思考和设计。

每个提供 AttributeModifier 的对象都有一个独特的 uuid。
我们规定，该对象提供的 AttributeModifier 的 uuid 将和该对象本身的 uuid 一致。
由于一个对象上不允许有多个同类属性（的不同值/相同值）， 因此对于玩家身上的任何一个 AttributeInstance 来说，
一个对象所提供的 AttributeModifier 的 uuid 是肯定不与其他对象提供的 AttributeModifier 相同的。
这直接提高了添加/移除一个属性的 AttributeModifier 的效率，也刚好能发挥这个架构的全部潜力。
例如，未来如果还想要给玩家添加一个任意来源的 AttributeModifier，比如说来自特殊药剂的效果，
那么这个 AttributeModifier 的 uuid 就可以用药剂本身的 uuid。

# NBT Specifications of WakaItemStack

```
Compound('wakame')
  String('ns'): 'short_sword' // 物品的 namespace
  String('id'): 'demo' // 物品的 id
  Byte('lvl'): 12 // 等级
  Byte('rarity'): 0 // 稀有度
  Byte('set'): 0 // 铭刻
  Byte('elem'): 1 // 元素（可能根本不需要）
  Compound('skin') // 皮肤
    Byte('id'): 35 // 标识
    LongArray('owner'): [114L, 514L] // 所有者，储存为 [mostSigBits, leastSigBits]
  Compound('slots') // 词条栏
    Compound('a') // 词条栏的 id
      String('id'): 'attribute:attack_damage' // 词条的 id
      Compound('meta') // 词条的元数据
        Short('min'): 10
        Short('max'): 15
    Compound('b')
      String('id'): 'attribute:fire_attack_damage'
      Compound('meta')
        Short('min'): 10
        Short('max'): 15
        Byte('element'): 2
    Compound('c')
      String('id'): 'attribute:attack_damage_rate'
      Compound('meta'):
        Short('value'): 0.2
    Compound('d')
      String('id'): 'ability:dash'
      Compound('meta')
        Byte('mana'): 12
        Byte('cooldown'): 4
        Byte('damage'): 8
    Compound('e')
      String('id'): 'ability:blink'
      Compound('meta')
        Byte('mana'): 12
        Byte('cooldown'): 10
        Byte('distance'): 16
  Compound('reforge') // 重铸数据
    Compound('a') // 词条栏 a 的数据
      Boolean('changeable'): 1
    Compound('b') // 词条栏 b 的数据
      Boolean('changeable'): 0
    Compound('c') // ...
      Boolean('changeable'): 1
    Compound('d')
      Boolean('changeable'): 0
    Compound('e')
      Boolean('changeable'): 0
  Compound('conditions') // 词条栏使用条件
    String('b'): 'condition_a' // TODO 单纯一个字符串不足以定位条件
    String('c'): 'condition_b'
    String('d'): 'condition_c'
```
