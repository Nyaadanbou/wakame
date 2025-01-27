package cc.mewcraft.wakame.world

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.world.WeatherControl.execute
import me.lucko.helper.cooldown.Cooldown
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.SerializationException
import xyz.xenondevs.commons.provider.map
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * 控制所有维度的天气.
 */
object WeatherControl {
    private val useInterval: Cooldown by MAIN_CONFIG
        .entry<Long>("world_weather_control_use_interval_ticks")
        .map(Cooldown::ofTicks)

    /**
     * 检查全局最小的使用间隔是否已经过去.
     * 如果返回 `true` 则可以继续执行 [execute],
     * 否则应该提示玩家无法使用, 需要等待一段时间.
     */
    fun isReady(): Boolean {
        return useInterval.testSilently()
    }

    fun getTimeUntilReadyTicks(): Long {
        return useInterval.remainingMillis() / 50
    }

    fun execute(vararg actions: Action) {
        execute(actions.asIterable())
    }

    fun execute(actions: Iterable<Action>) {
        for (act in actions) {
            LOGGER.info("Executing weather control action: ${act.type}")
            act.execute()
        }
        useInterval.reset()
    }

    interface Action {
        val type: ActionType
        fun execute()
    }

    enum class ActionType {
        SET_STORM,
        SET_WEATHER_DURATION,
        SET_CLEAR_WEATHER_DURATION,
        SET_THUNDERING,
        SET_THUNDER_DURATION,
    }
}


// type serializer

internal object WeatherControlActionSerializer : TypeSerializer<WeatherControl.Action> {
    private val TYPE_MAPPINGS: Map<WeatherControl.ActionType, KType> = mapOf(
        WeatherControl.ActionType.SET_STORM to typeOf<SetStorm>(),
        WeatherControl.ActionType.SET_WEATHER_DURATION to typeOf<SetWeatherDuration>(),
        WeatherControl.ActionType.SET_CLEAR_WEATHER_DURATION to typeOf<SetClearWeatherDuration>(),
        WeatherControl.ActionType.SET_THUNDERING to typeOf<SetThundering>(),
        WeatherControl.ActionType.SET_THUNDER_DURATION to typeOf<SetThunderDuration>(),
    )

    override fun deserialize(type: Type, node: ConfigurationNode): WeatherControl.Action {
        val typeNode = node.node("type")
        val actionType = typeNode.require<WeatherControl.ActionType>()
        val actionTypeKType = TYPE_MAPPINGS[actionType] ?: throw SerializationException(
            typeNode, javaTypeOf<WeatherControl.ActionType>(), "unknown action type: $actionType"
        )
        return node.require(actionTypeKType)
    }
}

/* implementations of WeatherControl.Action */

@ConfigSerializable
internal data class SetStorm(
    @Setting("value")
    val flag: Boolean,
) : WeatherControl.Action {
    override val type = WeatherControl.ActionType.SET_STORM
    override fun execute() {
        for (world in SERVER.worlds) {
            world.setStorm(flag)
        }
    }
}

@ConfigSerializable
internal data class SetWeatherDuration(
    @Setting("value")
    val duration: Int,
) : WeatherControl.Action {
    override val type = WeatherControl.ActionType.SET_WEATHER_DURATION
    override fun execute() {
        for (world in SERVER.worlds) {
            world.weatherDuration = duration
        }
    }
}

@ConfigSerializable
internal data class SetClearWeatherDuration(
    @Setting("value")
    val duration: Int,
) : WeatherControl.Action {
    override val type = WeatherControl.ActionType.SET_CLEAR_WEATHER_DURATION
    override fun execute() {
        for (world in SERVER.worlds) {
            world.clearWeatherDuration = duration
        }
    }
}

@ConfigSerializable
internal data class SetThundering(
    @Setting("value")
    val flag: Boolean,
) : WeatherControl.Action {
    override val type = WeatherControl.ActionType.SET_THUNDERING
    override fun execute() {
        for (world in SERVER.worlds) {
            world.isThundering = flag
        }
    }
}

@ConfigSerializable
internal data class SetThunderDuration(
    @Setting("value")
    val duration: Int,
) : WeatherControl.Action {
    override val type = WeatherControl.ActionType.SET_THUNDER_DURATION
    override fun execute() {
        for (world in SERVER.worlds) {
            world.thunderDuration = duration
        }
    }
}