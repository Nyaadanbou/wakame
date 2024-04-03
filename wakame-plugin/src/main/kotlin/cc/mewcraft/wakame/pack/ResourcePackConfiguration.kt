package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.MAIN_CONFIG_NODE
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode

class ResourcePackConfiguration : KoinComponent {
    private val mainConfigNode: ConfigurationNode by reloadable { get(named(MAIN_CONFIG_NODE)) }

    //<editor-fold desc="Resource Pack Generation">
    val description: String by reloadable { mainConfigNode.node("resource_pack", "generation", "description").krequire() }
    //</editor-fold>

    //<editor-fold desc="Resource Pack Auto Upload">
    val enabled: Boolean by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "enabled").krequire() }
    val service: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "service").krequire() }
    val host: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "host").krequire() }
    val port: Int by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "port").krequire() }
    val appendPort: Boolean by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "append_port").krequire() }

    val githubUsername: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "username").krequire() }
    val githubToken: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "token").krequire() }
    val githubRepo: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "repo").krequire() }
    val githubBranch: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "branch").krequire() }
    val githubPath: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "path").krequire() }
    val githubCommitMessage: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "commit_message").krequire() }
    //</editor-fold>
}