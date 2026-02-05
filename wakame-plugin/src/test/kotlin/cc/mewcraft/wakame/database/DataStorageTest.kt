package cc.mewcraft.wakame.database

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.config.KoishConfigs
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals

class DataStorageTest {
    companion object {
        @OptIn(TestOnly::class)
        @BeforeAll
        @JvmStatic
        fun setup() {
            KoishDataPaths.initializeForTest(TestPath.TEST)
            ConfigAccess.setImplementation(KoishConfigs)
            DataStorageInitializer.init()

            transaction {
                SchemaUtils.drop(Users)
                SchemaUtils.create(Users)
            }
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            transaction {
                SchemaUtils.drop(Users)
            }
            DataStorageInitializer.disable()
        }
    }

    @Test
    fun `test inserting and reading data by dsl`() {
        transaction {
            Users.insert {
                it[name] = "test_user"
                it[password] = "test_password"
            }

            val user = Users
                .selectAll()
                .where { Users.name eq "test_user" }
                .singleOrNull()

            assert(user != null) { "User should be inserted successfully." }
            assertEquals("test_password", user?.get(Users.password), "Password should match.")
            assertEquals(false, user?.get(Users.isBanned), "User should not be banned by default.")
            assert(user?.get(Users.createdAt) != null) { "User creation time should be set." }
        }
    }

    @Test
    fun `test modifying and reading data by dsl`() {
        transaction {
            Users.insert {
                it[name] = "test_user_to_ban"
                it[password] = "test_password"
            }

            Users.update({ Users.name eq "test_user_to_ban" }) {
                it[isBanned] = true
            }

            val user = Users
                .selectAll()
                .where { Users.name eq "test_user_to_ban" }
                .singleOrNull()

            assert(user != null) { "User should exist after update." }
            assertEquals(true, user?.get(Users.isBanned), "User should be banned after update.")
        }
    }

    @Test
    fun `test inserting and reading data by dao`() {
        transaction {
            DaoUser.new {
                name = "dao_test_user"
                password = "dao_test_password"
                isBanned = true
            }

            val foundUser = DaoUser.find { Users.name eq "dao_test_user" }.first()

            assertEquals("dao_test_user", foundUser.name, "DAO User name should match.")
            assertEquals("dao_test_password", foundUser.password, "DAO User password should match.")
            assertEquals(true, foundUser.isBanned, "DAO User should be banned.")
        }
    }

    @Test
    fun `test modifying and reading data by dao`() {
        transaction {
            val user = DaoUser.new {
                name = "dao_test_user_to_unban"
                password = "dao_test_password"
                isBanned = true
            }

            user.isBanned = false

            val foundUser = DaoUser.find { Users.name eq "dao_test_user_to_unban" }.first()

            assertEquals(false, foundUser.isBanned, "DAO User should not be banned after modification.")
        }
    }
}