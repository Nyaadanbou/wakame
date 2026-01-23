@file:JvmName("Configs")

package cc.mewcraft.lazyconfig

import cc.mewcraft.lazyconfig.access.ConfigAccess
import org.spongepowered.configurate.CommentedConfigurationNode
import xyz.xenondevs.commons.provider.Provider


/**
 * 主配置文件.
 */
@get:JvmName("main")
val MAIN_CONFIG: Provider<CommentedConfigurationNode> = ConfigAccess["config"]
