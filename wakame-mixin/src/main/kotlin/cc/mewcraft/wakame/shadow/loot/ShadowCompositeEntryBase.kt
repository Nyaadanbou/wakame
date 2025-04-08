package cc.mewcraft.wakame.shadow.loot

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer

@ClassTarget(CompositeEntryBase::class)
interface ShadowCompositeEntryBase : Shadow {
    @get:Field
    @get:Target("children")
    val children: List<LootPoolEntryContainer>
}