package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import org.koin.core.component.KoinComponent

class ResourcePackConfiguration : KoinComponent {
    private val config = MAIN_CONFIG.node("resource_pack")

    //<editor-fold desc="Resource Pack Generation">
    val description: String by config.entry("generation", "description")
    //</editor-fold>

    //<editor-fold desc="Resource Pack Auto Upload">
    val enabled: Boolean by config.entry("auto_upload", "enabled")
    val service: String by config.entry("auto_upload", "service")
    val host: String by config.entry("auto_upload", "host")
    val port: Int by config.entry("auto_upload", "port")
    val appendPort: Boolean by config.entry("auto_upload", "append_port")

    val githubUsername: String by config.entry("auto_upload", "github", "username")
    val githubToken: String by config.entry("auto_upload", "github", "token")
    val githubRepo: String by config.entry("auto_upload", "github", "repo")
    val githubBranch: String by config.entry("auto_upload", "github", "branch")
    val githubPath: String by config.entry("auto_upload", "github", "path")
    val githubCommitMessage: String by config.entry("auto_upload", "github", "commit_message")
    //</editor-fold>
}