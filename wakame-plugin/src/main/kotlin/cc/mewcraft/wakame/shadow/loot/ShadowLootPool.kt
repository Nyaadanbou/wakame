package cc.mewcraft.wakame.shadow.loot

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer

@ClassTarget(LootPool::class)
interface ShadowLootPool : Shadow {
    @get:Field
    @get:Target("entries")
    val entries: List<LootPoolEntryContainer>
}