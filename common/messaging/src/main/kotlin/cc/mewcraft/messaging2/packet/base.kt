/**
 *
 * ### 开发指南
 *
 * 理论上所有的封包类都可以直接定义在核心模块中, 因为封包类所包含的数据类型应该都是可以由基础数据类型推导出来的
 * (也就是仅使用 [cc.mewcraft.messaging2.handler.SimplePacket] 中 `write...` 开头的函数就能完成读写),
 * 这些数据类型也应该都是核心模块可以直接访问到的.
 *
 * 但如果某个功能需要直接从封包类直接读写一个比较复杂的数据类型, 那么应该在那个模块内定义扩展函数.
 */
package cc.mewcraft.messaging2.packet

/**
 * 这是一个标记接口, 表示一个自定义封包类的处理逻辑.
 */
interface SimplePacketHandler