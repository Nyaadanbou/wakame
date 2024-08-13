package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Emitter
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.util.Ticks
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
        val component = AudienceMessageSupport.miniMessage.deserialize(text, tagResolver)
        audience.sendMessage(component)
    }
}

internal class ActionbarAudienceMessage(
    private val text: String,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        val component = AudienceMessageSupport.miniMessage.deserialize(text, tagResolver)
        audience.sendActionBar(component)
    }
}

internal class TitleAudienceMessage(
    private val title: String,
    private val subtitle: String,
    private val times: Times,
) : AudienceMessage {
    override fun send(audience: Audience, tagResolver: TagResolver) {
        val titleComponent = AudienceMessageSupport.miniMessage.deserialize(title, tagResolver)
        val subtitleComponent = AudienceMessageSupport.miniMessage.deserialize(subtitle, tagResolver)
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
internal interface AudienceMessageSerializer<T> : TypeSerializer<T> {
    override fun deserialize(type: Type, node: ConfigurationNode): T
    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Unit = throw UnsupportedOperationException()
}

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
internal object CombinedAudienceMessageSerializer : AudienceMessageSerializer<AudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): AudienceMessage {
        if (node.rawScalar() != null) {
            return ChatAudienceMessageSerializer.deserialize(type, node)
        }

        val messageTypeNode = node.node("type")
        return when (
            val messageType = messageTypeNode.krequire<String>()
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

internal object AudienceMessageGroupSerializer : AudienceMessageSerializer<AudienceMessageGroup> {
    override fun deserialize(type: Type, node: ConfigurationNode): AudienceMessageGroup {
        if (node.rawScalar() != null) {
            return AudienceMessageGroupImpl(listOf(node.krequire()))
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
internal object ChatAudienceMessageSerializer : AudienceMessageSerializer<ChatAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ChatAudienceMessage {
        if (node.rawScalar() != null) {
            return ChatAudienceMessage(node.krequire())
        }
        val text = node.node("text").krequire<String>()
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
internal object ActionbarAudienceMessageSerializer : AudienceMessageSerializer<ActionbarAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): ActionbarAudienceMessage {
        val text = node.node("text").krequire<String>()
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
internal object TitleAudienceMessageSerializer : AudienceMessageSerializer<TitleAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): TitleAudienceMessage {
        val title = node.node("title").krequire<String>()
        val subtitle = node.node("subtitle").krequire<String>()
        val fadeIn = node.node("fade_in").krequire<Long>()
        val stay = node.node("stay").krequire<Long>()
        val fadeOut = node.node("fade_out").krequire<Long>()
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
internal object SoundAudienceMessageSerializer : AudienceMessageSerializer<SoundAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundAudienceMessage {
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
internal object SoundStopAudienceMessageSerializer : AudienceMessageSerializer<SoundStopAudienceMessage> {
    override fun deserialize(type: Type, node: ConfigurationNode): SoundStopAudienceMessage {
        // read name
        val name = node.node("name").krequire<Key>()

        // read source
        val source = node.node("source").takeIf { !it.isNull }?.krequire<Source>()

        val soundStop = if (source != null) {
            SoundStop.namedOnSource(name, source)
        } else {
            SoundStop.named(name)
        }

        return SoundStopAudienceMessage(soundStop)
    }

}
//</editor-fold>

private object AudienceMessageSupport : KoinComponent {
    val miniMessage: MiniMessage by inject()
}