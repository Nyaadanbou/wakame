package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.mixin.core.InvokerCriteriaTriggers;

public class ExtraCriteriaTriggers {

    public static final KoishInventoryChangeTrigger INVENTORY_CHANGED = InvokerCriteriaTriggers.register("koish:inventory_changed", new KoishInventoryChangeTrigger());

    public static void bootstrap() {}

}
