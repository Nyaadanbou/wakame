package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.enchantment2.component.Smelter
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.serialization.codec.BukkitCodecs
import cc.mewcraft.wakame.serialization.codec.KoishCodecs
import cc.mewcraft.wakame.serialization.codec.setOf
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import org.bukkit.Material
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
    val sound: Identifier,
    /**
     * List of blocks / items that are immune to the Smelter effect.
     * TODO #365: 支持自定义物品 (使用 Set<ItemRef>)
     */
    val exemptedItems: Set<Material>,
) : EnchantmentListenerBasedEffect {

    companion object {

        @JvmField
        val CODEC: Codec<EnchantmentSmelterEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("disable_on_crouch", true).forGetter(EnchantmentSmelterEffect::disableOnCrouch),
                KoishCodecs.IDENTIFIER.fieldOf("sound").forGetter(EnchantmentSmelterEffect::sound),
                BukkitCodecs.MATERIAL.setOf().fieldOf("exempted_items").forGetter(EnchantmentSmelterEffect::exemptedItems)
            ).apply(instance, ::EnchantmentSmelterEffect)
        }

    }

    // 可用于魔咒效果的烧炼配方
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe> by lazy {
        SERVER.recipeIterator().asSequence()
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

    context(EntityComponentContext)
    override fun apply(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it += Smelter(
                disableOnCrouch,
                sound,
                registeredFurnaceRecipes
            )
        }
    }

    context(EntityComponentContext)
    override fun remove(entity: Entity, level: Int, slot: ItemSlot) {
        entity.configure {
            it -= Smelter
        }
    }

}