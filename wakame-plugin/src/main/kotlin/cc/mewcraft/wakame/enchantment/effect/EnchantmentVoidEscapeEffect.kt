package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.VoidEscape
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import org.bukkit.entity.LivingEntity

data object EnchantmentVoidEscapeEffect : EnchantmentListenerBasedEffect {
    @JvmField
    val DATA_KEY: MetadataKey<VoidEscape> = metadataKey("enchantment:void_escape")

    @JvmField
    val CODEC: Codec<EnchantmentVoidEscapeEffect> = MapCodec.unitCodec(this)

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(DATA_KEY, VoidEscape)
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}