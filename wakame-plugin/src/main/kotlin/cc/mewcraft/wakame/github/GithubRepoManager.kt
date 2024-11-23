package cc.mewcraft.wakame.github

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.RepositoryState
import org.eclipse.jgit.transport.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.io.File


class GithubRepoManager(
    private val localRepoPath: File,
    private val resourcePackDirPath: File,
    username: String,
    token: String,
    repo: String,
    branch: String,
    private val remotePath: String,
    private val commitMessage: String,
) {
    companion object : KoinComponent {
        private val logger: Logger by inject()

        /**
         * 分析 PullResult 的状态
         * @param pullResult PullResult 对象
         * @return 成功信息或失败原因
         */
        fun analyzePullResult(pullResult: PullResult?): String? {
            if (pullResult == null) {
                return "Pull operation failed: PullResult is null."
            }

            if (pullResult.isSuccessful) {
                return null
            }

            val errorMessage = StringBuilder("Pull operation failed.")

            // 检查合并结果
            val mergeResult = pullResult.mergeResult
            if (mergeResult != null) {
                errorMessage.append("\nMerge Status: ${mergeResult.mergeStatus}")

                // 检查冲突文件
                if (mergeResult.conflicts != null && mergeResult.conflicts.isNotEmpty()) {
                    errorMessage.append("\nConflicts detected in the following files:")
                    mergeResult.conflicts.keys.forEach { conflictFile ->
                        errorMessage.append("\n - $conflictFile")
                    }
                } else {
                    errorMessage.append("\nNo specific conflict files were detected.")
                }

                // 检查失败的详细原因（如果有）
                if (mergeResult.failingPaths != null && mergeResult.failingPaths.isNotEmpty()) {
                    errorMessage.append("\nMerge failed for the following paths:")
                    mergeResult.failingPaths.forEach { (path, reason) ->
                        errorMessage.append("\n - $path: $reason")
                    }
                }
            } else {
                errorMessage.append(" No merge result available.")
            }

            return errorMessage.toString()
        }

        /**
         * 分析 PushResult 的状态
         * @param pushResults Iterable<PushResult> 对象
         *
         * @return 成功为 null, 或失败原因
         */
        fun analyzePushResults(pushResults: Iterable<PushResult>?): String? {
            if (pushResults == null) {
                return "Push operation failed: PushResult is null."
            }

            val errorMessage = StringBuilder()
            var allSuccessful = true

            for (pushResult in pushResults) {
                for (update in pushResult.remoteUpdates) {
                    val status = update.status
                    if (status != RemoteRefUpdate.Status.OK && status != RemoteRefUpdate.Status.UP_TO_DATE) {
                        allSuccessful = false
                        errorMessage.append("Push failed for ref ${update.remoteName}: $status\n")
                        update.message?.let { message ->
                            errorMessage.append("Message: $message\n")
                        }
                    }
                }
            }

            return if (allSuccessful) null else errorMessage.toString()
        }
    }

    private val credentialsProvider = UsernamePasswordCredentialsProvider(username, token)
    private val git: Git

    init {
        if (!localRepoPath.exists() || !localRepoPath.isDirectory || !localRepoPath.resolve(".git").exists()) {
            localRepoPath.mkdirs()
            git = Git.cloneRepository()
                .setURI("https://github.com/$repo.git")
                .setDirectory(localRepoPath)
                .setRemote("origin")
                .setBranch(branch)
                .setCredentialsProvider(credentialsProvider)
                .call()
        } else git = Git.open(localRepoPath)
    }

    /**
     * 将资源包推送到远程仓库.
     *
     * @return 推送结果的信息.
     */
    fun publishPack(): Result<Unit> {
        try {
            val repo = git.repository

            // 检查仓库状态
            if (repo.repositoryState != RepositoryState.SAFE) {
                logger.error("Repository is in an invalid state: ${repo.repositoryState}")
                if (repo.repositoryState == RepositoryState.MERGING_RESOLVED) {
                    logger.info("Committing resolved merge...")
                    git.commit().setMessage("Merge resolved").call()
                } else {
                    return Result.failure(
                        IllegalStateException("Cannot pull: Repository is in an invalid state: ${repo.repositoryState}")
                    )
                }
            }

            // 删除本地 git 仓库中指定文件夹下的所有文件
            git.rm().addFilepattern(remotePath).call()

            // 拉取最新分支信息
            val pullResult = git.pull()
                .setCredentialsProvider(credentialsProvider)
                .call()
            val pullError = analyzePullResult(pullResult)
            if (pullError != null) {
                error("Pull operation failed: $pullError")
            }

            // 确保资源包正确复制到目标路径
            resourcePackDirPath.copyRecursively(localRepoPath.resolve(remotePath), true)

            // 将本地的资源文件夹下的所有文件添加到本地 git 仓库
            git.add().addFilepattern(remotePath).call()

            // 提交更改
            val commit = git.commit().setMessage(commitMessage).call()
            logger.info("Committed with ID: ${commit.id}")

            // 推送到远程 git 仓库
            val pushResults = git.push()
                .setCredentialsProvider(credentialsProvider)
                .call()
            val pushError = analyzePushResults(pushResults)
            if (pushError != null) {
                error("Push operation failed: $pushError")
            }

        } catch (e: GitAPIException) {
            logger.error("Git API Exception occurred", e)
            e.cause?.let { cause ->
                logger.error("Cause: ", cause)
            }
            return Result.failure(e)
        } catch (e: Exception) {
            logger.error("Unexpected exception", e)
            return Result.failure(e)
        }

        return Result.success(Unit)
    }



}