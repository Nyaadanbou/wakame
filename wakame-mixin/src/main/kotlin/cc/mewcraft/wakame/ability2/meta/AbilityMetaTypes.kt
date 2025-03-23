package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.ability2.component.MultiJump
import cc.mewcraft.wakame.ability2.meta.impl.MetaMultiJump
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.TypeSerializerCollection

object AbilityMetaTypes {

    // ------------
    // 注册表
    // ------------

    @JvmField
    val MULTI_JUMP: AbilityMetaType<MetaMultiJump, MultiJump> = typeOf("multi_jump")

    // ------------
    // 方便函数
    // ------------

    /**
     * @param id 将作为注册表中的 ID
     * @param block 用于配置 [AbilityMetaType]
     */
    private inline fun <reified U : AbilityMetaEntry<V>, V> typeOf(id: String, block: AbilityMetaType.Builder<U, V>.() -> Unit = {}): AbilityMetaType<U, V> {
        val type = AbilityMetaType.builder<U, V>(typeTokenOf<U>()).apply(block).build()
        return type.also { KoishRegistries2.ABILITY_META_TYPE.add(id, it) }
    }

    // ------------
    // 内部实现
    // -–----------

    /**
     * 获取一个 [TypeSerializerCollection] 实例, 可用来序列化 [AbilityMetaContainer] 中的数据类型.
     *
     * 返回的 [TypeSerializerCollection] 仅包含在这里显式声明的序列化操作, 不包含隐式声明的例如 [Int].
     *
     * 该 [TypeSerializerCollection] 的序列化代码被调用的时机发生在 *加载物品配置文件* 时.
     */
    internal fun directSerializers(): TypeSerializerCollection {
        val collection = TypeSerializerCollection.builder()

        KoishRegistries2.ABILITY_META_TYPE.fold(collection) { acc, type ->
            val serializers = type.serializers
            if (serializers != null) acc.registerAll(serializers) else acc
        }

        return collection.build()
    }
}