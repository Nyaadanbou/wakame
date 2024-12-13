package cc.mewcraft.wakame.display2.implementation.recycling_station

import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable


@ConfigSerializable
internal data class SellButtonTitleRendererFormat(
    override val namespace: String,
    val emptyInput: Part,
    val unconfirmed: Part,
    val confirmed: Part,
) : RendererFormat.Simple {
    override val id: String = "sell_button_title"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    @ConfigSerializable
    data class Part(
        val name: Component,
        val lore: List<Component> = emptyList(),
    )
}

@ConfigSerializable
internal data class SellButtonUsageRendererFormat(
    override val namespace: String,
    val emptyInput: List<Component>,
    val unconfirmed: List<Component>,
    val confirmed: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_usage"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
}

@ConfigSerializable
internal data class SellButtonItemListRendererFormat(
    override val namespace: String,
    val withLevel: String,
    val withoutLevel: String,
) : RendererFormat.Simple {
    override val id: String = "sell_button_item_list"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
}

@ConfigSerializable
internal data class SellButtonTotalWorthRendererFormat(
    override val namespace: String,
    val totalWorth: List<String>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_total_worth"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)
}
