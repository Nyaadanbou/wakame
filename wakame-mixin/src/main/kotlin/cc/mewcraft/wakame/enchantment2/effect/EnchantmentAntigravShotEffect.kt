package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.enchantment2.component.AntigravShot
import cc.mewcraft.wakame.item.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec

data object EnchantmentAntigravShotEffect : EnchantmentListenerBasedEffect {

    @JvmField
    val CODEC: Codec<EnchantmentAntigravShotEffect> = Codec.unit { this }

    context(EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += AntigravShot
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= AntigravShot
        }
    }

}
