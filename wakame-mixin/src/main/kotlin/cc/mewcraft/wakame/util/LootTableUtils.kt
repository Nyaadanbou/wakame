package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.LOGGER
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

object LootTableUtils {
    /**
     * 以战利品表在数据包中的路径为唯一标识符.
     * 从 `id` 获取 Mojang 战利品表实例.
     * @return 对应的战利品表实例, 未找到则返回 [net.minecraft.world.level.storage.loot.LootTable.EMPTY].
     */
    fun getMojangLootTable(id: String): MojangLootTable {
        return MINECRAFT_SERVER.reloadableRegistries().getLootTable(
            ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.parse(id)
            )
        )
    }

    /**
     * 以战利品表在数据包中的路径为唯一标识符.
     * 从 `id` 获取 Mojang 战利品表实例.
     * @return 对应的战利品表实例, 未找到则返回 [net.minecraft.world.level.storage.loot.LootTable.EMPTY] 并警告.
     */
    fun getMojangLootTableWithWarning(id: String): MojangLootTable {
        return getMojangLootTable(id).apply {
            if (this == MojangLootTable.EMPTY) {
                LOGGER.warn("Can't find loot table: '$id', use an empty loot table instead")
            }
        }
    }
}