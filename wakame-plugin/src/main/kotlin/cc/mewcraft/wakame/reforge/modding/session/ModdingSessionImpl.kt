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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * 定制词条栏*核心*的过程, 封装了一次定制所需要的所有状态.
 */
internal class CoreModdingSession(
    override val viewer: Player,
    input: NekoStack,
    override val recipeSessions: ModdingSession.RecipeSessionMap<Core>,
) : ModdingSession<Core>, KoinComponent {
    private val logger: Logger by inject()

    private val _input: NekoStack = input
    override val input: NekoStack
        get() = _input.clone()

    private var _output: NekoStack? = null
    override var output: NekoStack?
        get() = _output
        set(value) {
            _output = value
            logger.info("Core modding session's output updated")
        }
    private var _confirmed: Boolean = false
    override var confirmed: Boolean
        get() = _confirmed
        set(value) {
            _confirmed = value
            logger.info("Core modding session's confirmed status updated: $value")
        }

    private var frozen: Boolean = false
    override fun frozen(): Boolean = frozen
    override fun freeze() {
        frozen = true
    }

    override fun reforge(): ModdingSession.Result {
        // 永远在克隆上进行操作
        val clone = input
        // 根据玩家当前的输入, 修改每个词条栏
        var cells = clone.components.get(ItemComponentTypes.CELLS) ?: throw IllegalArgumentException("Null cells")
        for ((id, recipeSession) in recipeSessions) {
            val inputCore = recipeSession.input?.components?.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped
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

    class Result(modded: NekoStack) : ModdingSession.Result {
        override val copy: NekoStack = modded.clone()
    }

    class RecipeSession(
        override val id: String,
        override val rule: ModdingTable.CellRule,
        override val display: Display,
    ) : ModdingSession.RecipeSession<Core> {
        override var input: NekoStack? = null

        override fun test(replacement: Core): ModdingSession.RecipeSession.TestResult {
            // TODO 检查权限
            // TODO 检查定制次数是否达到上限
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
        ) : ModdingSession.RecipeSession.Display {
            override fun apply(item: ItemStack) {
                item.editMeta {
                    it.itemName(name)
                    it.lore(lore)
                }
            }
        }

        class TestResult(
            override val isSuccess: Boolean,
            override val resultType: String,
        ) : ModdingSession.RecipeSession.TestResult
    }

    class RecipeSessionMap : ModdingSession.RecipeSessionMap<Core> {
        private val map: MutableMap<String, ModdingSession.RecipeSession<Core>> = HashMap()

        override val size: Int
            get() = map.size

        override fun get(id: String): ModdingSession.RecipeSession<Core>? {
            return map[id]
        }

        override fun put(id: String, recipeSession: ModdingSession.RecipeSession<Core>) {
            map[id] = recipeSession
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun getInputItems(): List<ItemStack> {
            return map.values.mapNotNull { it.input?.handle }
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.RecipeSession<Core>>> {
            return map.iterator()
        }
    }
}

/**
 * 定制词条栏*诅咒*的过程, 封装了一次定制所需要的所有状态.
 */
internal class CurseModdingSession(
    override val viewer: Player,
    input: NekoStack,
    override val recipeSessions: ModdingSession.RecipeSessionMap<Curse>,
) : ModdingSession<Curse>, KoinComponent {
    private val logger: Logger by inject()

    private val _input: NekoStack = input
    override val input: NekoStack
        get() = _input.clone()

    private var _output: NekoStack? = null
    override var output: NekoStack?
        get() = _output
        set(value) {
            _output = value
            logger.info("Curse modding session's output updated")
        }

    private var _confirmed: Boolean = false
    override var confirmed: Boolean
        get() = _confirmed
        set(value) {
            _confirmed = value
            logger.info("Curse modding session's confirmed status updated: $value")
        }

    private var frozen: Boolean = false
    override fun frozen(): Boolean = frozen
    override fun freeze() {
        frozen = true
    }

    override fun reforge(): ModdingSession.Result {
        // 永远在克隆上进行操作
        val clone = input
        // 根据玩家当前的输入, 修改每个词条栏
        var cells = clone.components.get(ItemComponentTypes.CELLS) ?: throw IllegalArgumentException("Null cells")
        for ((id, recipeSession) in recipeSessions) {
            val inputCurse = recipeSession.input?.components?.get(ItemComponentTypes.PORTABLE_CURSE)?.wrapped
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

    class Result(modded: NekoStack) : ModdingSession.Result {
        override val copy: NekoStack = modded.clone()
    }

    class RecipeSession(
        override val id: String,
        override val rule: ModdingTable.CellRule,
        override val display: Display,
    ) : ModdingSession.RecipeSession<Curse> {
        override var input: NekoStack? = null

        override fun test(replacement: Curse): ModdingSession.RecipeSession.TestResult {
            // TODO 检查权限
            // TODO 检查定制次数是否达到上限
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
        ) : ModdingSession.RecipeSession.Display {
            override fun apply(item: ItemStack) {
                item.editMeta {
                    it.itemName(name)
                    it.lore(lore)
                }
            }
        }

        class TestResult(
            override val isSuccess: Boolean,
            override val resultType: String,
        ) : ModdingSession.RecipeSession.TestResult
    }

    class RecipeSessionMap : ModdingSession.RecipeSessionMap<Curse> {
        private val map: HashMap<String, ModdingSession.RecipeSession<Curse>> = HashMap()

        override val size: Int
            get() = map.size

        override fun get(id: String): ModdingSession.RecipeSession<Curse>? {
            return map[id]
        }

        override fun put(id: String, recipeSession: ModdingSession.RecipeSession<Curse>) {
            map[id] = recipeSession
        }

        override fun contains(id: String): Boolean {
            return map.containsKey(id)
        }

        override fun getInputItems(): List<ItemStack> {
            return map.values.mapNotNull { it.input?.handle }
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.RecipeSession<Curse>>> {
            return map.iterator()
        }
    }
}
