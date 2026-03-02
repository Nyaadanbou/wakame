package cc.mewcraft.wakame.feature.jeicompat

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.feature.FEATURE_CONFIG

object JetCompatConfig {

    val enable by FEATURE_CONFIG.entryOrElse(false, "jei_compatibility")
}