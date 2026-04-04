package cc.mewcraft.wakame.item.feature

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.item.behavior.impl.SequenceComboHandler
import cc.mewcraft.wakame.item.feature.SequenceComboState.Companion.COMBO_LENGTH
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import cc.mewcraft.wakame.item.property.impl.SequenceCastableTrigger
import cc.mewcraft.wakame.item.tryCastSkill
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import cc.mewcraft.wakame.item.behavior.impl.Castable as CastableBehavior
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp
import org.bukkit.Sound as BukkitSound

//<editor-fold desc="Config Data Classes">

/**
 * 组合键序列功能的完整配置.
 *
 * 对应 config.yml 中 `sequence_castable_trigger_display` 节点.
 */
@ConfigSerializable
data class SequenceComboConfig(
    /**
     * 组合键超时 (ticks), 超过该时间未输入下一个键则重置组合.
     */
    val comboTimeoutTicks: Int = 20,
    /**
     * 左键的显示配置.
     */
    val leftClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "左"),
    /**
     * 右键的显示配置.
     */
    val rightClick: ClickDisplayConfig = ClickDisplayConfig(literalSymbol = "右"),
    /**
     * 序列结果的显示配置.
     */
    val sequence: SequenceResultConfig = SequenceResultConfig(),
)

/**
 * 单个点击类型 (左键/右键) 的显示配置.
 *
 * [literalSymbol] 和三种样式 ([successStyle], [failureStyle], [progressStyle])
 * 的组合方式为: `"{style}{symbol}"`, 交由 MiniMessage 解析.
 */
@ConfigSerializable
data class ClickDisplayConfig(
    /** 该点击类型在 action bar 上显示的符号. */
    val literalSymbol: String = "?",
    /** 序列匹配成功时应用的 MiniMessage 样式标签. */
    val successStyle: String = "<green>",
    /** 序列匹配失败时应用的 MiniMessage 样式标签. */
    val failureStyle: String = "<red>",
    /** 输入进行中时应用的 MiniMessage 样式标签. */
    val progressStyle: String = "<yellow>",
)

/**
 * 序列结果的显示配置.
 */
@ConfigSerializable
data class SequenceResultConfig(
    /** 各输入之间的连接符 (MiniMessage 格式). */
    val connector: String = "<gray> ➔ </gray>",
    /** 序列匹配成功时追加的后缀文本 (MiniMessage 格式). */
    val success: String = "<green>OK",
    /** 序列匹配失败时追加的后缀文本 (MiniMessage 格式). */
    val failure: String = "<red>BAD",
    /** 输入超时时追加的后缀文本 (MiniMessage 格式). */
    val timeout: String = "<gray>TIMEOUT",
)

//</editor-fold>

/**
 * 组合键序列的 per-player 状态.
 */
class SequenceComboState {
    /**
     * 已累积的输入序列.
     */
    val inputs: MutableList<GenericCastableTrigger> = mutableListOf()

    /**
     * 输入超时倒计时 (ticks). 归零时重置 combo.
     */
    var remainingTimeoutTicks: Int = 0

    /**
     * 累积一个输入.
     */
    fun addInput(input: GenericCastableTrigger) {
        if (inputs.size < COMBO_LENGTH) {
            inputs.add(input)
        }
    }

    /**
     * 是否已满 (累积了 [COMBO_LENGTH] 个输入).
     */
    fun isFull(): Boolean = inputs.size >= COMBO_LENGTH

    /**
     * 重置 combo 状态.
     */
    fun reset() {
        inputs.clear()
        remainingTimeoutTicks = 0
    }

    companion object {
        const val COMBO_LENGTH = 3

        val METADATA_KEY: MetadataKey<SequenceComboState> = metadataKey("castable:sequence_combo")
    }
}

/**
 * 实现了组合键序列触发技能的机制.
 *
 * 玩家每次左键/右键时累积输入到 per-player 的 combo 状态中,
 * 满 3 次后与物品上所有 [SequenceCastableTrigger] 匹配:
 * 匹配成功则施放技能并展示成功反馈,
 * 无匹配则展示失败反馈.
 * 超时未完成自动重置.
 */
@Init(InitStage.POST_WORLD)
object SequenceComboFeature : OnlineUserTicker, SequenceComboHandler {

    private val config: SequenceComboConfig by MAIN_CONFIG.entryOrElse(SequenceComboConfig(), "sequence_castable_trigger_display")

    private val SOUND_PROGRESS: Sound = Sound.sound().type(BukkitSound.UI_BUTTON_CLICK).volume(.5f).pitch(1.2f).build()
    private val SOUND_FAILURE: Sound = Sound.sound().type(BukkitSound.ENTITY_SHULKER_HURT).volume(.5f).build()

    init {
        // 注入组合键处理器到 Castable 行为
        CastableBehavior.sequenceComboHandler = this
    }

