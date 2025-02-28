package cc.mewcraft.wakame.shadow.loot

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.core.Holder
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.entries.LootItem

@ClassTarget(LootItem::class)
internal interface ShadowLootItem : Shadow {
    @get:Field
    @get:Target("item")
    val item: Holder<Item>
}