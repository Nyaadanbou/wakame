package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

data class ItemAttack(
    val attackType: AttackType,
) : Examinable {
    companion object : ItemComponentBridge<Unit> {
        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {
        override fun read(holder: ItemComponentHolder): Unit? {
            return if (holder.hasTag()) Unit else null
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.editTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data class Template(
        val attackType: AttackType,
    ) : ItemTemplate<Unit> {
        override val componentType: ItemComponentType<Unit> = ItemComponentTypes.ATTACK

        override fun generate(context: GenerationContext): GenerationResult<Unit> {
            return GenerationResult.of(Unit)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        override fun decode(node: ConfigurationNode): Template {
            val attackType = node.krequire<AttackType>()
            return Template(attackType)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(AttackTypeSerializer)
                .build()
        }
    }
}

/**
 * 攻击类型.
 * 其实现类先不要写成单例.
 * 未来确认是无参数的实现再写成单例.
 */
sealed interface AttackType

/**
 * 原版剑横扫攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: sword
 * ```
 */
class SwordAttack : AttackType {
    companion object {
        const val NAME = "sword"
    }
}

/**
 * 原版斧单体攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: axe
 * ```
 */
class AxeAttack : AttackType {
    companion object {
        const val NAME = "axe"
    }
}

/**
 * 原版弓攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: bow
 * ```
 */
class BowShoot : AttackType {
    companion object {
        const val NAME = "bow"
    }
}

/**
 * 原版弩攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: crossbow
 * ```
 */
class CrossbowShoot : AttackType {
    companion object {
        const val NAME = "crossbow"
    }
}

/**
 * 原版三叉戟攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: trident
 * ```
 */
class TridentAttack : AttackType {
    companion object {
        const val NAME = "trident"
    }
}

/**
 * 原版重锤攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: mace
 * ```
 */
class MaceAttack : AttackType {
    companion object {
        const val NAME = "mace"
    }
}

/**
 * 自定义锤攻击.
 * 对直接攻击到的实体造成面板伤害.
 * 对 锤击半径 内的实体造成 锤击威力*面板 的伤害.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: hammer
 * ```
 */
class HammerAttack : AttackType {
    companion object {
        const val NAME = "hammer"
    }
}


/**
 * [AttackType] 的序列化器.
 */
internal object AttackTypeSerializer : TypeSerializer<AttackType> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttackType {
        val attackType = node.node("type").krequire<String>()
        return when (attackType) {
            SwordAttack.NAME -> {
                SwordAttack()
            }

            AxeAttack.NAME -> {
                AxeAttack()
            }

            BowShoot.NAME -> {
                BowShoot()
            }

            CrossbowShoot.NAME -> {
                CrossbowShoot()
            }

            TridentAttack.NAME -> {
                TridentAttack()
            }

            MaceAttack.NAME -> {
                MaceAttack()
            }

            HammerAttack.NAME -> {
                HammerAttack()
            }

            else -> {
                throw SerializationException("Unknown attack type")
            }
        }
    }
}