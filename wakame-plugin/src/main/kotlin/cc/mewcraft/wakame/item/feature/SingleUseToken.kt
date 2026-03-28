package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.database.DatabaseManager
import cc.mewcraft.wakame.item.datagen.impl.MetaSingleUseToken
import cc.mewcraft.wakame.item.token.SingleUseTokenRepository
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.util.*

/**
 * 已消耗的一次性令牌数据库表.
 *
 * 以令牌 (`token`) 作为主键, 保证同一个令牌只能被插入一次.
 */
object RedeemedSingleUseTokensTable : Table("koish_redeemed_single_use_tokens") {
    val token = varchar("token", MetaSingleUseToken.TOKEN_LENGTH)
    val playerId = varchar("player_id", 36) // UUID string
    val redeemedAt = long("redeemed_at") // epoch seconds

    override val primaryKey = PrimaryKey(token)
}

/**
 * 基于 Exposed 的 [SingleUseTokenRepository] 数据库实现.
 *
 * 使用 `insertIgnore` (即 `INSERT ... ON CONFLICT DO NOTHING`) 来实现原子性唯一插入,
 * 确保每个令牌只能被成功消耗一次.
 *
 * @param db 用于该仓库的 Exposed Database 实例
 */
class ExposedSingleUseTokenRepository(
    private val db: Database,
) : SingleUseTokenRepository {

    /**
     * 初始化表结构. 应在模块启动时调用一次.
     */
    fun createSchemaIfNeeded() {
        transaction(db) {
            SchemaUtils.create(RedeemedSingleUseTokensTable)
        }
    }

    override fun markRedeemed(token: String, playerId: UUID): Boolean {
        return transaction(db) {
            val result = RedeemedSingleUseTokensTable.insertIgnore {
                it[RedeemedSingleUseTokensTable.token] = token
                it[RedeemedSingleUseTokensTable.playerId] = playerId.toString()
                it[RedeemedSingleUseTokensTable.redeemedAt] = Instant.now().epochSecond
            }
            // insertIgnore 返回的 insertedCount 为 0 表示该令牌已存在
            result.insertedCount > 0
        }
    }
}

/**
 * 一次性令牌数据库模块的初始化器.
 */
@Init(InitStage.POST_WORLD)
object SingleUseTokenDatabaseInitializer {

    @InitFun
    fun init() {
        LOGGER.info("[SingleUseToken] Initializing single-use token database...")
        val repository = ExposedSingleUseTokenRepository(DatabaseManager.database())
        repository.createSchemaIfNeeded()
        SingleUseTokenRepository.setImplementation(repository)
        LOGGER.info("[SingleUseToken] Single-use token database initialized.")
    }
}