    //<editor-fold desc="Tick System (超时处理)">
    override fun onTickUser(user: User, player: Player) {
        val metadata = player.metadata()
        val comboState = metadata.getOrNull(SequenceComboState.METADATA_KEY) ?: return
        if (comboState.inputs.isEmpty())
            return

        comboState.remainingTimeoutTicks--
        if (comboState.remainingTimeoutTicks <= 0) {
            // 超时: 发送反馈并重置
            val message = buildTimeoutMessage(comboState.inputs)
            player.sendActionBar(message)
            player.playSound(SOUND_FAILURE)
            comboState.reset()
            metadata.remove(SequenceComboState.METADATA_KEY)
        }
    }
    //</editor-fold>

    //<editor-fold desc="SequenceComboHandler 实现">
    override fun handleInput(player: Player, castableMap: Map<String, CastableProp>, input: GenericCastableTrigger) {
        // 检查物品上是否有 SequenceCastableTrigger 类型的条目
        val sequenceEntries = castableMap.values.filter { it.trigger.unwrap() is SequenceCastableTrigger }
        if (sequenceEntries.isEmpty())
            return

        val metadata = player.metadata()
        val comboState = metadata.getOrPut(SequenceComboState.METADATA_KEY) { SequenceComboState() }

        // 累积输入
        comboState.addInput(input)
        comboState.remainingTimeoutTicks = config.comboTimeoutTicks

        if (!comboState.isFull()) {
            // 未满: 发送进度反馈
            val message = buildProgressMessage(comboState.inputs)
            player.sendActionBar(message)
            player.playSound(SOUND_PROGRESS)
            return
        }

        // 已满 3 个输入: 检查是否匹配
        val matchedEntry = sequenceEntries.firstOrNull { entry ->
            val trigger = entry.trigger.unwrap() as SequenceCastableTrigger
            trigger.sequence.toList() == comboState.inputs
        }

        if (matchedEntry != null) {
            // 匹配成功: 展示成功反馈, 施放技能
            val message = buildSuccessMessage(comboState.inputs)
            player.sendActionBar(message)
            tryCastSkill(player, matchedEntry)
        } else {
            // 匹配失败: 展示失败反馈
            val message = buildFailureMessage(comboState.inputs)
            player.sendActionBar(message)
            player.playSound(SOUND_FAILURE)
        }

        // 无论成功或失败, 重置 combo
        comboState.reset()
        metadata.remove(SequenceComboState.METADATA_KEY)
    }
    //</editor-fold>

    //<editor-fold desc="Display (Action Bar 消息构建)">
    private val mm = MiniMessage.miniMessage()

    /**
     * 根据输入类型获取对应的 [ClickDisplayConfig].
     */
    private fun clickConfigOf(input: GenericCastableTrigger): ClickDisplayConfig = when (input) {
        GenericCastableTrigger.LEFT_CLICK -> config.leftClick
        GenericCastableTrigger.RIGHT_CLICK -> config.rightClick
    }

    /**
     * 将输入序列中的每个元素用指定的样式选择器渲染, 以连接符拼接.
     */
    private fun buildSequenceComponent(
        inputs: List<GenericCastableTrigger>,
        styleSelector: (ClickDisplayConfig) -> String,
    ): Component {
        val parts = inputs.map { input ->
            val cfg = clickConfigOf(input)
            mm.deserialize("${styleSelector(cfg)}${cfg.literalSymbol}")
        }
        return Component.join(
            JoinConfiguration.separator(mm.deserialize(config.sequence.connector)),
            parts,
        )
    }

    /**
     * 构建进度消息 (未满 3 个输入时).
     *
     * 例: `<yellow>L</yellow> ➔ <yellow>R</yellow>`
     */
    private fun buildProgressMessage(inputs: List<GenericCastableTrigger>): Component {
        return buildSequenceComponent(inputs) { it.progressStyle }
    }

    /**
     * 构建成功消息 (匹配成功时).
     *
     * 例: `<green>L</green> ➔ <green>R</green> ➔ <green>L</green> OK`
     */
    private fun buildSuccessMessage(inputs: List<GenericCastableTrigger>): Component {
        val sequence = buildSequenceComponent(inputs) { it.successStyle }
        return sequence.append(Component.space()).append(mm.deserialize(config.sequence.success))
    }

    /**
     * 构建失败消息 (匹配失败时).
     *
     * 例: `<red>L</red> ➔ <red>R</red> ➔ <red>R</red> BAD`
     */
    private fun buildFailureMessage(inputs: List<GenericCastableTrigger>): Component {
        val sequence = buildSequenceComponent(inputs) { it.failureStyle }
        return sequence.append(Component.space()).append(mm.deserialize(config.sequence.failure))
    }

    /**
     * 构建超时消息.
     *
     * 例: `<red>L</red> ➔ <red>R</red> TIMEOUT`
     */
    private fun buildTimeoutMessage(inputs: List<GenericCastableTrigger>): Component {
        val sequence = buildSequenceComponent(inputs) { it.failureStyle }
        return sequence.append(Component.space()).append(mm.deserialize(config.sequence.timeout))
    }
    //</editor-fold>
}
