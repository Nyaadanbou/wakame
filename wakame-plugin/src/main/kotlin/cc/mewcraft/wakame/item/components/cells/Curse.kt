package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentInjections
import cc.mewcraft.wakame.item.components.cells.curses.CurseEmpty
import cc.mewcraft.wakame.item.components.cells.curses.CurseEntityKills
import cc.mewcraft.wakame.item.components.cells.curses.CursePeakDamage
import cc.mewcraft.wakame.registry.CurseRegistry
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个词条栏中的诅咒. 诅咒用于控制[词条栏][Cell]的[核心][Core]是否应该“生效”.
 *
 * 基本概念:
 * - 不生效的词条栏相当于该词条栏不存在, 因此没有任何效果.
 * - 如果诅咒是锁定状态([isLocked] 为 `true`), 则词条栏不应该生效.
 * - 如果诅咒是解锁状态([isLocked]] 为 `false`), 则词条栏应该生效.
 */
interface Curse : Keyed, Examinable, BinarySerializable, TooltipProvider.SingleWithName {
    /**
     * 诅咒的唯一标识.
     */
    override val key: Key

    /**
     * 该诅咒的类型.
     */
    val type: CurseType<*>

    /**
     * 检查诅咒是否为空诅咒. 空诅咒相当于不存在.
     */
    val isEmpty: Boolean

    /**
     * 检查该诅咒是否是 `锁定状态`.
     */
    fun isLocked(context: NekoStack): Boolean

    /**
     * 检查该诅咒是否是 `解锁状态`.
     */
    fun isUnlocked(context: NekoStack): Boolean

    companion object {
        /**
         * 返回一个空的诅咒.
         */
        fun empty(): Curse {
            return CurseEmpty
        }

        /**
         * 构建一个 [Curse].
         */
        fun of(nbt: CompoundTag): Curse {
            if (nbt.isEmpty) {
                // It's an empty binary curse,
                // just return the singleton.
                return CurseEmpty
            }

            val id = nbt.getString(CurseBinaryKeys.CURSE_IDENTIFIER)
            val key = Key(id)
            require(key.namespace() == Namespaces.CURSE)
            val ret = when (key.value()) {
                CurseConstants.ENTITY_KILLS -> CurseEntityKills(nbt)
                CurseConstants.PEAK_DAMAGE -> CursePeakDamage(nbt)
                else -> throw IllegalArgumentException("Failed to parse NBT tag: ${nbt.asString()}")
            }

            return ret
        }
    }
}

/**
 * 代表一个诅咒的类型.
 */
interface CurseType<T : Curse>

/**
 * 一个诅咒的配置文件.
 */
internal class CurseConfig(
    configPath: String,
) {
    val root: ConfigProvider = CurseRegistry.CONFIG.derive(configPath)
    val enabled: Boolean by root.entry<Boolean>("enabled")
    val displayName: Component by root.entry<Component>("display_name")

    inner class SingleTooltip : Examinable {
        val single: String by root.entry<String>("tooltips", "single")

        fun render(): Component {
            return ItemComponentInjections.miniMessage.deserialize(single)
        }

        fun render(resolver: TagResolver): Component {
            return ItemComponentInjections.miniMessage.deserialize(single, resolver)
        }

        fun render(vararg resolver: TagResolver): Component {
            return ItemComponentInjections.miniMessage.deserialize(single, *resolver)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("single", single))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}