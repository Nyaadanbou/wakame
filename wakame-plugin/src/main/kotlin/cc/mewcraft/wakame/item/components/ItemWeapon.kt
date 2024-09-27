package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

data class ItemWeapon(
    val attackType: AttackType,
    val parameters: Map<String, Double>
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
        val parameters: Map<String, Double>
    ) : ItemTemplate<Unit> {
        override val componentType: ItemComponentType<Unit> = ItemComponentTypes.WEAPON

        override fun generate(context: GenerationContext): GenerationResult<Unit> {
            return GenerationResult.of(Unit)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   attack_type: <enum>
         *   parameters:
         *     parameter_1: <double>
         *     parameter_2: <double>
         *     parameter_3: <double>
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            val attackType = node.node("attack_type").get<AttackType>(AttackType.SINGLE)
            val parameters = node.node("parameters")
                .childrenMap()
                .mapKeys { (key, _) ->
                    key.toString()
                }
                .mapValues { (_, value) ->
                    value.krequire<Double>()
                }

            return Template(attackType, parameters)
        }
    }
}

enum class AttackType {
    /**
     * 原版剑横扫攻击.
     */
    SWEEP,

    /**
     * 原版斧单体攻击.
     * 或作为默认值使用.
     */
    SINGLE,

    /**
     * 原版弓攻击.
     */
    BOW,

    /**
     * 原版弩攻击.
     */
    CROSSBOW,

    /**
     * 锤攻击.
     * 对直接攻击到的实体造成面板伤害.
     * 对 锤击半径 内的实体造成 锤击威力*面板 的伤害.
     */
    HAMMER,
}