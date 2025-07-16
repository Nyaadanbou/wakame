package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.ecs.configure
import cc.mewcraft.wakame.enchantment2.component.Veinminer
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import cc.mewcraft.wakame.serialization.codec.BukkitCodecs
import cc.mewcraft.wakame.serialization.codec.KoishCodecs
import cc.mewcraft.wakame.serialization.codec.setOf
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.Material

@JvmRecord
data class EnchantmentVeinminerEffect(
    val longestChainMining: LevelBasedValue,
    val allowedBlockTypes: Set<Material>,
    val blockBreakSound: Identifier,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentVeinminerEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("longest_chain_mining").forGetter(EnchantmentVeinminerEffect::longestChainMining),
                BukkitCodecs.MATERIAL.setOf().fieldOf("allowed_block_types").forGetter(EnchantmentVeinminerEffect::allowedBlockTypes),
                KoishCodecs.IDENTIFIER.fieldOf("block_break_sound").forGetter(EnchantmentVeinminerEffect::blockBreakSound)
            ).apply(instance, ::EnchantmentVeinminerEffect)
        }

    }

    context(_: EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += Veinminer(
                longestChainMining.calculate(level).toInt().toShort().coerceIn(1, 128),
                allowedBlockTypes,
                blockBreakSound,
            )
        }
    }

    context(_: EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= Veinminer
        }
    }

}