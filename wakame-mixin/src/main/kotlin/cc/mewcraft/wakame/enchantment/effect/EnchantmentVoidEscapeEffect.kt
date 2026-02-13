package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.ecs.configure
import cc.mewcraft.wakame.enchantment.component.VoidEscape
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec

data object EnchantmentVoidEscapeEffect : EnchantmentListenerBasedEffect {

    @JvmField
    val CODEC: Codec<EnchantmentVoidEscapeEffect> = MapCodec.unitCodec(this)

    context(_: EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += VoidEscape
        }
    }

    context(_: EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= VoidEscape
        }
    }
}