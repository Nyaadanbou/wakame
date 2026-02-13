package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKey
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.inventory.ItemStack

/**
 * 此标签系统并非原版的标签系统.
 * Koish 的标签系统不会影响原版数据包标签的内容, 是一套额外的标签系统.
 * 该系统目的是能够方便地利用原版的物品标签, 并支持自定义物品.
 *
 * 例如:
 * Koish 添加了新的木板物品 koish:new_plank, 在该物品配置文件中添加了 minecraft:planks 标签.
 * 同时将原版使用 #minecraft:planks 标签的配方屏蔽, 手动替换成使用 minecraft:planks 标签的 Koish 配方.
 * 即可如原版般在配方系统中使用标签, 简化编写配方配置文件的工作量.
 * TODO 自动识别数据包标签并注册替换相应原版配方
 */
@Init(InitStage.POST_WORLD)
object ItemTagManager {

    /**
     * `标签唯一标识 -> 物品引用集合` 的映射.
     */
    private val map: MutableMap<KoishKey, MutableSet<ItemRef>> = mutableMapOf()

    /**
     * 检查特定物品是否属于某个标签.
     * 标签不存在时返回 false.
     */
    fun ItemStack.isTagged(tagId: KoishKey): Boolean {
        return map[tagId]?.contains(ItemRef.create(this)) == true
    }

    /**
     * 获取特定标签中全部物品引用的集合.
     * 标签不存在时返回空集合.
     */
    fun getValues(tagId: KoishKey): Set<ItemRef> {
        return map[tagId] ?: emptySet()
    }

    /**
     * 重新载入标签数据.
     */
    fun reload() {
        loadTags()
    }

    /**
     * 载入标签数据.
     */
    fun loadTags() {
        map.clear()

        // 载入原版数据包中的标签.
        val dataPackTags = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags
        for (tag in dataPackTags) {
            val tagId = tag.tagKey().key()
            // 理论上不会出现空 ItemRef, 但保险起见还是使用 mapNotNull
            val itemRefs = tag.values().mapNotNull { ItemRef.create(it.key()) }
            map.computeIfAbsent(tagId) { mutableSetOf() }.addAll(itemRefs)
        }

        // 载入 Koish 物品(非套皮) 配置文件指定的标签.
        for (koishItem in BuiltInRegistries.ITEM) {
            // 从注册表读取的物品, 创建的 ItemRef 必然不为空.
            val itemRef = ItemRef.create(koishItem.id) ?: continue
            val tagIds = koishItem.properties[ItemPropTypes.ITEM_TAG] ?: continue
            tagIds.forEach { tagId ->
                map.computeIfAbsent(tagId) { mutableSetOf() }.add(itemRef)
            }
        }

        // 载入 Koish 套皮物品 配置文件指定的标签.
        for (koishItem in BuiltInRegistries.ITEM_PROXY) {
            // 从注册表读取的物品, 创建的 ItemRef 必然不为空.
            val itemRef = ItemRef.create(koishItem.id) ?: continue
            val tagIds = koishItem.properties[ItemPropTypes.ITEM_TAG] ?: continue
            tagIds.forEach { tagId ->
                map.computeIfAbsent(tagId) { mutableSetOf() }.add(itemRef)
            }
        }

        LOGGER.info("Loaded ${map.size} item tags")
    }
}