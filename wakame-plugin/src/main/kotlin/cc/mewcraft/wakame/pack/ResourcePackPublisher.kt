package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.github.GithubRepoManager
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.io.File
import java.lang.reflect.Type

/**
 * 将资源包推送到第三方, 由第三方系统分发资源包给玩家.
 */
interface ResourcePackPublisher {
    fun publish()
}

data object NonePublisher : ResourcePackPublisher {
    override fun publish() {
        // Do nothing
    }
}

/**
 * 将资源包推送到特定 Github 仓库, 再由第三方系统进行分发.
 */
data class GithubPublisher(
    private val repo: String,
    private val username: String,
    private val token: String,
    private val remotePath: String,
    private val branch: String,
    private val commitMessage: String,
) : ResourcePackPublisher, KoinComponent {
    private val logger: Logger by inject()

    override fun publish() {
        logger.info("Publishing resource pack to Github")
        val pluginDataDir = PublisherSupport.pluginDataDir
        val manager = GithubRepoManager(
            localRepoPath = pluginDataDir.resolve("cache").resolve("repo"),
            resourcePackDirPath = pluginDataDir.resolve(GENERATED_RESOURCE_PACK_DIR),
            username = username,
            token = token,
            repo = repo,
            branch = branch,
            remotePath = remotePath,
            commitMessage = commitMessage,
        )

        manager.publishPack().onFailure {
            logger.error("Failed to publish resource pack", it)
        }
    }

}

private object PublisherSupport : KoinComponent {
    val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))
}

internal object ResourcePackPublisherSerializer : TypeSerializer<ResourcePackPublisher> {
    override fun deserialize(type: Type, node: ConfigurationNode): ResourcePackPublisher {
        return when (node.node("type").krequire<String>().lowercase()) {
            "github" -> {
                val username = node.node("username").krequire<String>()
                val token = node.node("token").krequire<String>()
                val repo = node.node("repo").krequire<String>()
                val branch = node.node("branch").krequire<String>()
                val path = node.node("path").krequire<String>()
                val commitMessage = node.node("commit_message").krequire<String>()
                return GithubPublisher(username, token, repo, branch, path, commitMessage)
            }

            else -> NonePublisher
        }
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ResourcePackPublisher? {
        return NonePublisher
    }
}