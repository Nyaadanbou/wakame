package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.util.MojangStack
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ResolvableProfile
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaPersistentProfile(
    @Setting(nodeFromParent = true)
    val profile: ResolvableProfile,
) : ItemMetaEntry<ResolvableProfile> {

    override fun randomized(): Boolean {
        return false
    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<ResolvableProfile> {
        return ItemMetaResult.of(profile)
    }

    override fun write(value: ResolvableProfile, itemstack: MojangStack) {
        itemstack.set(DataComponents.PROFILE, profile)
    }
}