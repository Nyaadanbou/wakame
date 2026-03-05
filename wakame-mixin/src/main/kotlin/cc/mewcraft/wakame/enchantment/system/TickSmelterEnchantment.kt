package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.effect.EnchantmentSmelterEffect
import cc.mewcraft.wakame.util.adventure.sound
import cc.mewcraft.wakame.util.metadata.metadata
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.sound.Sound
import org.bukkit.Particle
import org.bukkit.block.Container
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.FurnaceRecipe

/**
 * @see cc.mewcraft.wakame.enchantment.effect.EnchantmentSmelterEffect
 */
object TickSmelterEnchantment : Listener {

    @EventHandler
    fun on(event: BlockDropItemEvent) {
        val player = event.player
        val metadata = player.metadata()
        val smelter = metadata.getOrNull(EnchantmentSmelterEffect.DATA_KEY) ?: return
        if (smelter.disableOnCrouch && player.isSneaking) return
        val blockState = event.blockState
        if (blockState is Container) return

        var anySmelted = false
        event.items.forEach { drop ->
            anySmelted = anySmelted || drop.smelt(smelter.registeredFurnaceRecipes)
        }
        if (!anySmelted) return

        // 播放粒子
        val blockCenter = event.block.location.toCenterLocation()
        ParticleBuilder(Particle.FLAME).location(blockCenter)
            .source(player)
            .count(3)
            .receivers(8)
            .offset(0.25, 0.25, 0.25)
            .extra(0.05)
            .spawn()

        // 播放声音
        player.playSound(sound {
            source(Sound.Source.BLOCK)
            type(smelter.smeltingSound)
        }, blockCenter.x, blockCenter.y, blockCenter.z)
    }

    private fun Item.smelt(recipes: HashSet<FurnaceRecipe>): Boolean {
        val stack = itemStack
        val recipe = recipes.find { it.inputChoice.test(stack) } ?: return false
        val smeltedStack = recipe.result.apply {
            // Copy amount of the origin drop item. Fixes Fortune compatibility and overall original drop amount.
            // Hopefully furnaces will not require multiple input items in the future, otherwise we'll suck here :D
            this.amount = stack.amount
        }
        itemStack = smeltedStack
        return true
    }

}