package cc.mewcraft.wakame.reforge.modding.session

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.reforge.modding.config.ModdingTable
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 定制词条栏*核心*的过程, 封装了一次定制所需要的所有状态.
 */
internal class CoreModdingSession(
    override val viewer: Player,
    override val input: NekoStack,
    override val recipes: ModdingSession.RecipeMap<Core>,
) : ModdingSession<Core> {
    override var output: NekoStack? = null
    override var frozen: Boolean = false
    override var confirmed: Boolean = false

    override fun reforge(): ModdingSession.Result {
        // 永远在克隆上进行操作
        val clone = input.clone()
        // 根据玩家当前的输入, 修改每个词条栏
        var cells = clone.components.get(ItemComponentTypes.CELLS) ?: throw IllegalArgumentException("Null cells")
        for ((id, recipe) in recipes) {
            val inputCore = recipe.input?.components?.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped
            if (inputCore != null) {
                // 重新赋值变量为一个新对象
                cells = cells.modify(id) { it.setCore(inputCore) }
            }
        }
        clone.components.set(ItemComponentTypes.CELLS, cells)
        // 更新 output
        output = clone
        // 返回修改后的物品
        return Result(clone)
    }

    override fun toString(): String {
        return toSimpleString()
    }

    class Result(
        override val modded: NekoStack,
    ) : ModdingSession.Result

    class Recipe(
        override val id: String,
        override val rule: ModdingTable.CellRule,
        override val display: Display,
    ) : ModdingSession.Recipe<Core> {
        override var input: NekoStack? = null

        override fun test(replacement: Core): ModdingSession.Recipe.TestResult {
            val any = rule.acceptedCores.any { it.test(replacement) }
            if (any) {
                return TestResult(true, "Test OK")
            } else {
                return TestResult(false, "Test failed")
            }
        }

        class Display(
            override val name: Component,
            override val lore: List<Component>,
        ) : ModdingSession.Recipe.Display {
            override fun apply(item: ItemStack) {
                item.editMeta {
                    it.displayName(name)
                    it.lore(lore)
                }
            }
        }

        class TestResult(
            override val isSuccess: Boolean,
            override val resultType: String,
        ) : ModdingSession.Recipe.TestResult
    }

    class RecipeMap(
        private val map: MutableMap<String, ModdingSession.Recipe<Core>>,
    ) : ModdingSession.RecipeMap<Core> {
        override val size: Int
            get() = map.size

        override fun get(id: String): ModdingSession.Recipe<Core>? {
            return map[id]
        }

        override fun put(id: String, recipe: ModdingSession.Recipe<Core>) {
            map[id] = recipe
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun getInputItems(): List<ItemStack> {
            return map.values.mapNotNull { it.input }.map { it.handle }
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Recipe<Core>>> {
            return map.iterator()
        }
    }
}

/**
 * 定制词条栏*诅咒*的过程, 封装了一次定制所需要的所有状态.
 */
internal class CurseModdingSession(
    override val viewer: Player,
    override val input: NekoStack,
    override val recipes: RecipeMap,
) : ModdingSession<Curse> {
    override var output: NekoStack? = null
    override var frozen: Boolean = false
    override var confirmed: Boolean = false

    override fun reforge(): ModdingSession.Result {
        // 永远在克隆上进行操作
        val clone = input.clone()
        // 根据玩家当前的输入, 修改每个词条栏
        var cells = clone.components.get(ItemComponentTypes.CELLS) ?: throw IllegalArgumentException("Null cells")
        for ((id, recipe) in recipes) {
            val inputCurse = recipe.input?.components?.get(ItemComponentTypes.PORTABLE_CURSE)?.wrapped
            if (inputCurse != null) {
                // 重新赋值变量为一个新对象
                cells = cells.modify(id) { it.setCurse(inputCurse) }
            }
        }
        clone.components.set(ItemComponentTypes.CELLS, cells)
        // 更新 output
        output = clone
        // 返回修改后的物品
        return Result(clone)
    }

    override fun toString(): String {
        return toSimpleString()
    }

    class Result(
        override val modded: NekoStack,
    ) : ModdingSession.Result

    class Recipe(
        override val id: String,
        override val rule: ModdingTable.CellRule,
        override val display: Display,
    ) : ModdingSession.Recipe<Curse> {
        override var input: NekoStack? = null

        override fun test(replacement: Curse): ModdingSession.Recipe.TestResult {
            val any = rule.acceptedCurses.any { it.test(replacement) }
            if (any) {
                return TestResult(true, "Test OK")
            } else {
                return TestResult(false, "Test failed")
            }
        }

        class Display(
            override val name: Component,
            override val lore: List<Component>,
        ) : ModdingSession.Recipe.Display {
            override fun apply(item: ItemStack) {
                item.editMeta {
                    it.displayName(name)
                    it.lore(lore)
                }
            }
        }

        class TestResult(
            override val isSuccess: Boolean,
            override val resultType: String,
        ) : ModdingSession.Recipe.TestResult
    }

    class RecipeMap(
        private val map: MutableMap<String, ModdingSession.Recipe<Curse>>,
    ) : ModdingSession.RecipeMap<Curse> {
        override val size: Int
            get() = map.size

        override fun get(id: String): ModdingSession.Recipe<Curse>? {
            return map[id]
        }

        override fun put(id: String, recipe: ModdingSession.Recipe<Curse>) {
            map[id] = recipe
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun getInputItems(): List<ItemStack> {
            return map.values.mapNotNull { it.input }.map { it.handle }
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Recipe<Curse>>> {
            return map.iterator()
        }
    }
}
