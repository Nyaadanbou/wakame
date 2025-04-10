package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.UseCooldown
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.util.*

@ConfigSerializable
data class MetaCooldownGroup(
    @Setting(nodeFromParent = true)
    val id: Identifier,
) : ItemMetaEntry<Identifier> {

    override fun make(context: Context): ItemMetaResult<Identifier> {
        return ItemMetaResult.of(id)
    }

    override fun write(value: Identifier, itemstack: MojangStack) {
        itemstack.set(DataComponents.USE_COOLDOWN, UseCooldown(.1f, Optional.of(PaperAdventure.asVanilla(id))))
    }
}