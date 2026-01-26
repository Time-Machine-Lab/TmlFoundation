package io.github.timemachinelab.util;

import java.util.*;

/**
 * <p>
 * Operations on collections that complement the standard
 * {@link java.util.Collections} class.
 * </p>
 * 
 * <p>
 * This class provides utility methods for common collection operations
 * including:
 * <ul>
 * <li>Null-safe emptiness checks</li>
 * <li>Array to collection conversions</li>
 * <li>Deep array equality comparisons</li>
 * <li>Element searching with special array handling</li>
 * </ul>
 * </p>
 * 
 * <p>
 * All methods in this class are null-safe and thread-safe for immutable
 * collections.
 * </p>
 * 
 * @since 1.2.0
 * @source org.apache.commons.collections4.CollectionUtils (inspired by)
 */
public class CollectionUtils {

    /**
     * <p>
     * Checks if a Collection is empty or null.
     * </p>
     * 
     * <pre>
     * CollectionUtils.isEmpty(null)   = true
     * CollectionUtils.isEmpty([])     = true
     * CollectionUtils.isEmpty(["a"])  = false
     * </pre>
     * 
     * @param collection the collection to check, may be null
     * @return {@code true} if the collection is empty or null
     * @since 1.2.0
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * <p>
     * Checks if a Map is empty or null.
     * </p>
     * 
     * <pre>
     * CollectionUtils.isEmpty(null)   = true
     * CollectionUtils.isEmpty({})     = true
     * CollectionUtils.isEmpty({"a":"b"}) = false
     * </pre>
     * 
     * @param map the map to check, may be null
     * @return {@code true} if the map is empty or null
     * @since 1.2.0
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * <p>
     * Converts an array to a List.
     * </p>
     * 
     * <pre>
     * CollectionUtils.arrayToList(null)    = []
     * CollectionUtils.arrayToList(["a"])  = ["a"]
     * CollectionUtils.arrayToList(["a","b"]) = ["a", "b"]
     * </pre>
     * 
     * @param <T>    the element type
     * @param source the array to convert, may be null
     * @return a list containing the array elements, or empty list if null array
     * @since 1.2.0
     */
    public static <T> List<T> arrayToList(T[] source) {
        return source != null ? Arrays.asList(source) : Collections.emptyList();
    }

    /**
     * <p>
     * Merges the elements of an array into the given Collection.
     * </p>
     * 
     * <pre>
     * CollectionUtils.mergeArrayIntoCollection(null, collection)      = no change
     * CollectionUtils.mergeArrayIntoCollection([], collection)        = no change  
     * CollectionUtils.mergeArrayIntoCollection(["a"], collection)  = collection.add("a")
     * CollectionUtils.mergeArrayIntoCollection(["a","b"], list)    = list.addAll(["a", "b"])
     * </pre>
     * 
     * @param <E>        the element type
     * @param array      the array to merge, may be null
     * @param collection the collection to merge into, may be null
     * @since 1.2.0
     */
    public static <E> void mergeArrayIntoCollection(E[] array, Collection<E> collection) {
        if (array != null && array.length > 0 && collection != null) {
            collection.addAll(Arrays.asList(array));
        }
    }

    /**
     * <p>
     * Deep equality check for arrays of any type.
     * </p>
     * 
     * <p>
     * This method handles arrays of primitive types and object arrays,
     * performing element-by-element comparison using {@link Arrays#equals}.
     * </p>
     * 
     * <pre>
     * CollectionUtils.arrayEquals(null, null) = true
     * CollectionUtils.arrayEquals([1,2], [1,2]) = true  
     * CollectionUtils.arrayEquals([1,2], [1,3]) = false
     * CollectionUtils.arrayEquals("a", "a") = false // not arrays
     * </pre>
     * 
     * @param o1 the first object to compare, may be null
     * @param o2 the second object to compare, may be null
     * @return {@code true} if both objects are arrays with equal elements
     * @since 1.2.0
     */
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

    /**
     * <p>
     * Checks if an Iterator contains the given element, with special handling for
     * arrays.
     * </p>
     * 
     * <p>
     * This method performs a deep equality check for array elements using
     * {@link #arrayEquals}.
     * </p>
     * 
     * <pre>
     * CollectionUtils.contains(null, "a") = false
     * CollectionUtils.contains(["a","b"].iterator(), "a") = true
     * CollectionUtils.contains(["a","b"].iterator(), "c") = false
     * CollectionUtils.contains([[1,2]].iterator(), [1,2]) = true // array deep equality
     * </pre>
     * 
     * @param <E>      the element type
     * @param iterator the iterator to search, may be null
     * @param element  the element to find, may be null
     * @return {@code true} if the iterator contains the element
     * @since 1.2.0
     */
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

    /**
     * <p>
     * Checks if a Collection contains the given element, with special handling for
     * arrays.
     * </p>
     * 
     * <p>
     * This method delegates to {@link #contains(Iterator, Object)} for the actual
     * search logic,
     * including deep equality checking for array elements.
     * </p>
     * 
     * <pre>
     * CollectionUtils.contains(null, "a") = false
     * CollectionUtils.contains(["a","b"], "a") = true
     * CollectionUtils.contains(["a","b"], "c") = false
     * CollectionUtils.contains([[1,2]], [1,2]) = true // array deep equality
     * </pre>
     * 
     * @param <E>        the element type
     * @param collection the collection to search, may be null
     * @param element    the element to find, may be null
     * @return {@code true} if the collection contains the element
     * @since 1.2.0
     */
    public static <E> boolean contains(Collection<E> collection, E element) {
        if (collection != null) {
            Iterator<E> iterator = collection.iterator();

            return contains(iterator, element);
        }

        return false;
    }

    /**
     * <p>
     * Checks if a Collection contains the given element using instance equality
     * (==).
     * </p>
     * 
     * <p>
     * Unlike {@link #contains(Collection, Object)}, this method uses reference
     * equality
     * instead of {@link Object#equals(Object)} for comparison.
     * </p>
     * 
     * <pre>
     * String a = "test";
     * CollectionUtils.containsInstance(["test"], a) = false // different instance
     * CollectionUtils.containsInstance([a], a) = true // same instance
     * CollectionUtils.containsInstance(null, "a") = false
     * </pre>
     * 
     * @param collection the collection to search, may be null
     * @param element    the element to find, may be null
     * @return {@code true} if the collection contains the exact same instance
     * @since 1.2.0
     */
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
