package cc.mewcraft.wakame.attribute

internal class AttributeProviderImpl : AttributeProvider {
    override val descriptionIds: Set<String>
        get() = Attributes.descriptionIds

    override fun getBy(descriptionId: String): Attribute? {
        return Attributes.getBy(descriptionId)
    }
}