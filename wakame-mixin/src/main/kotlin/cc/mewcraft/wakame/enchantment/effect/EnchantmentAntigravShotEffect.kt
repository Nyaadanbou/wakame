package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.ecs.configure
import cc.mewcraft.wakame.enchantment.component.AntigravShot
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec

data object EnchantmentAntigravShotEffect : EnchantmentListenerBasedEffect {

    @JvmField
    val CODEC: Codec<EnchantmentAntigravShotEffect> = Codec.unit { this }

    context(_: EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += AntigravShot
        }
    }

    context(_: EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= AntigravShot
        }
    }

}
