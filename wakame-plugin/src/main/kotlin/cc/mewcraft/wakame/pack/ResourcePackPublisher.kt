package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.github.GithubRepoManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

/**
 * 负责将资源包推送到指定的位置, 比如 Github 仓库.
 */
interface ResourcePackPublisher {
    /**
     * 推送资源包.
     */
    fun publish()

    /**
     * 执行清理逻辑.
     */
    fun cleanup()
}

/**
 * 负责提供 [ResourcePackPublisher] 实例给外部使用.
 */
object ResourcePackPublisherProvider {
    private var INSTANCE: ResourcePackPublisher? = null

    /**
     * 获取当前的 [ResourcePackPublisher] 实例.
     *
     * 警告: 非线程安全!
     */
    fun get(): ResourcePackPublisher {
        return INSTANCE ?: loadAndSet()
    }

    /**
     * 重新加载并设置新的 [ResourcePackPublisher] 实例.
     *
     * 警告: 非线程安全!
     */
    fun loadAndSet(): ResourcePackPublisher {
        val config = RESOURCE_PACK_CONFIG.derive("publisher")
        val inst = when (
            val type = config.entry<String>("type").get()
        ) {
            "none" -> {
                NonePublisher
            }

            "github" -> {
                val username = config.entry<String>("username").get()
                val token = config.entry<String>("token").get()
                val repo = config.entry<String>("repo").get()
                val branch = config.entry<String>("branch").get()
                val path = config.entry<String>("path").get()
                val commitMessage = config.entry<String>("commit_message").get()
                GithubPublisher(
                    repo = repo,
                    username = username,
                    token = token,
                    remotePath = path,
                    branch = branch,
                    commitMessage = commitMessage
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown publisher type: '$type'")
            }
        }

        return inst.also {
            INSTANCE = it
        }
    }
}

/* Internals */


/**
 * 无操作.
 */
private data object NonePublisher : ResourcePackPublisher {
    override fun publish() = Unit
    override fun cleanup() = Unit
}

/**
 * 将资源包推送到特定的 Github 仓库.
 */
private data class GithubPublisher(
    private val repo: String,
    private val username: String,
    private val token: String,
    private val remotePath: String,
    private val branch: String,
    private val commitMessage: String,
) : ResourcePackPublisher, KoinComponent {
    private val logger: Logger by inject()
    private val pluginDataDir: File by inject(named(PLUGIN_DATA_DIR))

    override fun publish() {
        logger.info("Publishing resource pack to Github (repo: $repo, branch: $branch, path: $remotePath)")
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
            logger.error("Failed to publish resource pack to Github", it)
        }
    }

    override fun cleanup() {
        logger.info("Cleaning up Github publisher")
    }
}
