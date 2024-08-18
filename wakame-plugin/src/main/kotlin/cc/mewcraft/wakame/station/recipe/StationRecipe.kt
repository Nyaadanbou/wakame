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
sealed interface StationRecipe : Keyed, Examinable {
    val input: List<StationChoice>
    val output: StationResult

    /**
     * 该 [StationRecipe] 是否有效
     * 用于延迟验证配方是否能够注册
     */
    fun isValid(): Boolean

    /**
     * 检查一特定玩家某配方的各项要求是否分别满足.
     */
    fun match(player: Player): RecipeMatcherResult

    /**
     * 执行此 [StationRecipe] 的消耗
     */
    fun consume(player: Player)
}

/**
 * 合成站配方的实现.
 */
internal class SimpleStationRecipe(
    override val key: Key,
    override val input: List<StationChoice>,
    override val output: StationResult
) : StationRecipe {

    override fun isValid(): Boolean {
        input.forEach {
            if (!it.isValid()) return false
        }
        return output.isValid()
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

    override fun toString(): String =
        toSimpleString()
}


/**
 * [StationRecipe] 的序列化器.
 */
internal object StationRecipeSerializer : TypeSerializer<StationRecipe>, KoinComponent {
    val HINT_NODE: RepresentationHint<Key> = RepresentationHint.of("key", typeTokenOf<Key>())

    override fun deserialize(type: Type, node: ConfigurationNode): StationRecipe {
        val key = node.hint(HINT_NODE) ?: throw SerializationException(
            "The hint node for station recipe key is not present"
        )

        val input = node.node("input").getList<StationChoice>(emptyList()).apply {
            require(isNotEmpty()) { "Station recipe input is not present" }
        }

        val output = node.node("output").krequire<StationResult>()

        return SimpleStationRecipe(key, input, output)
    }
}