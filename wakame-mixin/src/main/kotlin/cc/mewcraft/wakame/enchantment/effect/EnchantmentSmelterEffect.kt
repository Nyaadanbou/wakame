package cc.mewcraft.wakame.enchantment.effect

import cc.mewcraft.wakame.enchantment.component.Smelter
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.serialization.codec.BukkitCodecs
import cc.mewcraft.wakame.serialization.codec.KoishCodecs
import cc.mewcraft.wakame.serialization.codec.setOf
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.RecipeChoice

data class EnchantmentSmelterEffect(
    /**
     * Sets whether enchantment will have no effect when crouching.
     */
    val disableOnCrouch: Boolean,
    /**
     * Sound to play on smelting.
     */
    val sound: KoishKey,
    /**
     * List of blocks / items that are immune to the Smelter effect.
     */
    val exemptedItems: Set<Material>,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val DATA_KEY: MetadataKey<Smelter> = metadataKey<Smelter>("enchantment:smelter")

        @JvmField
        val CODEC: Codec<EnchantmentSmelterEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("disable_on_crouch", true).forGetter(EnchantmentSmelterEffect::disableOnCrouch),
                KoishCodecs.KOISH_KEY.fieldOf("sound").forGetter(EnchantmentSmelterEffect::sound),
                BukkitCodecs.MATERIAL_ITEM.setOf().fieldOf("exempted_items").forGetter(EnchantmentSmelterEffect::exemptedItems)
            ).apply(instance, ::EnchantmentSmelterEffect)
        }
    }

    // 可用于魔咒效果的烧炼配方
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe> by lazy {
        Bukkit.getServer().recipeIterator().asSequence()
            .filterIsInstance<FurnaceRecipe>()
            .filterTo(HashSet()) {
                val choice = it.inputChoice
                if (choice is RecipeChoice.MaterialChoice) {
                    choice.choices.all { it !in exemptedItems }
                } else {
                    false // TODO #365: 暂不支持非 MaterialChoice
                }
            }
    }

    override fun apply(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().put(
            DATA_KEY, Smelter(
                disableOnCrouch,
                sound,
                registeredFurnaceRecipes
            )
        )
    }

    override fun remove(entity: LivingEntity, level: Int, slot: ItemSlot) {
        entity.metadata().remove(DATA_KEY)
    }
}