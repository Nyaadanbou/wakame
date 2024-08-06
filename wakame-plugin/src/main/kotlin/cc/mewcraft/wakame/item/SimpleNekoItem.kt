package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.vanilla.VanillaComponentRemover
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 一个标准的 [NekoItem].
 */
internal class SimpleNekoItem(
    override val key: Key,
    override val config: ConfigProvider,
    override val itemType: Key,
    override val slotGroup: ItemSlotGroup,
    override val removeComponents: VanillaComponentRemover,
    override val templates: ItemTemplateMap,
    override val behaviors: ItemBehaviorMap,
) : NekoItem {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("config", config.relPath),
        ExaminableProperty.of("item_type", itemType.asString()),
        ExaminableProperty.of("slot_group", slotGroup),
        ExaminableProperty.of("remove_components", removeComponents),
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

/**
 * 空的 [NekoItem], 某些特殊情况下使用.
 */
internal object EmptyNekoItem : NekoItem {
    override val key: Key = GenericKeys.EMPTY
    override val config: ConfigProvider
        get() = throw UnsupportedOperationException("EmptyNekoItem does not have a config.")
    override val itemType: Key = Key.key("air")
    override val slotGroup: ItemSlotGroup = ItemSlotGroup.empty()
    override val removeComponents: VanillaComponentRemover = VanillaComponentRemover.nop()
    override val templates: ItemTemplateMap = ItemTemplateMap.empty()
    override val behaviors: ItemBehaviorMap = ItemBehaviorMap.empty()
}