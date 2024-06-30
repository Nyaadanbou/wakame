package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.util.Ticks
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

/**
 * 代表一个与条件相关的消息提示.
 */
interface ConditionMessage {
    /**
     * 发送消息给 [audience].
     */
    fun send(audience: Audience)
}

/**
 * 代表多个 [ConditionMessage].
 */
interface ConditionMessageGroup {
    val messages: Iterable<ConditionMessage>
    fun send(audience: Audience)

    companion object {
        fun empty(): ConditionMessageGroup = EmptyConditionMessageGroup
    }
}

/* 实现类 */

//<editor-fold desc="Description">
private object EmptyConditionMessage : ConditionMessage {
    override fun send(audience: Audience) = Unit
}

private object EmptyConditionMessageGroup : ConditionMessageGroup {
    override val messages: Iterable<ConditionMessage> = emptyList()
    override fun send(audience: Audience) {}
}

private class ConditionMessageGroupImpl(
    override val messages: Iterable<ConditionMessage>,
) : ConditionMessageGroup {
    override fun send(audience: Audience) {
        messages.forEach { it.send(audience) }
    }
}

internal class ChatConditionMessage(
    private val text: Component,
) : ConditionMessage {
    override fun send(audience: Audience) {
        audience.sendMessage(text)
    }
}

internal class ActionbarConditionMessage(
    private val text: Component,
) : ConditionMessage {
    override fun send(audience: Audience) {
        audience.sendMessage(text)
    }
}

internal class TitleConditionMessage(
    private val title: Component,
    private val subtitle: Component,
    private val times: Times,
) : ConditionMessage {
    override fun send(audience: Audience) {
        audience.showTitle(Title.title(title, subtitle, times))
    }
}

internal class SoundConditionMessage(
    private val sound: Sound,
    private val emitter: EmitterValue,
) : ConditionMessage {
    override fun send(audience: Audience) {
        when (emitter) {
            EmitterValue.SELF -> audience.playSound(sound, Emitter.self())
            EmitterValue.RECIPIENT_LOCATION -> audience.playSound(sound)
        }
    }

    enum class EmitterValue {
        /**
         * See [Emitter.self].
         */
        SELF,

        /**
         * See [Audience.playSound]
         */
        RECIPIENT_LOCATION,
        ;
    }
}

internal class SoundStopConditionMessage(
    private val soundStop: SoundStop,
) : ConditionMessage {
    override fun send(audience: Audience) {
        audience.stopSound(soundStop)
    }
}
//</editor-fold>

/* 序列化 */

//<editor-fold desc="Serializers">
internal interface ConditionMessageSerializer<T> : TypeSerializer<T> {
    override fun deserialize(type: Type, node: ConfigurationNode): T
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Unit = throw UnsupportedOperationException()
}

/**
 * 集成所有 [ConditionMessage] 为一体的序列化器.
 *
 * 实现会首先判断 `type` 这个节点, 来调用对应的序列化器.
 *
 * ## Node structure
 *
 * ```yaml
 * type: chat | actionbar | title | sound
 * (相关设置 ...)
 * ```
 */
internal object CombinedConditionMessageSerializer : ConditionMessageSerializer<ConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConditionMessage {
        val messageTypeNode = node.node("type")
        return when (
            val messageType = messageTypeNode.krequire<String>()
        ) {
            "chat" -> ChatConditionMessageSerializer.deserialize(type, node)
            "actionbar" -> ActionbarConditionMessageSerializer.deserialize(type, node)
            "title" -> TitleConditionMessageSerializer.deserialize(type, node)
            "sound" -> SoundConditionMessageSerializer.deserialize(type, node)
            "sound_stop" -> SoundStopConditionMessageSerializer.deserialize(type, node)
            "sound_stop_all" -> SoundStopConditionMessage(SoundStop.all())
            else -> throw SerializationException(messageTypeNode, type, "Invalid message type: '$messageType'. Valid values: 'chat', 'actionbar', 'title', 'sound', 'sound_stop', 'sound_stop_all'")
        }
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ConditionMessage {
        return EmptyConditionMessage
    }
}

