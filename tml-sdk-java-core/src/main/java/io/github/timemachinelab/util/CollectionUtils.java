package io.github.timemachinelab.util;

import java.util.*;

public class CollectionUtils {
    static final float DEFAULT_LOAD_FACTOR = 0.75F;

    public CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> List<T> arrayToList(T[] source) {
        return source != null ? Arrays.asList(source) : Collections.emptyList();
    }

    public static <E> void mergeArrayIntoCollection(E[] array, Collection<E> collection) {
        int len = array.length;

        collection.addAll(Arrays.asList(array).subList(0, len));
    }

    public static <E> boolean contains(Iterator<E> iterator, E element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (Objects.equals(candidate, element)) {
                    return true;
                } else if (candidate != null
                        && element != null
                        && candidate.getClass().isArray()
                        && element.getClass().isArray()) {
                    return arrayEquals(candidate, element);
                }
            }
        }

        return false;
    }

    public static <E> boolean arrayEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) ((Object[]) o1), (Object[]) ((Object[]) o2));
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) ((boolean[]) o1), (boolean[]) ((boolean[]) o2));
        } else if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) ((byte[]) o1), (byte[]) ((byte[]) o2));
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) ((char[]) o1), (char[]) ((char[]) o2));
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) ((double[]) o1), (double[]) ((double[]) o2));
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) ((float[]) o1), (float[]) ((float[]) o2));
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) ((int[]) o1), (int[]) ((int[]) o2));
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) ((long[]) o1), (long[]) ((long[]) o2));
        } else {
            return o1 instanceof short[] && o2 instanceof short[]
                    && Arrays.equals((short[]) ((short[]) o1), (short[]) ((short[]) o2));
        }
    }

    public static <E> boolean contains(Collection<E> collection, E element) {
        if (collection != null) {
            Iterator<E> iterator = collection.iterator();

            return contains(iterator, element);
        }

        return false;
    }

    public static boolean containsInstance(Collection<?> collection, Object element) {
        if (collection != null) {

            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }

        return false;
    }
}
