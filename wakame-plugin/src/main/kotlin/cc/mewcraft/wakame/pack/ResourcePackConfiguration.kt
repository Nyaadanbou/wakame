package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.config.config
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent

class ResourcePackConfiguration : KoinComponent {
    //<editor-fold desc="Resource Pack Generation">
    val description: String by config("resource_pack", "generation", "description") { krequire() }
    //</editor-fold>

    //<editor-fold desc="Resource Pack Auto Upload">
    val enabled: Boolean by config("resource_pack", "auto_upload", "enabled") { krequire() }
    val service: String by config("resource_pack", "auto_upload", "service") { krequire() }
    val host: String by config("resource_pack", "auto_upload", "host") { krequire() }
    val port: Int by config("resource_pack", "auto_upload", "port") { krequire() }
    val appendPort: Boolean by config("resource_pack", "auto_upload", "append_port") { krequire() }

    val githubUsername: String by config("resource_pack", "auto_upload", "github", "username") { krequire() }
    val githubToken: String by config("resource_pack", "auto_upload", "github", "token") { krequire() }
    val githubRepo: String by config("resource_pack", "auto_upload", "github", "repo") { krequire() }
    val githubBranch: String by config("resource_pack", "auto_upload", "github", "branch") { krequire() }
    val githubPath: String by config("resource_pack", "auto_upload", "github", "path") { krequire() }
    val githubCommitMessage: String by config("resource_pack", "auto_upload", "github", "commit_message") { krequire() }
    //</editor-fold>
}