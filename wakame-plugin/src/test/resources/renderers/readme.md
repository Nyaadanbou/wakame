# 物品渲染器

*物品渲染器* 负责渲染物品的样子, 也就是负责修改物品的提示框, 使其正确反应物品上面的具体信息 (特别是组件
`minecraft:custom_data` 中的信息).

要修改一个物品的样子, 实际上就是修改一个物品的特定组件.

被修改的物品组件至少包括这些:

- `minecraft:item_name`
- `minecraft:custom_name`
- `minecraft:lore`

除此之外, 还包括所有包含 `show_in_tooltip` 字段的组件, 例如: `minecraft:attribute_modifiers`.

有些(原版)组件的信息我们不希望展示在物品的提示框中, 这时我们可以将 `show_in_tooltip` 设置为 `false`.

## 配置文件

```
renderers/
├── crafting/
│   ├── formats.yml
│   └── layout.yml
├── standard/
│   ├── formats.yml
│   └── layout.yml
└── ...
```

`renderers` 文件夹下的每个一级文件夹存放的是一个单独的 *渲染器*. 每个渲染器单独运行, 互不干扰.

每个渲染器下面有两个文件: `formats.yml` 和 `layout.yml`. 下面将详细介绍这两个文件的作用.

### `formats.yml`

`formats.yml` 文件存放的是渲染器的 *格式*, 定义了一个数据如何转换为用户可读的形式.

概念上可以把这个文件的作用, 理解为如何把下面这样的数据 (NBT):

```nbtt
"elements": {
	raw: [1, 2, 3]
}
```

转换为一个这样的数据 (Text Component):

```text
元素: 水, 火, 风 
```

### `layout.yml`

`layout.yml` 文件存放的是渲染器的 *布局*, 定义了物品提示框上的不同内容之间的相对顺序.
例如你想让 “元素” 在物品提示框中始终显示在 “等级” 的上面, 那么就可以修改这个文件达成效果.

每一个 `layout.yml` 文件都有一个 `primary` 节点, 数据类型是一个列表 (字符串).
这个 `primary` 节点就是定义相对顺序的“主要节点”, 列表中的每一个元素必须遵循语法.

语法说明:

`[...]` 需要替换成用户输入!

```
(fixed)[文本内容或留空]
  始终渲染的内容

(fixed:[namespace])[文本内容或留空]
  仅当下面存在 [namespace] 命名空间下的内容时才渲染

(fixed:*)[文本内容或留空]
  仅当下面存在任意命名空间下的内容时才渲染

(default:'[文本内容]')[namespace:value]
  为其添加“默认值” - 当源数据不存在时，改用 *指定的文本内容* 替换

(default:blank)[namespace:value]
  为其添加“默认值” - 当源数据不存在时，改用 *空行* 替换

[namespace]:[value]
  一个占位符, 用于指定特定内容的相对位置;
  如果占位符不存在, 那么对应内容将不渲染
```
