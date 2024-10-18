package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.ElementRegistry

internal class AttributeProviderImpl : AttributeProvider {
    override val descriptionIds: Set<String>
        get() = Attributes.descriptionIds

    override fun getBy(descriptionId: String): Attribute? {
        val simpleAttribute = Attributes.getBy(descriptionId)
        if (simpleAttribute != null) {
            return simpleAttribute
        }

        for ((_, element) in ElementRegistry.INSTANCES) {
            val elementAttribute = Attributes.element(element).getBy(descriptionId)
            if (elementAttribute != null) {
                return elementAttribute
            }
        }

        return null
    }
}