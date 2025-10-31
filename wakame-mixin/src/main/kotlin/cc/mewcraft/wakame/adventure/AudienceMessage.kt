package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.util.Ticks
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

/**
 * 代表一个与条件相关的消息提示.
 */
interface AudienceMessage {
    /**
     * 发送消息给 [audience].
     */
    fun send(audience: Audience, tagResolver: TagResolver = TagResolver.empty())
}

/**
 * 代表多个 [AudienceMessage].
 */
interface AudienceMessageGroup {
    val messages: Iterable<AudienceMessage>
    fun send(audience: Audience, tagResolver: TagResolver = TagResolver.empty())

    companion object {
        @JvmField
        val SERIALIZER: TypeSerializerCollection = TypeSerializerCollection.builder()
            .register(AudienceMessageGroupSerializer)
            .register(CombinedAudienceMessageSerializer)
            .build()

        fun empty(): AudienceMessageGroup = EmptyAudienceMessageGroup
    }
}

/* 实现类 */

//<editor-fold desc="Description">
private object EmptyAudienceMessage : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) = Unit
}

private object EmptyAudienceMessageGroup : AudienceMessageGroup {
    override val messages: Iterable<AudienceMessage> = emptyList()
    override fun send(audience: Audience, tagResolver: TagResolver) {}
}

private class AudienceMessageGroupImpl(
    override val messages: Iterable<AudienceMessage>,
) : AudienceMessageGroup {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        messages.forEach { it.send(audience, tagResolver) }
    }
}

internal class ChatAudienceMessage(
    private val text: String,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        val component = MM.deserialize(text, tagResolver)
        audience.sendMessage(component)
    }
}

internal class ActionbarAudienceMessage(
    private val text: String,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        val component = MM.deserialize(text, tagResolver)
        audience.sendActionBar(component)
    }
}

internal class TitleAudienceMessage(
    private val title: String,
    private val subtitle: String,
    private val times: Times,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        val titleComponent = MM.deserialize(title, tagResolver)
        val subtitleComponent = MM.deserialize(subtitle, tagResolver)
        audience.showTitle(Title.title(titleComponent, subtitleComponent, times))
    }
}

internal class SoundAudienceMessage(
    private val sound: Sound,
    private val emitter: EmitterValue,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
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

internal class SoundStopAudienceMessage(
    private val soundStop: SoundStop,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        audience.stopSound(soundStop)
    }
}
//</editor-fold>

/* 序列化 */

//<editor-fold desc="Serializers">
/**
 * 集成所有 [AudienceMessage] 为一体的序列化器.
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
private object CombinedAudienceMessageSerializer : TypeSerializer2<AudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): AudienceMessage {
        if (node.rawScalar() != null) {
            return ChatAudienceMessageSerializer.deserialize(type, node)
        }

        val messageTypeNode = node.node("type")
        return when (
            val messageType = messageTypeNode.require<String>()
        ) {
            "chat" -> ChatAudienceMessageSerializer.deserialize(type, node)
            "actionbar" -> ActionbarAudienceMessageSerializer.deserialize(type, node)
            "title" -> TitleAudienceMessageSerializer.deserialize(type, node)
            "sound" -> SoundAudienceMessageSerializer.deserialize(type, node)
            "sound_stop" -> SoundStopAudienceMessageSerializer.deserialize(type, node)
            "sound_stop_all" -> SoundStopAudienceMessage(SoundStop.all())
            else -> throw SerializationException(messageTypeNode, type, "Invalid message type: '$messageType'. Valid values: 'chat', 'actionbar', 'title', 'sound', 'sound_stop', 'sound_stop_all'")
        }
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): AudienceMessage {
        return EmptyAudienceMessage
    }
}

private object AudienceMessageGroupSerializer : TypeSerializer2<AudienceMessageGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): AudienceMessageGroup {
        if (node.rawScalar() != null) {
            return AudienceMessageGroupImpl(listOf(node.require<AudienceMessage>()))
        }
        val messages = node.getList<AudienceMessage>(emptyList())
        return AudienceMessageGroupImpl(messages)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): AudienceMessageGroup {
        return EmptyAudienceMessageGroup
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * text: "foo"
 * ```
 *
 * or
 *
 * ```yaml
 * "foo"
 * ```
 */
private object ChatAudienceMessageSerializer : TypeSerializer2<ChatAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ChatAudienceMessage {
        if (node.rawScalar() != null) {
            return ChatAudienceMessage(node.require<String>())
        }
        val text = node.node("text").require<String>()
        return ChatAudienceMessage(text)
    }
}

/**
 * ## Node structure
 *
 * ```yaml
 * text: "foo"
 * ```
 */
private object ActionbarAudienceMessageSerializer : TypeSerializer2<ActionbarAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ActionbarAudienceMessage {
        val text = node.node("text").require<String>()
        return ActionbarAudienceMessage(text)
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
private object TitleAudienceMessageSerializer : TypeSerializer2<TitleAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): TitleAudienceMessage {
        val title = node.node("title").require<String>()
        val subtitle = node.node("subtitle").require<String>()
        val fadeIn = node.node("fade_in").require<Long>()
        val stay = node.node("stay").require<Long>()
        val fadeOut = node.node("fade_out").require<Long>()
        return TitleAudienceMessage(title, subtitle, Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)))
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
private object SoundAudienceMessageSerializer : TypeSerializer2<SoundAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundAudienceMessage {
        // read name
        val name = node.node("name").require<Key>()

        // read source
        val source = node.node("source").get<Source>(Source.MASTER)

        // read volume
        val volumeNode = node.node("volume")
        val volume = volumeNode.get<Float>(1F).takeIf { 0F <= it && it <= Int.MAX_VALUE } ?: throw SerializationException(volumeNode, type, "The volume must be in between 0 and ${Int.MAX_VALUE}")

        // read pitch
        val pitchNode = node.node("pitch")
        val pitch = pitchNode.get<Float>(1F).takeIf { -1F <= it && it <= 1F } ?: throw SerializationException(pitchNode, type, "The pitch must be in between -1 and 1")

        // read seed
        val seed = node.node("seed").takeIf { !it.isNull }?.require<Long>()

        // read emitter
        val emitter = node.node("emitter").get<SoundAudienceMessage.EmitterValue>(SoundAudienceMessage.EmitterValue.RECIPIENT_LOCATION)

        val sound = Sound.sound().apply {
            type(name)
            source(source)
            volume(volume)
            pitch(pitch)
            seed?.let { seed(seed) }
        }.build()

        return SoundAudienceMessage(sound, emitter)
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
private object SoundStopAudienceMessageSerializer : TypeSerializer2<SoundStopAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundStopAudienceMessage {
        // read name
        val name = node.node("name").require<Key>()

        // read source
        val source = node.node("source").takeIf { !it.isNull }?.require<Source>()

        val soundStop = if (source != null) {
            SoundStop.namedOnSource(name, source)
        } else {
            SoundStop.named(name)
        }

        return SoundStopAudienceMessage(soundStop)
    }

}
//</editor-fold>
