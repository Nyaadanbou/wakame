package cc.mewcraft.wakame.ability

/**
 * 需要额外读写持久性数据的技能。
 *
 * 例如，记录该技能积攒了多少灵魂（写入），并且在释放技能时将灵魂释放出去（读取+写入）。
 */
interface PersistentDataAbility {
}