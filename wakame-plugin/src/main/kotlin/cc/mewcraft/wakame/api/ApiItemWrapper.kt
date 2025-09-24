package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.block.KoishBlock
import cc.mewcraft.wakame.entity.player.koishLevel
import cc.mewcraft.wakame.item.KoishItem
import cc.mewcraft.wakame.item.KoishStackGenerator
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.name
import cc.mewcraft.wakame.util.adventure.plain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import cc.mewcraft.wakame.api.item.KoishItem as INekoItem

internal class ApiItemWrapper(
    private val item: KoishItem,
) : INekoItem {
    override fun getId(): Key {
        return item.id
    }

    override fun getBlock(): KoishBlock? {
        TODO("Not yet implemented")
    }

    override fun getName(): Component {
        return item.name
    }

    override fun getPlainName(): String {
        return item.name.plain
    }

    override fun createItemStack(amount: Int): ItemStack {
        val itemStack = KoishStackGenerator.generate(item, ItemGenerationContext(item, 0f, 1)).apply { this.amount = amount }
        return itemStack
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack {
        val itemStack = if (player == null) {
            KoishStackGenerator.generate(item, ItemGenerationContext(item, 0f, 1))
        } else {
            KoishStackGenerator.generate(item, ItemGenerationContext(item, 0f, player.koishLevel))
        }
        return itemStack.apply { this.amount = amount }
    }
}