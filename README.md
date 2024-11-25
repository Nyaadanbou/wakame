## 自定义附魔 & 数据包

萌芽的自定义附魔实现采取了一种“怎么方便怎么实现”的策略.

萌芽完全使用了数据包来实现自定义附魔的新增/移除/获得途径/等级需求等等.
服务端必须安装好正确的数据包, 萌芽的自定义附魔才会出现在游戏中.
也就是说, 数据包里必须包含萌芽新增的所有附魔实例.

对于附魔的其他方面, 例如提供的萌芽属性/萌芽机制, 萌芽通过监听玩家的背包变化来实现.
这些方面仅用数据包无法实现与萌芽系统比较好的联动效果, 因此萌芽会完全接管这方面.

## 资源包分发服务器的正确用法

如果想使用萌芽内置的 HTTP 服务器分发资源包 (`self_host`), 必须添加以下变量到服务端启动参数的 `-jar` 之前:

```
-Djava.net.preferIPv4Stack=true
```

## AttributeModifier 的设计哲学

关于一个对象所提供的 AttributeModifier 到底应该用什么 UUID 的问题，我做了如下思考和设计。

首先，每个提供 AttributeModifier 的对象都有一个独特的 UUID。
同时我们规定，该对象提供的 AttributeModifier 的 UUID 将和该对象本身的 UUID 一致。
由于一个对象上不允许有多个同类属性（的不同值/相同值）， 因此对于玩家身上的任何一个 AttributeInstance 来说，一个对象所提供的
AttributeModifier 的 UUID 是肯定不与其他对象提供的 AttributeModifier 相同的。
这直接提高了添加/移除一个属性的 AttributeModifier 的效率，也刚好能发挥这个架构的全部潜力。
例如，未来如果还想要给玩家添加一个**任意来源**的 AttributeModifier，比如说来自特殊药剂的效果，
那么这个 AttributeModifier 的 UUID 就可以用药剂物品本身的 UUID。

## 萌芽物品的 NBT 规范

```
Compound('wakame')
    // 物品的命名空间及 ID
    String('id'): 'weapon:short_sword'
    
    // 物品的变体
    // 可以不存在, 即为 `0`
    String('variant'): 0
    
    // 物品的组件
    // 以下组件按照字母顺序排序
    Compound('components')
        
        // 可以释放技能?
        Compound('castable')

        // 盲盒(未鉴定)
        Compound('crate')
            String('key'): 'common:demo'

        // 物品元素
        Compound('elements')
            ByteArray('raw'): [1b, 2b]
        
        // ... 以及其他组件
```
