@file:JvmName("FeatureConfig")

package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.ConfigAccess

@get:JvmName("featureConfig")
val FEATURE_CONFIG = ConfigAccess["features"]
