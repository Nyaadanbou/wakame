package cc.mewcraft.extracontexts.common.example

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import org.slf4j.Logger
import java.util.*


fun registerDummyKeyValuePairs(log: Logger) {
    log.info("========== ExtraContexts KeyValueStoreManager 功能测试 ==========")

    val uuid1 = UUID.randomUUID()
    log.info("生成测试 UUID: $uuid1")

    // ===== 测试 1: 设置和获取单个值 =====
    log.info("\n[测试 1] 设置和获取单个值")
    KeyValueStoreManager.set(uuid1, "example_key", "example_value")
    log.info("  - 设置: example_key = example_value")

    val retrievedValue = KeyValueStoreManager.get(uuid1, "example_key")
    log.info("  - 获取: example_key = $retrievedValue")
    assert(retrievedValue == "example_value") { "获取的值与设置的值不匹配" }
    log.info("  ✓ 测试通过")

    // ===== 测试 2: 设置带命名空间的键 =====
    log.info("\n[测试 2] 设置带命名空间的键")
    KeyValueStoreManager.set(uuid1, "namespace:key", "namespaced_value")
    log.info("  - 设置: namespace:key = namespaced_value")

    val namespacedValue = KeyValueStoreManager.get(uuid1, "namespace:key")
    log.info("  - 获取: namespace:key = $namespacedValue")
    assert(namespacedValue == "namespaced_value") { "命名空间值不匹配" }
    log.info("  ✓ 测试通过")

    // ===== 测试 3: 批量设置数据 =====
    log.info("\n[测试 3] 批量设置数据")
    val testData = mapOf(
        "config:language" to "zh_CN",
        "config:theme" to "dark",
        "profile:level" to "10",
        "profile:experience" to "5000",
        "profile:rank" to "gold"
    )

    testData.forEach { (key, value) ->
        KeyValueStoreManager.set(uuid1, key, value)
        log.info("  - 设置: $key = $value")
    }
    log.info("  ✓ 批量设置完成")

    // ===== 测试 4: 存在性检查 =====
    log.info("\n[测试 4] 存在性检查")
    val existsResult1 = KeyValueStoreManager.exists(uuid1, "example_key")
    log.info("  - 检查 example_key 存在: $existsResult1")
    assert(existsResult1) { "example_key 应该存在" }

    val existsResult2 = KeyValueStoreManager.exists(uuid1, "non_existent_key")
    log.info("  - 检查 non_existent_key 存在: $existsResult2")
    assert(!existsResult2) { "non_existent_key 不应该存在" }
    log.info("  ✓ 测试通过")

    // ===== 测试 5: 获取所有键值对 =====
    log.info("\n[测试 5] 获取所有键值对")
    val allPairs = KeyValueStoreManager.get(uuid1)
    log.info("  - 获取所有键值对，共 ${allPairs.size} 条:")
    allPairs.forEach { (key, value) ->
        log.info("    · $key = $value")
    }
    assert(allPairs.isNotEmpty()) { "应该至少有一个键值对" }
    log.info("  ✓ 测试通过")

    // ===== 测试 6: 删除单个键 =====
    log.info("\n[测试 6] 删除单个键")
    KeyValueStoreManager.delete(uuid1, "config:theme")
    log.info("  - 删除: config:theme")

    val afterDelete = KeyValueStoreManager.get(uuid1, "config:theme")
    log.info("  - 删除后获取: config:theme = $afterDelete")
    assert(afterDelete == null) { "config:theme 应该被删除" }
    log.info("  ✓ 测试通过")

    // ===== 测试 7: 更新值 =====
    log.info("\n[测试 7] 更新值")
    val oldValue = KeyValueStoreManager.get(uuid1, "profile:level")
    log.info("  - 旧值: profile:level = $oldValue")

    KeyValueStoreManager.set(uuid1, "profile:level", "25")
    log.info("  - 更新为: profile:level = 25")

    val newValue = KeyValueStoreManager.get(uuid1, "profile:level")
    log.info("  - 验证: profile:level = $newValue")
    assert(newValue == "25") { "profile:level 应该被更新为 25" }
    log.info("  ✓ 测试通过")

    // ===== 测试 8: 使用多个 UUID =====
    log.info("\n[测试 8] 使用多个 UUID 的独立性")
    val uuid2 = UUID.randomUUID()
    log.info("  - 生成另一个 UUID: $uuid2")

    KeyValueStoreManager.set(uuid2, "user:name", "player2")
    log.info("  - 在其他 UUID 中设置: user:name = player2")

    val firstUserValue = KeyValueStoreManager.get(uuid1, "user:name")
    val secondUserValue = KeyValueStoreManager.get(uuid2, "user:name")
    log.info("  - UUID1 中的 user:name: $firstUserValue")
    log.info("  - UUID2 中的 user:name: $secondUserValue")
    assert(firstUserValue == null) { "UUID1 中不应该有 user:name" }
    assert(secondUserValue == "player2") { "UUID2 中的 user:name 应该是 player2" }
    log.info("  ✓ 测试通过")

    // ===== 测试数据保留 =====
    log.info("\n========== 测试数据保留 ==========")
    log.info("UUID1 ($uuid1) 中的数据:")
    KeyValueStoreManager.get(uuid1).forEach { (key, value) -> log.info("  · $key = $value") }

    log.info("UUID2 ($uuid2) 中的数据:")
    KeyValueStoreManager.get(uuid2).forEach { (key, value) -> log.info("  · $key = $value") }

    log.info("\n========== 所有测试完成！==========")
    log.info("✓ 功能测试: 通过")
    log.info("✓ 测试数据已保留在数据库中")
    log.info("✓ UUID1: $uuid1")
    log.info("✓ UUID2: $uuid2")
}