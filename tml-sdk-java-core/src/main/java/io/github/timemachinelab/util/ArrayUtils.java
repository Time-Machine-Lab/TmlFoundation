package io.github.timemachinelab.util;

import java.lang.reflect.Array;

/**
 * Array utility.
 *
 * @author Genius
 */
@Beta
public final class ArrayUtils {

    private ArrayUtils() {

    }

    public static int getLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    public static boolean isEmpty(final Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean notEmpty(final Object[] array) {
        return !isEmpty(array);
    }
}
