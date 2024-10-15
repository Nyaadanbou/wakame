package cc.mewcraft.wakame.compatibility.mechanic

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeAccessors
import cc.mewcraft.wakame.attribute.AttributeProvider
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.core.logging.MythicLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object MythicMobsMechanicSupport : KoinComponent {
    val attributeProvider: AttributeProvider by inject()
    val attributeAccessors: AttributeAccessors by inject()

    fun getAttributeFromConfig(config: MythicLineConfig): Attribute {
        return config.getString(arrayOf("attribute", "attr"))?.let { attrString ->
            attributeProvider.getBy(attrString) ?: attributeProvider.empty().also { MythicLogger.errorGenericConfig("Attribute $it not found in WakameAttributes.") }
        } ?: attributeProvider.empty().also { MythicLogger.errorGenericConfig("Attribute property not found") }
    }
}