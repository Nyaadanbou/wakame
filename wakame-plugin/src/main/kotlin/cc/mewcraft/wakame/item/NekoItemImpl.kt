package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.UUID
import java.util.stream.Stream

internal class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val config: ConfigProvider,
    override val itemType: Key,
    override val hideTooltip: Boolean,
    override val hideAdditionalTooltip: Boolean,
    override val shownInTooltip: ShownInTooltipApplicator,
    override val slot: ItemSlot,
    override val templates: ItemTemplateMap,
    override val behaviors: ItemBehaviorMap,
) : Examinable, NekoItem {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("uuid", uuid),
        ExaminableProperty.of("itemType", itemType),
        ExaminableProperty.of("hideTooltip", hideTooltip),
        ExaminableProperty.of("hideAdditionalTooltip", hideAdditionalTooltip),
        ExaminableProperty.of("shownInTooltip", shownInTooltip),
        ExaminableProperty.of("slot", slot),
        ExaminableProperty.of("templates", templates),
        ExaminableProperty.of("behaviors", behaviors),
    )

    override fun toString(): String = toSimpleString()

    // 必须最后执行验证，以保证所有 member properties 已经初始化
    init {
        // No validation for now
        // NekoItemValidator.chain(
        //
        // ).validate(NekoItemValidator.Args(this))
    }
}