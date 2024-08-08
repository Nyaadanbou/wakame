package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.user.User
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
 * 工作站配方.
 * 包含若干项输入与若干项输出.
 */
sealed interface StationRecipe : Keyed, Examinable {
    val input: List<StationChoice>
    val output: List<StationResult>
    fun matches(user: User<Player>): Boolean
}

/**
 * 工作站配方的实现.
 */
class SimpleStationRecipe(
    override val key: Key,
    override val input: List<StationChoice>,
    override val output: List<StationResult>
) : StationRecipe {
    override fun matches(user: User<Player>): Boolean {
        TODO("Not yet implemented")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("input", input),
        ExaminableProperty.of("output", output),
    )

    override fun toString(): String = toSimpleString()

}


/**
 * [StationRecipe] 的序列化器.
 */
internal object StationRecipeSerializer : TypeSerializer<StationRecipe>, KoinComponent {
    val HINT_NODE: RepresentationHint<Key> = RepresentationHint.of("key", typeTokenOf<Key>())

    override fun deserialize(type: Type, node: ConfigurationNode): StationRecipe {
        val key = node.hint(HINT_NODE) ?: throw SerializationException(
            "The hint node for recipe key is not present"
        )

        val input = node.node("input").getList<StationChoice>(emptyList()).apply {
            require(isNotEmpty()) { "Station recipe input is not present" }
        }

        val output = node.node("output").getList<StationResult>(emptyList()).apply {
            require(isNotEmpty()) { "Station recipe output is not present" }
        }

        return SimpleStationRecipe(key, input, output)
    }
}