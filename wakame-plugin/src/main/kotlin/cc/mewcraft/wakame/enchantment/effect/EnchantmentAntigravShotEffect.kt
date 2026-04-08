package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.AntigravShot
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import org.bukkit.entity.LivingEntity

data object EnchantmentAntigravShotEffect : EnchantmentListenerBasedEffect {

    @JvmField
    val DATA_KEY: MetadataKey<AntigravShot> = metadataKey("enchantment:antigrav_shot")

    @JvmField
    val CODEC: Codec<EnchantmentAntigravShotEffect> = MapCodec.unitCodec(this)

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(DATA_KEY, AntigravShot)
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}
