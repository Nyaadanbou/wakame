package cc.mewcraft.wakame.github

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
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

    fun publishPack(): Result<Unit> {
        try {
            // 删除远程仓库中指定文件夹下的所有文件
            git.rm().addFilepattern(remotePath).call()

            // 将本地的资源文件夹下的所有文件添加到仓库
            resourcePackDirPath.copyRecursively(localRepoPath.resolve(remotePath), true)
            git.add().addFilepattern(remotePath).call()

            // 提交更改
            git.commit().setMessage(commitMessage).call()
            // 推送到远程仓库
            git.push()
                .setCredentialsProvider(credentialsProvider)
                .call()
            return Result.success(Unit)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

}