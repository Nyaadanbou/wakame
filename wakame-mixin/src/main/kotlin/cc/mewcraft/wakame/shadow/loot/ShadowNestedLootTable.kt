package cc.mewcraft.wakame.shadow.loot

import com.mojang.datafixers.util.Either
import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.NestedLootTable

@ClassTarget(NestedLootTable::class)
interface ShadowNestedLootTable : Shadow {
    @get:Field
    @get:Target("contents")
    val contents: Either<ResourceKey<LootTable>, LootTable>
}