internal object ConditionMessageGroupSerializer : ConditionMessageSerializer<ConditionMessageGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConditionMessageGroup {
        val messages = node.getList<ConditionMessage>(emptyList())
        return ConditionMessageGroupImpl(messages)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ConditionMessageGroup {
        return EmptyConditionMessageGroup
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * text: "foo"
 * ```
 */
internal object ChatConditionMessageSerializer : ConditionMessageSerializer<ChatConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ChatConditionMessage {
        val text = node.node("text").krequire<Component>()
        return ChatConditionMessage(text)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * text: "foo"
 * ```
 */
internal object ActionbarConditionMessageSerializer : ConditionMessageSerializer<ActionbarConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ActionbarConditionMessage {
        val text = node.node("text").krequire<Component>()
        return ActionbarConditionMessage(text)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * title: "foo"
 * subtitle: "foo"
 * fade_in: 20 # 单位: 刻
 * stay: 20 # 单位: 刻
 * fade_out: # 单位: 刻
 * ```
 */
internal object TitleConditionMessageSerializer : ConditionMessageSerializer<TitleConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): TitleConditionMessage {
        val title = node.node("title").krequire<Component>()
        val subtitle = node.node("subtitle").krequire<Component>()
        val fadeIn = node.node("fade_in").krequire<Long>()
        val stay = node.node("stay").krequire<Long>()
        val fadeOut = node.node("fade_out").krequire<Long>()
        return TitleConditionMessage(title, subtitle, Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)))
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * name: "entity.pig.hurt" # 必写, 可用: https://minecraft.wiki/w/Sounds.json#Java_Edition_values
 * source: "master" # 可选, 默认: MASTER, 可用: https://minecraft.wiki/w/Sound#Categories
 * volume: 1.0 # 可选, 默认: 0.0, 可用: 0 到 2147483647
 * pitch: 1.0 # 可选, 默认: 0.0, 可用: -1 到 1
 * seed: 233 # 可选, 默认: null, 可用: 整数
 * emitter: self # 可选, 默认: 'recipient_location', 可用值: 'self', 'recipient_location'
 * ```
 */
internal object SoundConditionMessageSerializer : ConditionMessageSerializer<SoundConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundConditionMessage {
        // read name
        val name = node.node("name").krequire<Key>()

        // read source
        val source = node.node("source").get<Source>(Source.MASTER)

        // read volume
        val volumeNode = node.node("volume")
        val volume = volumeNode.get<Float>(1F).takeIf { 0F <= it && it <= Int.MAX_VALUE } ?: throw SerializationException(volumeNode, type, "The volume must be in between 0 and ${Int.MAX_VALUE}")

        // read pitch
        val pitchNode = node.node("pitch")
        val pitch = pitchNode.get<Float>(1F).takeIf { -1F <= it && it <= 1F } ?: throw SerializationException(pitchNode, type, "The pitch must be in between -1 and 1")

        // read seed
        val seed = node.node("seed").takeIf { !it.isNull }?.krequire<Long>()

        // read emitter
        val emitter = node.node("emitter").get<SoundConditionMessage.EmitterValue>(SoundConditionMessage.EmitterValue.RECIPIENT_LOCATION)

        val sound = Sound.sound().apply {
            type(name)
            source(source)
            volume(volume)
            pitch(pitch)
            seed?.let { seed(seed) }
        }.build()

        return SoundConditionMessage(sound, emitter)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * name: "entity.pig.hurt" # 必写, 可用: https://minecraft.wiki/w/Sounds.json#Java_Edition_values
 * source: "master" # 可选, 默认: MASTER, 可用: https://minecraft.wiki/w/Sound#Categories
 * ```
 */
internal object SoundStopConditionMessageSerializer : ConditionMessageSerializer<SoundStopConditionMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundStopConditionMessage {
        // read name
        val name = node.node("name").krequire<Key>()

        // read source
        val source = node.node("source").takeIf { !it.isNull }?.krequire<Source>()

        val soundStop = if (source != null) {
            SoundStop.namedOnSource(name, source)
        } else {
            SoundStop.named(name)
        }

        return SoundStopConditionMessage(soundStop)
    }

}
//</editor-fold>