package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.bridge.serialization.codec.AdventureCodecs
import cc.mewcraft.wakame.bridge.serialization.codec.PaperCodecs
import cc.mewcraft.wakame.bridge.serialization.codec.setOf
import cc.mewcraft.wakame.enchantment.component.Veinminer
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.enchantment.LevelBasedValue
import org.bukkit.Material
import org.bukkit.entity.LivingEntity

@JvmRecord
data class EnchantmentVeinminerEffect(
    val longestChainMining: LevelBasedValue,
    val allowedBlockTypes: Set<Material>,
    val blockBreakSound: KoishKey,
    val period: Long,
) : EnchantmentListenerBasedEffect {
    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<Veinminer> = metadataKey("enchantment:veinminer")

        @JvmField
        val CODEC: Codec<EnchantmentVeinminerEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelBasedValue.CODEC.fieldOf("longest_chain_mining").forGetter(EnchantmentVeinminerEffect::longestChainMining),
                PaperCodecs.MATERIAL_BLOCK.setOf().fieldOf("allowed_block_types").forGetter(EnchantmentVeinminerEffect::allowedBlockTypes),
                AdventureCodecs.KEY_WITH_MINECRAFT_NAMESPACE.fieldOf("block_break_sound").forGetter(EnchantmentVeinminerEffect::blockBreakSound),
                Codec.LONG.optionalFieldOf("period", 3L).forGetter(EnchantmentVeinminerEffect::period),
            ).apply(instance, ::EnchantmentVeinminerEffect)
        }

    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(
            DATA_KEY,
            Veinminer(
                longestChainMining.calculate(level).toInt().toShort().coerceIn(1, 128),
                allowedBlockTypes,
                blockBreakSound,
                period.coerceAtLeast(1),
            )
        )
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}