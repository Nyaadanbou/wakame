## 名词

**渲染**

修改一个 ItemStack 的各项数据, 使其变成期望的样子.
被修改的数据包括但不限于下列物品组件:

- `minecraft:item_name`
- `minecraft:custom_name`
- `minecraft:lore`

以及所有具有 `show_in_tooltip` 字段的物品组件.

## 核心逻辑

对于一个 ItemStack,
其底层的萌芽数据是储存在 `minecraft:custom_data` 中的;
其玩家看到的样子是储存在 `minecraft:item_name`, `minecraft:custom_name` 和 `minecraft:lore` 中的.

所谓“渲染”一个物品, 就是把 `minecraft:custom_data` 中的数据转换成对应的 `minecraft:item_name`, `minecraft:custom_name` 和
`minecraft:lore`.

也就是说, 一个最基本的渲染过程可以看成是几个函数:

1. (minecraft:custom_data) -> minecraft:item_name
2. (minecraft:custom_data) -> minecraft:custom_name
3. (minecraft:custom_data) -> minecraft:lore

但要满足所有的实际需求, 函数的输入不能仅仅是 `minecraft:custom_data`.

这是因为 `minecraft:custom_data` 中的数据是*结果*, 而有的时候我们想把*过程*给玩家看.

例如展示在物品合成站中的物品,
如果只是单纯的把 `minecraft:custom_data` 中的数据展示出来,
那么玩家看到的永远是这个物品所有可能性中的其中一种.
玩家(特别是新人)可能会觉得这物品就只有这一种状态.

## Use case 1: 渲染一个已知的 NekoStack

任何操作都应该在原 NekoStack 的克隆上进行.

提供一个新的函数: NekoStack#display(...), 需要传入一个 DSL.
该 DSL 应该包含所有可能的渲染操作, 例如是否保留萌芽 NBT, 是否渲染物品名, 所使用的 PipelineHandler ...
函数最终会返回一个新的 NekoStack 实例, 实现了所有 DSL 设定的渲染结果.

## Use case 2: 把一个物品 id 转换成一个 ItemStack

初衷是希望配置文件里指定一个萌芽物品的 id,
就能够把指定的萌芽物品直接当作菜单的图标.

> 目前来看初衷应该就是全部的需求了.

物品 id 是萌芽物品的 id.
这个 id 会被转换成一个 NekoItem 实例.

如果对应的 NekoItem 不存在 (也就是 id 无效),
那么始终会生成一个默认的 ItemStack.
这个默认的 ItemStack 可以是一个屏障,
并把给定的萌芽物品 id 作为其物品名.
这样可以快速的发现配置文件中的错误.

类的设计

class MenuItemDelegate
constructor(id: String)
fun configureDisplay(...)
fun getItemStack(): ItemStack
fun copy(): MenuItemDelegate

函数 fun configureDisplay(...) 需要传入一个 DSL, 用来指定如何渲染该物品.
物品是否需要 CustomModelData, 是否保留原来的物品名, 是否保留原来的物品描述,
是否需要用户完全自行接管物品的名字和描述内容, 全由 DSL 来指定.
这个 DSL 跟 Use case 1 中的 DSL 是一样的.

然后, 只需要调用函数 fun getItemStack()
就能得到一个期望中的 ItemStack 实例.
该函数应该每次都返回新的实例.
如果需要缓存, 用户自行处理.

函数 fun copy() 用来生成一个深拷贝.
也就是说使用函数 fun configureDisplay() 设置的渲染参数也会被拷贝.

懒加载机制, 必须懒加载 NekoItem, 避免跟现有的 Initializer 依赖图系统直接耦合.
