package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.attribute.base.AttributeMap
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.util.toStableInt

class ResourceSupplier(
    private val attributeMap: AttributeMap,
) {
    fun initialValue(type: ResourceType): Int {
        return 0 // always return 0
    }

    fun maximumValue(type: ResourceType): Int {
        return when (type) {
            /* FIXME Register new resource here */

            ResourceType.MANA -> attributeMap.getValue(Attributes.MAX_MANA)

        }.toStableInt()
    }
}