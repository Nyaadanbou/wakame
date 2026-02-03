package cc.mewcraft.wakame.config

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.api.config.PrimaryConfig
import cc.mewcraft.wakame.feature.FEATURE_CONFIG
import net.kyori.adventure.key.Key

class PrimaryConfigImpl : PrimaryConfig {
    override val netherPortalFunctionalDimensions: Set<Key> by FEATURE_CONFIG.entryOrElse(emptySet(), "nether_portal_functional_dimensions")
    override val printStackOnSetHealthScale: Boolean by MAIN_CONFIG.entryOrElse(false, "print_stack_on_set_health_scale")
}