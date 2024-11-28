package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 合成站配方.
 * 包含若干项输入与若干项输出.
 */
internal sealed interface StationRecipe : Keyed, Examinable {
    /**
     * 配方的输入.
     */
    val input: List<StationChoice>

    /**
     * 配方的输出.
     */
    val output: StationResult

    /**
     * 检查该 [StationRecipe] 是否有效.
     * 用于延迟检查配方是否能够注册到服务端.
     */
    fun valid(): Boolean

    /**
     * 针对 [player] 发起一次检查. 返回的结果包含了玩家已满足/不满足的条件.
     */
    fun match(player: Player): RecipeMatcherResult

    /**
     * 针对 [player] 发起一次消耗. 该函数将消耗掉玩家背包中的物品堆叠.
     */
    fun consume(player: Player)
}

/**
 * 合成站配方的实现.
 */
internal class SimpleStationRecipe(
    override val key: Key,
    override val input: List<StationChoice>,
    override val output: StationResult,
) : StationRecipe {

    override fun valid(): Boolean {
        input.forEach { choice: StationChoice ->
            if (!choice.valid()) {
                return false
            }
        }
        return output.valid()
    }

    override fun match(player: Player): RecipeMatcherResult {
        return RecipeMatcher.match(this, player)
    }

    override fun consume(player: Player) {
        return RecipeConsumer.consume(this, player)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("output", output),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}


/**
 * [StationRecipe] 的序列化器.
 */
internal object StationRecipeSerializer : TypeSerializer<StationRecipe>, KoinComponent {
    val HINT_NODE: RepresentationHint<Key> = RepresentationHint.of("key", typeTokenOf<Key>())

    override fun deserialize(type: Type, node: ConfigurationNode): StationRecipe {
        val key = node.hint(HINT_NODE) ?: throw SerializationException("the hint node for station recipe key is not present")
        val input = node.node("input").getList<StationChoice>(emptyList())
        require(input.isNotEmpty()) { "station recipe input is not present" }
        val output = node.node("output").krequire<StationResult>()

        return SimpleStationRecipe(key, input, output)
    }
}