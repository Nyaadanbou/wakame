package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.reforge.mod.ModdingSession
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ReplaceRender {
    fun render(result: ModdingSession.Replace.Result): ItemStack {
        val ingredient = result.ingredient
        val rendered: ItemStack

        if (result.applicable) {
            // 耗材可用于定制

            if (ingredient == null) {
                // 出现内部错误

                rendered = ItemStack(Material.BARRIER)
                rendered.editMeta { meta ->
                    val name = "<red>内部错误".mini
                    val lore = buildList {
                        add(Component.empty())
                        add("<!i><gray>⤷ 点击以取回".mini)
                    }

                    meta.itemName(name)
                    meta.lore(lore)
                }
            } else {
                // 正常情况

                rendered = ingredient.itemStack
            }
        } else {
            // 耗材不可用于定制

            rendered = ItemStack(Material.BARRIER)
            rendered.editMeta { meta ->
                val name = "<red>无效材料".mini
                val lore = buildList<Component> {
                    addAll(result.description)
                    add(Component.empty())
                    add("<!i><gray>⤷ 点击以取回".mini)
                }

                meta.itemName(name)
                meta.lore(lore)
            }
        }

        return rendered
    }
}