package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.schema.behavior.ItemBehavior
import cc.mewcraft.wakame.item.schema.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.registry.BehaviorRegistry
import cc.mewcraft.wakame.util.toSimpleString
import com.google.common.collect.ClassToInstanceMap
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.UUID
import java.util.stream.Stream
import kotlin.reflect.KClass

internal class NekoItemImpl(
    override val key: Key,
    override val uuid: UUID,
    override val config: ConfigProvider,
    override val material: Key,
    override val hideTooltip: Boolean,
    override val hideAdditionalTooltip: Boolean,
    override val shownInTooltip: ShownInTooltipApplicator,
    override val slot: ItemSlot,
    private val metaMap: ClassToInstanceMap<SchemaItemMeta<*>>,
    override val cellMap: Map<String, SchemaCell>,
    behaviorHolders: List<String>,
) : Examinable, NekoItem {
    override val meta: Set<SchemaItemMeta<*>> = metaMap.values.toSet()
    override val cell: Set<SchemaCell> = cellMap.values.toSet()

    override fun <T : SchemaItemMeta<*>> getMeta(metaClass: KClass<T>): T {
        return requireNotNull(metaMap.getInstance(metaClass.java)) { "Can't find schema item meta '${metaClass.simpleName}' for item '$key'" }
    }

    override val behaviors: List<ItemBehavior> = behaviorHolders
        .map { BehaviorRegistry.INSTANCES[it] to it }
        .map { (behavior, behaviorName) ->
            when (behavior) {
                is ItemBehavior -> behavior
                is ItemBehaviorFactory<*> -> behavior.create(this, config.derive("behaviors", behaviorName))
            }
        }

    override fun <T : ItemBehavior> getBehavior(behaviorClass: KClass<T>): T {
        return getBehaviorOrNull(behaviorClass) ?: throw IllegalStateException("Item $key does not have a behavior of type ${behaviorClass.simpleName}")
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("uuid", uuid),
        ExaminableProperty.of("material", material),
        ExaminableProperty.of("hideTooltip", hideTooltip),
        ExaminableProperty.of("hideAdditionalTooltip", hideAdditionalTooltip),
        ExaminableProperty.of("shownInTooltip", shownInTooltip),
        ExaminableProperty.of("slot", slot),
        ExaminableProperty.of("behaviors", behaviors),
        ExaminableProperty.of("cell", cell),
        ExaminableProperty.of("meta", meta),
    )

    override fun toString(): String = toSimpleString()

    // 必须最后执行验证，以保证所有 member properties 已经初始化
    init {
        NekoItemValidator.chain(
            BehaviorValidator(),
        ).validate(NekoItemValidator.Args(this))
    }
}