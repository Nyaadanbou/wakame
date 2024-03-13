package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer.MAIN_CONFIG_NODE
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.requireKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode

class ResourcePackConfiguration : KoinComponent {
    private val mainConfigNode: ConfigurationNode by reloadable { get(named(MAIN_CONFIG_NODE)) }

    //<editor-fold desc="Resource Pack Generation">
    val description: String by reloadable { mainConfigNode.node("resource_pack", "generation", "description").requireKt() }
    //</editor-fold>

    //<editor-fold desc="Resource Pack Auto Upload">
    val enabled: Boolean by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "enabled").requireKt() }
    val service: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "service").requireKt() }
    val host: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "host").requireKt() }
    val port: Int by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "port").requireKt() }
    val appendPort: Boolean by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "append_port").requireKt() }

    val githubUsername: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "username").requireKt() }
    val githubToken: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "token").requireKt() }
    val githubRepo: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "repo").requireKt() }
    val githubBranch: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "branch").requireKt() }
    val githubPath: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "path").requireKt() }
    val githubCommitMessage: String by reloadable { mainConfigNode.node("resource_pack", "auto_upload", "github", "commit_message").requireKt() }
    //</editor-fold>
}