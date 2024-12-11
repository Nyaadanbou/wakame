package cc.mewcraft.wakame.display2.implementation.recycling_station

import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required


@ConfigSerializable
internal data class SellButtonTitleRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val emptyInput: Part,
    @Required
    val unconfirmed: Part,
    @Required
    val confirmed: Part,
) : RendererFormat.Simple {
    override val id: String = "sell_button_title"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    @ConfigSerializable
    data class Part(
        val name: Component,
        val lore: List<Component> = emptyList(),
    )
}

@ConfigSerializable
internal data class SellButtonUsageRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val emptyInput: List<Component>,
    @Required
    val unconfirmed: List<Component>,
    @Required
    val confirmed: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_usage"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}

@ConfigSerializable
internal data class SellButtonItemListRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val withLevel: String,
    @Required
    val withoutLevel: String,
) : RendererFormat.Simple {
    override val id: String = "sell_button_item_list"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}

@ConfigSerializable
internal data class SellButtonTotalWorthRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val totalWorth: List<String>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_total_worth"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}
