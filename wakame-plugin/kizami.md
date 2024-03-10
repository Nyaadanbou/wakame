# class Kizami

不可变的数据类。
在加载配置文件时就创建好。

主要用于识别铭刻，也储存了铭刻的一些UI信息。

# class KizamiMap

状态将动态变化的类。

每个玩家都有一个 KizamiMap 的实例。
记录了该玩家当前所拥有的 Kizami 种类，以及每个种类的 Kizami 有多少个。
i.e. 本质是一个映射 Kizami -> Int

KizamiMap 是一个需要被实时更新的实例，需要监听特定事件来完成更新。
例如：
- 当玩家切换主手手持的物品时
- 当背包里的物品状态发生变化时

# class KizamiEffect

不可变的数据类。
在加载配置文件时就创建好。

记录了铭刻的具体效果。
目前提供以下效果：
- 属性（修饰符）
- 技能

单独没有用，将在 KizamiInstance 里被调用。

# class KizamiDefinition

不可变的数据类。
在加载配置文件时就创建好。

用于查询：给定数量 N，分别对应哪些效果。
i.e. 本质是一个映射 Int -> KizamiEffect

例如：
- 为 1 时，提供 KizamiEffect A
- 为 2 时，提供 KizamiEffect B
- 为 3 时，提供 KizamiEffect C

单独没有用，将在 KizamiInstance 里被调用。

# class KizamiInstance

不可变的数据类。
在加载配置文件时就创建好。

负责将
- Kizami
- KizamiEffect
- KizamiDefinition
组合在一起的类，以表示一个概念层面上完整的铭刻。

成员变量：
val kizami: Kizami
val definition: KizamiDefinition

成员函数：
- 给定 kizami 和数量，返回一个 KizamiEffect

