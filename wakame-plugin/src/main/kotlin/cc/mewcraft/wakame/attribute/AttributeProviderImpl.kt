package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.ElementRegistry

internal class AttributeProviderImpl : AttributeProvider {
    override fun getBy(descriptionId: String): Attribute? {
        val simpleAttribute =  Attributes.getBy(descriptionId)
        if (simpleAttribute != null) {
            return simpleAttribute
        }

        for (element in ElementRegistry.INSTANCES.values) {
            val elementAttributes = Attributes.element(element)
            val attribute = elementAttributes.getBy(descriptionId)
            if (attribute != null) {
                return attribute
            }
        }

        return null
    }

    override fun allDescriptionId(): List<String> {
        val simpleAttributeIds = Attributes.allDescriptionId().toMutableList()
        for (element in ElementRegistry.INSTANCES.values) {
            simpleAttributeIds.addAll(Attributes.element(element).allDescriptionId())
        }
        return simpleAttributeIds
    }

    override fun empty(): Attribute {
        return Attributes.EMPTY
    }
}