package cc.mewcraft.wakame.integration.skill

import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import cc.mewcraft.wakame.util.require
import org.bukkit.entity.Player
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 封装了一个外部的机制.
 */
// 开发日记 2025/11/24:
// 目前实现上封装的是 MM 的技能
// 很长一段时间应该都是用 MM 吧?
interface SkillWrapper {

    // 用于序列化
    val type: SkillWrapperType

    /**
     * 释放该机制.
     *
     * @param player 释放者
     */
    fun cast(player: Player)

    /**
     * 一个完整的机制, 以 [id] 唯一识别.
     * 实现上直接用该 [id] 获取对应的机制, 然后释放.
     */
    interface Block : SkillWrapper {
        val id: String

        companion object {
            // 注意这里的子类不是 data class, 所以我们必须写专门的序列化实现
            fun serializer(): TypeSerializer2<Block> = TypeSerializer2 { type, node ->
                SkillIntegration.lookupBlockSkill(node.node("value").require())
            }
        }
    }

    /**
     * 一个内联的机制, 仅储存原始的 [line].
     * 实现上需要先解析 [line] 获取对应机制, 然后释放.
     */
    interface Inline : SkillWrapper {
        val line: String

        companion object {
            // 注意这里的子类不是 data class, 所以我们必须写专门的序列化实现
            fun serializer(): TypeSerializer2<Inline> = TypeSerializer2 { type, node ->
                SkillIntegration.lookupInlineSkill(node.node("value").require())
            }
        }
    }

    companion object {

        @JvmField
        val DEFAULT_BLOCK: Block = object : Block {
            override val type: SkillWrapperType get() = SkillWrapperTypes.BLOCK
            override val id: String get() = "no operation"
            override fun cast(player: Player) = Unit
        }

        @JvmField
        val DEFAULT_INLINE: Inline = object : Inline {
            override val type: SkillWrapperType get() = SkillWrapperTypes.INLINE
            override val line: String get() = "log{message=\"no operation\"}"
            override fun cast(player: Player) = Unit
        }

        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<SkillWrapperType>(BuiltInRegistries.SKILL_WRAPPER_TYPE.valueByNameTypeSerializer())
            serials.registerExact<SkillWrapper>(DispatchingSerializer.create({ it.type }, { it.kotlinType }))
            serials.registerExact<Block>(Block.serializer())
            serials.registerExact<Inline>(Inline.serializer())
            return serials.build()
        }
    }
}

class SkillWrapperType(internal val kotlinType: KType)

object SkillWrapperTypes {

    @JvmField
    val BLOCK: SkillWrapperType = register<SkillWrapper.Block>("block")

    @JvmField
    val INLINE: SkillWrapperType = register<SkillWrapper.Inline>("inline")

    private inline fun <reified E : SkillWrapper> register(id: String): SkillWrapperType {
        return Registry.register(BuiltInRegistries.SKILL_WRAPPER_TYPE, id, SkillWrapperType(typeOf<E>()))
    }
}