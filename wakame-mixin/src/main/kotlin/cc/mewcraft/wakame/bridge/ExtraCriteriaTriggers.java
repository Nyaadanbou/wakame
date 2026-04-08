package cc.mewcraft.wakame.bridge;

import cc.mewcraft.wakame.mixin.InvokerCriteriaTriggers;

public class ExtraCriteriaTriggers {
    public static final KoishInventoryChangeTrigger INVENTORY_CHANGED = InvokerCriteriaTriggers.register(
            "koish:inventory_changed", new KoishInventoryChangeTrigger()
    );

    public static void bootstrap() {}
}
