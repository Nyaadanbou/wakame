package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.github.GithubRepoManager
import java.io.File

/**
 * 负责将资源包推送到指定的位置, 比如 Github 仓库.
 */
interface ResourcePackPublisher {
    /**
     * 推送资源包.
     */
    fun publish(): Boolean

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
        val config = RESOURCE_PACK_CONFIG.node("publisher")
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
    override fun publish(): Boolean = true
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
) : ResourcePackPublisher {

    override fun publish(): Boolean {
        LOGGER.info("Publishing resource pack to Github (repo: $repo, branch: $branch, path: $remotePath)")
        val dataFolder = Injector.get<File>(InjectionQualifier.DATA_FOLDER)
        val manager = GithubRepoManager(
            localRepoPath = dataFolder.resolve(".cache/repo"),
            resourcePackDirPath = dataFolder.resolve(GENERATED_RESOURCE_PACK_DIR),
            username = username,
            token = token,
            repo = repo,
            branch = branch,
            remotePath = remotePath,
            commitMessage = commitMessage,
        )

        manager.publishPack()
            .onFailure {
                LOGGER.error("Failed to publish resource pack to Github", it)
                return false
            }

        return true
    }

    override fun cleanup() {
        LOGGER.info("Cleaning up Github publisher")
    }
}
