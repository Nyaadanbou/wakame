package cc.mewcraft.wakame.enchantment2.metadata.impl

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.enchantment2.component.AutoMelting
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentAutoMeltingEffect
import cc.mewcraft.wakame.enchantment2.metadata.EnchantmentMeta
import com.github.quillraven.fleks.EntityComponentContext

class MetaAutoMelting : EnchantmentMeta<EnchantmentAutoMeltingEffect, AutoMelting> {

    override fun make(effect: EnchantmentAutoMeltingEffect): AutoMelting {
        return AutoMelting(effect.activated)
    }

    context(EntityComponentContext)
    override fun apply(entity: FleksEntity, value: AutoMelting) {
        entity.configure {
            it += value
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: FleksEntity) {
        entity.configure {
            it -= AutoMelting
        }
    }

}