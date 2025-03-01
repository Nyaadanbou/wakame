package cc.mewcraft.wakame.shadow.loot

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable

@ClassTarget(LootTable::class)
internal interface ShadowLootTable : Shadow {
    @get:Field
    @get:Target("pools")
    val pools: List<LootPool>
}