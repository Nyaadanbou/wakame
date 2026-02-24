package cc.mewcraft.wakame.integration.skill

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.item.property.impl.Castable
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.serialization.configurate.serializer.valueByNameTypeSerializer
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
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
    fun cast(player: Player, ctx: Castable? = null)

    /**
     * 一个完整的机制, 以 [id] 唯一识别.
     * 实现上直接用该 [id] 获取对应的机制, 然后释放.
     */
    @ConfigSerializable
    data class Block(
        @Setting(value = "value")
        val id: String,
    ) : SkillWrapper {

        override val type: SkillWrapperType
            get() = SkillWrapperTypes.BLOCK

        override fun cast(player: Player, ctx: Castable?) {
            SkillIntegration.castBlockSkill(player, id, ctx)
        }
    }

    /**
     * 一个内联的机制, 仅储存原始的 [line].
     * 实现上需要先解析 [line] 获取对应机制, 然后释放.
     */
    @ConfigSerializable
    data class Inline(
        @Setting(value = "value")
        val line: String,
    ) : SkillWrapper {

        override val type: SkillWrapperType
            get() = SkillWrapperTypes.INLINE

        override fun cast(player: Player, ctx: Castable?) {
            SkillIntegration.castInlineSkill(player, line, ctx)
        }
    }

    companion object {

        fun serializers(): TypeSerializerCollection {
            val serials = TypeSerializerCollection.builder()
            serials.register<SkillWrapperType>(BuiltInRegistries.SKILL_WRAPPER_TYPE.valueByNameTypeSerializer())
            serials.registerExact<SkillWrapper>(DispatchingSerializer.create<SkillWrapperType, SkillWrapper>(SkillWrapper::type, SkillWrapperType::kotlinType))
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