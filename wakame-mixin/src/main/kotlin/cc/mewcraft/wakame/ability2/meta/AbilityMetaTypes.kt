package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.ability2.component.Blink
import cc.mewcraft.wakame.ability2.component.MultiJump
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.typeTokenOf
import com.github.quillraven.fleks.Component

data object AbilityMetaTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val BLINK: AbilityMetaType<Blink> = typeOf("blink")

    @JvmField
    val MULTI_JUMP: AbilityMetaType<MultiJump> = typeOf("multi_jump")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [AbilityMetaType]
     */
    private inline fun <reified T : Component<T>> typeOf(id: String, block: AbilityMetaType.Builder<T>.() -> Unit = {}): AbilityMetaType<T> {
        val type = AbilityMetaType.builder(typeTokenOf<T>()).apply(block).build()
        return type.also { BuiltInRegistries.ABILITY_META_TYPE.add(id, it) }
    }
}