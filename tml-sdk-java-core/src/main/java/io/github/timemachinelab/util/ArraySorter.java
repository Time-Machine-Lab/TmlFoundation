package io.github.timemachinelab.util;

import java.util.Arrays;
import java.util.Comparator;

public class ArraySorter {

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(byte[])
     */
    public static byte[] sort(final byte[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(char[])
     */
    public static char[] sort(final char[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(double[])
     */
    public static double[] sort(final double[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(float[])
     */
    public static float[] sort(final float[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(int[])
     */
    public static int[] sort(final int[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(long[])
     */
    public static long[] sort(final long[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(short[])
     */
    public static short[] sort(final short[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param <T> the array type.
     * @param array the array to sort (may be null).
     * @return the given array.
     * @see Arrays#sort(Object[])
     */
    public static <T> T[] sort(final T[] array) {
        if (array != null) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Sorts the given array into ascending order and returns it.
     *
     * @param <T> the array type.
     * @param array the array to sort (may be null).
     * @param comparator the comparator to determine the order of the array. A {@code null} value uses the elements'
     *        {@link Comparable natural ordering}.
     * @return the given array.
     * @see Arrays#sort(Object[])
     */
    public static <T> T[] sort(final T[] array, final Comparator<? super T> comparator) {
        if (array != null) {
            Arrays.sort(array, comparator);
        }
        return array;
    }
}
