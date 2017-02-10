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
     * Calculates the level associated with the specified amount of total xp
     *
     * @param xp The amount of xp
     * @return The level associated with the specified amount of xp
     */
    public static int calculateLevelFromXp(int xp) {
        if (xp > 1395) {
            return (int)((Math.sqrt(72 * xp - 54215) + 325) / 18);
        }

        if (xp > 315) {
            return (int)(Math.sqrt(40 * xp - 7839) / 10 + 8.1);
        }

        return (int)(Math.sqrt(xp + 9) - 3);
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
        }

        if (level >= 17) {
            return 5 * level - 38;
        }

        return 2 * level + 7;
    }
}
