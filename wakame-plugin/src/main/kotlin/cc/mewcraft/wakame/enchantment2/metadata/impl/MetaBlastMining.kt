package cc.mewcraft.wakame.enchantment2.metadata.impl

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.enchantment2.component.BlastMining
import cc.mewcraft.wakame.enchantment2.effect.EnchantmentBlastMiningEffect
import cc.mewcraft.wakame.enchantment2.metadata.EnchantmentMeta
import com.github.quillraven.fleks.EntityComponentContext

class MetaBlastMining : EnchantmentMeta<EnchantmentBlastMiningEffect, BlastMining> {

    override fun make(effect: EnchantmentBlastMiningEffect): BlastMining {
        return BlastMining(effect.explodeLevel)
    }

    context(EntityComponentContext)
    override fun apply(entity: FleksEntity, value: BlastMining) {
        entity.configure {
            it += value
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: FleksEntity) {
        entity.configure {
            it -= BlastMining
        }
    }

}