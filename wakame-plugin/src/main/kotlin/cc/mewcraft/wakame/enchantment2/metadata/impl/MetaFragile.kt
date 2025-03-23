package cc.mewcraft.wakame.enchantment2.metadata.impl

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.enchantment2.component.Fragile
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentFragileEffect
import cc.mewcraft.wakame.enchantment2.metadata.EnchantmentMeta
import com.github.quillraven.fleks.EntityComponentContext

class MetaFragile : EnchantmentMeta<EnchantmentFragileEffect, Fragile> {

    override fun make(effect: EnchantmentFragileEffect): Fragile {
        return Fragile(effect.multiplier)
    }

    context(EntityComponentContext)
    override fun apply(entity: FleksEntity, value: Fragile) {
        entity.configure {
            it += value
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: FleksEntity) {
        entity.configure {
            it -= Fragile
        }
    }

}