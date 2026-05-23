package cc.mewcraft.wakame.catalog.enchantment

import io.papermc.paper.registry.tag.TagKey
import org.bukkit.inventory.ItemType

/**
 * 一个 `enchantable/...` 物品标签的条目.
 *
 * 用于模式 c (按物品类型查询魔咒) 中表示一类可附魔的物品.
 */
data class EnchantableTagEntry(
    /** tag value 去掉 `enchantable/` 前缀后的部分, 如 `sword`, `mining`. */
    val tagValue: String,
    /** 原始 TagKey, 用于查询. */
    val fullTagKey: TagKey<ItemType>,
)
