package io.github.timemachinelab.util;

import java.lang.reflect.Array;

public class ArrayUtils {

    public static int getLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    public static boolean isEmpty(final Object[] array) {
        return getLength(array) == 0;
    }
}
