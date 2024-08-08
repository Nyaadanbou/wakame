package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream
import kotlin.properties.Delegates

/**
 * 定制词条栏*核心*的过程, 封装了一次定制所需要的所有状态.
 */
internal class SimpleModdingSession(
    override val viewer: Player,
    input: NekoStack,
    override val replaceMap: ModdingSession.ReplaceMap,
) : ModdingSession, KoinComponent {
    private val logger: Logger by inject()

    override val inputItem: NekoStack by NekoStackDelegates.readOnly(input)

    override var outputItem: NekoStack by NekoStackDelegates.copyOnWrite(input)

    override var confirmed: Boolean by Delegates.observable(false) { _, _, new ->
        logger.info("Core modding session's confirmed status updated: $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }
        logger.info("Core modding session's frozen status updated: $new")
        return@vetoable true
    }

    override fun reforge(): ModdingSession.Result {
        // 永远在克隆上进行操作
        val clone = inputItem
        // 根据玩家当前的输入, 修改每个词条栏
        val cells = clone.components.get(ItemComponentTypes.CELLS)?.builder() ?: throw IllegalArgumentException("Null cells")
        for ((id, recipeSession) in replaceMap) {
            val inputCore = recipeSession.input?.components?.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped
            if (inputCore != null) {
                // 重新赋值变量为一个新对象
                cells.modify(id) { it.setCore(inputCore) }
            }
        }
        clone.components.set(ItemComponentTypes.CELLS, cells.build())
        // 更新 output
        outputItem = clone
        // 返回修改后的物品
        return Result(clone)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> =
        Stream.of(
            ExaminableProperty.of("viewer", viewer.name),
            ExaminableProperty.of("input", inputItem),
            ExaminableProperty.of("output", outputItem),
        )

    override fun toString(): String =
        toSimpleString()

    class Result(
        modded: NekoStack,
    ) : ModdingSession.Result {
        override val item: NekoStack by NekoStackDelegates.readOnly(modded)
    }

    class Replace(
        override val id: String,
        override val rule: ModdingTable.CellRule,
        override val display: Display,
    ) : ModdingSession.Replace {
        override var input: NekoStack? = null

        override fun test(replacement: Core): ModdingSession.Replace.TestResult {
            // TODO 检查权限
            val input = input

            val itemElements = input?.components?.get(ItemComponentTypes.ELEMENTS)
            if (rule.requireElementMatch && itemElements != null && replacement is CoreAttribute) {
                val coreElement = replacement.element
                if (coreElement != null) {
                    val any = itemElements.elements.contains(coreElement)
                    if (!any) {
                        return Result.failure("Element mismatch")
                    }
                }
            }

            val cell = input?.components?.get(ItemComponentTypes.CELLS)?.get(id)
            if (cell != null) {
                val modCount = cell.getReforgeHistory().modCount
                if (modCount >= rule.modLimit) {
                    return Result.failure("Mod count exceeds limit")
                }
            }

            val any = rule.acceptedCores.any { it.test(replacement) }
            if (any) {
                return Result.success("Test OK")
            } else {
                return Result.failure("Test failed")
            }
        }

        class Display(
            override val name: Component,
            override val lore: List<Component>,
        ) : ModdingSession.Replace.Display {
            override fun apply(item: ItemStack) {
                item.editMeta {
                    it.itemName(name)
                    it.lore(lore)
                }
            }
        }

        class Result
        private constructor(
            override val successful: Boolean,
            override val message: String,
        ) : ModdingSession.Replace.TestResult {
            companion object {
                fun success(message: String): Result {
                    return Result(true, message)
                }

                fun failure(message: String): Result {
                    return Result(false, message)
                }
            }
        }
    }

    class ReplaceMap : ModdingSession.ReplaceMap {
        private val map: MutableMap<String, ModdingSession.Replace> = HashMap()

        override val size: Int
            get() = map.size

        override fun get(id: String): ModdingSession.Replace? {
            return map[id]
        }

        override fun set(id: String, replace: ModdingSession.Replace) {
            map[id] = replace
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun getInputItems(): List<ItemStack> {
            return map.values.mapNotNull { it.input?.unsafe?.handle }
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> {
            return map.iterator()
        }
    }
}