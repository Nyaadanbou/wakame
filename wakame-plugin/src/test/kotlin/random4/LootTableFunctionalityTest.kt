package random4

import cc.mewcraft.wakame.random4.LootPool
import cc.mewcraft.wakame.random4.LootTable
import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.random4.entry.AlternativesEntry
import cc.mewcraft.wakame.random4.entry.LootPoolSingletonContainer
import cc.mewcraft.wakame.random4.entry.SequentialEntry
import cc.mewcraft.wakame.random4.predicate.LootPredicate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LootTableFunctionalityTest {

    @Test
    fun `test singleton loot table`() {
        val lootPools = listOf(
            LootPool(
                rolls = 1,
                conditions = emptyList(),
                entries = listOf(Loot(42))
            )
        )

        val lootTable = LootTable(lootPools)
        val selected = lootTable.select(LootContext.EMPTY)
        assertEquals(1, selected.size, "Expected 1 loot entry to be selected")
        println("Singleton loot entry test: $selected")
        assertEquals(42, selected[0], "Expected the loot entry to be 42")
    }

    @Test
    fun `test multiple loot pools`() {
        val lootPools = listOf(
            LootPool(
                rolls = 1,
                conditions = emptyList(),
                entries = listOf(Loot(10))
            ),
            LootPool(
                rolls = 1,
                conditions = emptyList(),
                entries = listOf(Loot(20), Loot(30))
            )
        )

        val lootTable = LootTable(lootPools)
        val selected = lootTable.select(LootContext.EMPTY)
        assertEquals(2, selected.size, "Expected 2 loot entries to be selected")
        println("Multiple loot pools test: $selected")
        assertEquals(10, selected[0], "Expected the first loot entry to be 10")
        assertTrue("Expected the second loot entry to be either 20 or 30") {
            selected[1] == 20 || selected[1] == 30
        }
    }

    @Test
    fun `test alternatives loot`() {
        val lootPools = listOf(
            LootPool(
                rolls = 2,
                conditions = emptyList(),
                entries = listOf(
                    AlternativesEntry(
                        children = listOf(
                            Loot(50, conditions = listOf(LootPredicate { false })), // This will not be selected
                            Loot(100), // This will be selected
                            Loot(150), // This will not be selected
                        ),
                        conditions = emptyList()
                    )
                )
            )
        )

        val lootTable = LootTable(lootPools)
        val selected = lootTable.select(LootContext.EMPTY)
        assertEquals(2, selected.size, "Expected 1 loot entry to be selected")
        println("Alternatives loot entry test: $selected")
        // 只会出现: [100, 100]
        assertEquals(selected, listOf(100, 100), "Expected the loot entries to be [100, 100]")
    }

    @Test
    fun `test sequential loot`() {
        val lootPools = listOf(
            LootPool(
                rolls = 2,
                conditions = emptyList(),
                entries = listOf(
                    SequentialEntry(
                        children = listOf(
                            Loot(100), // This may be selected
                            Loot(150), // This may be selected
                            Loot(200, conditions = listOf(LootPredicate { false })), // This will not be selected
                            Loot(250), // This will not be selected as it comes after a failed condition
                        ),
                        conditions = emptyList()
                    ),
                )
            )
        )

        val lootTable = LootTable(lootPools)
        val selected = lootTable.select(LootContext.EMPTY)
        assertEquals(2, selected.size, "Expected 2 loot entries to be selected in sequence")
        println("Sequential loot entry test: $selected")

        // 可能会出现三种情况: [100, 150], [150, 100], [100, 100]
        assertTrue("Expected the loot entries to be in sequence, either [100, 150], [150, 100], or [100, 100]") {
            (selected[0] == 100 && selected[1] == 150) ||
            (selected[0] == 150 && selected[1] == 100) ||
            (selected[0] == 100 && selected[1] == 100)
        }
    }

    class Loot(
        val data: Int,
        weight: Int = 1,
        quality: Int = 0,
        conditions: List<LootPredicate> = emptyList(),
    ) : LootPoolSingletonContainer<Int>(weight, quality, conditions) {
        override fun createData(context: LootContext, dataConsumer: (Int) -> Unit) {
            dataConsumer(data)
        }
    }
}