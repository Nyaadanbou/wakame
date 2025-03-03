package cc.mewcraft.wakame.shadow.loot

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.entries.TagEntry

@ClassTarget(TagEntry::class)
internal interface ShadowTagEntry : Shadow {
    @get:Field
    @get:Target("tag")
    val tag: TagKey<Item>
}