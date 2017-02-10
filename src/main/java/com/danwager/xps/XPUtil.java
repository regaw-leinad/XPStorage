package com.danwager.xps;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for xp calculation
 *
 * @author Dan Wager
 */
public class XPUtil {

    // Map to cache calculations for how much xp a player needs to go from one level to the next
    private static Map<Integer, Integer> currentLevelToXpRequired;

    static {
        currentLevelToXpRequired = new HashMap<>();
    }

    /**
     * Gets the amount of xp required to go from the specified level to the next level
     *
     * @param level The level to start from
     * @return The amount of xp required to fully level up to the next level
     */
    public static int getXpRequiredFromLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("level can not be < 0");
        }

        return currentLevelToXpRequired.computeIfAbsent(level, XPUtil::calculateXpForLevel);
    }

    /**
     * Gets the amount of xp required to go from the specified level to the next level,
     * taking into account how far the level has already been progressed
     *
     * @param level The level to start from
     * @param levelProgress A value from 0.0 to 1.0 indicating the progress towards the next level
     * @return The amount of xp required to fully level up to the next level
     */
    public static int getXpRequiredFromLevel(int level, float levelProgress) {
        return Math.round(getXpRequiredFromLevel(level) * levelProgress);
    }

    /**
     * Gets the amount of xp to go from the previous level to the specified level
     *
     * @param level The level to end up at
     * @return The amount of xp required to fully level up to the specified level from the last level
     */
    public static int getXpRequiredToLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("level can not be <= 0");
        }

        return getXpRequiredFromLevel(level - 1);
    }

    /**
     * Gets the amount of xp to go from the previous level to the specified level
     *
     * @param level The level to end up at
     * @param levelProgress A value from 0.0 to 1.0 indicating the progress towards the next level
     * @return The amount of xp required to fully level up to the specified level from the last level
     */
    public static int getXpRequiredToLevel(int level, float levelProgress) {
        return Math.round(getXpRequiredToLevel(level) * levelProgress);
    }

    /**
     * Gets the amount of xp required to remove one level.
     * If the amount of levels to remove is more than one, it will calculate
     * the total amount of experience possible until reaching 0.
     *
     * @param startLevel The current startLevel to start from
     * @param levelProgress A value from 0.0 to 1.0 indicating the progress towards the next startLevel
     * @return The amount of xp required to remove one level, or down to 0 if there is not enough
     */
    public static int getXpToRemoveLevel(int startLevel, float levelProgress) {
        return getXpToRemoveLevel(startLevel, levelProgress, 1);
    }

    /**
     * Gets the amount of xp required to remove the specified amount of levels.
     * If the amount of levels to remove is more than the current amount, it will calculate
     * the total amount of experience possible until reaching 0.
     *
     * @param startLevel The current level to start from
     * @param levelProgress A value from 0.0 to 1.0 indicating the progress towards the next level
     * @param levelsToRemove How many full levels to remove
     * @return The amount of xp required to remove all the specified levels, or down to 0 if there is not enough
     */
    public static int getXpToRemoveLevel(int startLevel, float levelProgress, int levelsToRemove) {
        if (startLevel < 0) {
            System.err.println("Invalid startLevel value: " + startLevel);
            return 0;
        }

        if (levelProgress < 0) {
            System.err.println("Invalid levelProgress value: " + levelProgress);
            return 0;
        }

        if (levelsToRemove <= 0) {
            System.err.println("Invalid levelsToRemove value: " + levelsToRemove);
            return 0;
        }

        int amount = 0;

        for (int i = 0; i < levelsToRemove; i++) {
            int currentLevel = startLevel - i;

            // The part of the current level
            amount += Math.round(getXpRequiredFromLevel(currentLevel) * levelProgress);

            if (currentLevel == 0) {
                return amount;
            }

            // The part of the previous level to match xp level progress
            amount += Math.round(getXpRequiredToLevel(currentLevel) * (1 - levelProgress));
        }

        return amount;
    }

    /**
     * Calculates the amount of xp required to level up from a specific level.
     * Source: http://minecraft.gamepedia.com/Experience
     *
     * @param level The level to calculate for
     * @return The amount of xp required to move to the next level
     */
    private static int calculateXpForLevel(int level) {
        if (level >= 32) {
            return 9 * level - 158;
        } else if (level >= 17) {
            return 5 * level - 38;
        }

        return 2 * level + 7;
    }
}
