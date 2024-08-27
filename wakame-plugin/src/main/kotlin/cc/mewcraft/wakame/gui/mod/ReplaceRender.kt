package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

internal object ReplaceRender {

    fun render(result: ModdingSession.Replace.Result): ItemStack {
        val ingredient = result.ingredient
        val rendered: ItemStack

        val clickToWithdraw = "<gray>⤷ 点击以取回".mini.removeItalic

        if (result.applicable) {
            // 耗材可用于定制

            if (ingredient == null) {
                // 出现内部错误

                rendered = ItemStack(Material.BARRIER)
                rendered.editMeta { meta ->
                    val name = "<white>结果: <red>内部错误".mini
                    val lore = buildList {
                        add(Component.empty())
                        add(clickToWithdraw)
                    }

                    meta.itemName(name)
                    meta.lore(lore.removeItalic)
                }
            } else {
                // 正常情况

                // FIXME NekoStackDisplay 急急急
                ingredient.erase()
                rendered = ingredient.itemStack
                rendered.editMeta { meta ->
                    val name = "<white>结果: <green>就绪".mini
                    val lore = buildList<Component> {
                        result.getPortableCore()?.provideTooltipLore(RendererSystemName.STANDARD)?.content?.let { addAll(it) }
                        add(Component.empty())
                        add(clickToWithdraw)
                    }

                    meta.displayName(name.removeItalic)
                    meta.lore(lore.removeItalic)
                }
            }
        } else {
            // 耗材不可用于定制

            rendered = ItemStack(Material.BARRIER)
            rendered.editMeta { meta ->
                val name = "<white>结果: <red>无效".mini
                val lore = buildList<Component> {
                    addAll(result.description)
                    add(Component.empty())
                    add(clickToWithdraw)
                }

                meta.itemName(name)
                meta.lore(lore.removeItalic)
            }
        }

        return rendered
    }
}