package cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule;

import org.betonquest.betonquest.api.config.quest.QuestPackageManager;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.schedule.ScheduleID;
import org.betonquest.betonquest.kernel.processor.quest.VariableProcessor;
import org.betonquest.betonquest.schedule.impl.BaseScheduleFactory;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Factory to create {@link GameTickSchedule} instances.
 *
 * <p>Time format: a positive integer string representing the interval in Minecraft ticks.
 * For example, time: "10" means the schedule runs every 10 ticks.</p>
 */
public class GameTickScheduleFactory extends BaseScheduleFactory<GameTickSchedule> {

    /**
     * Create a new Game Tick Schedule Factory.
     *
     * @param variableProcessor the variable processor to create new variables
     * @param packManager       the quest package manager to get quest packages from
     */
    public GameTickScheduleFactory(final VariableProcessor variableProcessor, final QuestPackageManager packManager) {
        super(variableProcessor, packManager);
    }

    @Override
    public GameTickSchedule createNewInstance(final ScheduleID scheduleID, final ConfigurationSection config)
            throws QuestException {
        final ScheduleData scheduleData = parseScheduleData(scheduleID.getPackage(), config);
        final String time = scheduleData.time();
        final int interval;
        try {
            interval = Integer.parseInt(time.trim());
        } catch (final NumberFormatException e) {
            throw new QuestException("Unable to parse time '" + time + "' as tick interval: " + e.getMessage(), e);
        }
        if (interval <= 0L) {
            throw new QuestException("Time must be a positive integer tick interval, got '" + time + "'");
        }
        return new GameTickSchedule(scheduleID, scheduleData.events(), scheduleData.catchup(), interval);
    }
}

