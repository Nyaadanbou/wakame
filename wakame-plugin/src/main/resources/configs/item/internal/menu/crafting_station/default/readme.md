## Slot Display 示例用法
该图标对应合成站里一个特定[配方]中的特定物品输入.

该图标的原始信息由特定配方中的特定物品输入本身决定,
因此也不需要指定 base (即便指定了也不会被真的用到).

这里用于指定物品的提示框的整体布局, 也就是可以在物品原本的信息上添加一些额外的东西.
参考下面的示例:
物品最终的 `minecraft:item_name` 是 slot_display_name, 其中的 <item_name> 会被替换成物品原本的 `minecraft:item_name`.
物品最终的 `minecraft:lore` 是 slot_display_lore, 其中的 <item_lore> 会被替换成物品原本的 `minecraft:lore`.

```yaml
# 额外占位符
# <item_name>: 原始物品的 `minecraft:item_name`
slot_display_name: "<item_name>"

# 额外占位符
# {item_lore}: 原始物品的 `minecraft:lore`
slot_display_lore:
- "{item_lore}"
```
