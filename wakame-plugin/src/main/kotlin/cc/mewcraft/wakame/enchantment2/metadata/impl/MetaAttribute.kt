package cc.mewcraft.wakame.enchantment2.metadata.impl

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentAttributeEffect
import cc.mewcraft.wakame.enchantment2.metadata.EnchantmentMeta
import com.github.quillraven.fleks.EntityComponentContext

class MetaAttribute : EnchantmentMeta<EnchantmentAttributeEffect, Nothing> {

    override fun make(effect: EnchantmentAttributeEffect): Nothing {
        throw UnsupportedOperationException()
    }

    context(EntityComponentContext)
    override fun apply(entity: FleksEntity, value: Nothing) {

    }

    context(EntityComponentContext)
    override fun remove(entity: FleksEntity) {

    }

}