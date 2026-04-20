package io.github.timemachinelab.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TreeUtil {

    /**
     * 构建树
     *
     * @param menuList       需要合成树的List
     * @param pIdGetter      对象中获取父级ID方法,如:Menu:getParentId
     * @param idGetter       对象中获取主键ID方法 ,如：Menu:getId
     * @param rootCheck      判断对象是否根节点的方法，如： x->x.getParentId()==null,x->x.getParentMenuId()==0
     * @param setSubChildren 对象中设置下级数据列表方法，如： Menu::setSubMenus
     */
    public static <T, E> List<E> makeTree(List<E> menuList, Function<E, T> pIdGetter, Function<E, T> idGetter, Predicate<E> rootCheck, BiConsumer<E, List<E>> setSubChildren) {
        Map<T, List<E>> parentMenuMap = new HashMap<>();
        for (E node : menuList) {
            //获取父节点id
            T parentId = pIdGetter.apply(node);
            //节点放入父节点的value内
            parentMenuMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }
        List<E> result = new ArrayList<>();
        for (E node : menuList) {
            //添加到下级数据中
            setSubChildren.accept(node, parentMenuMap.getOrDefault(idGetter.apply(node), new ArrayList<>()));
            //如里是根节点，加入结构
            if (rootCheck.test(node)) {
                result.add(node);
            }
        }
        return result;
    }


    /**
     * 树中查找（当前节点不匹配predicate，只要其子节点列表匹配到即保留）
     * @param tree  需要查找的树
     * @param predicate  过滤条件，根据业务场景自行修改
     * @param getSubChildren 获取下级数据方法，如：MenuVo::getSubMenus
     * @return List<E> 过滤后的树
     * @param <E> 泛型实体对象
     */
    public static <E> List<E> search(List<E> tree, Predicate<E> predicate, Function<E, List<E>> getSubChildren) {
        List<E> result = new ArrayList<>();

        for (E item : tree) {
            List<E> childList = getSubChildren.apply(item);
            List<E> filteredChildren = new ArrayList<>();

            if (childList != null && !childList.isEmpty()) {
                filteredChildren = search(childList, predicate, getSubChildren);
            }
            // 如果当前节点匹配，或者至少有一个子节点保留，就保留当前节点
            if (predicate.test(item) || !filteredChildren.isEmpty()) {
                result.add(item);
                // 还原下一级子节点如果有
                if (!filteredChildren.isEmpty()) {
                    getSubChildren.apply(item).clear();
                    getSubChildren.apply(item).addAll(filteredChildren);
                }
            }
        }
        return result;
    }


    /**
     * 树中过滤
     * @param tree  需要进行过滤的树
     * @param predicate  过滤条件判断
     * @param getChildren 获取下级数据方法，如：Menu::getSubMenus
     * @return List<E> 过滤后的树
     * @param <E> 泛型实体对象
     */
    public static <E> List<E> filter(List<E> tree, Predicate<E> predicate, Function<E, List<E>> getChildren) {
        return tree.stream().filter(item -> {
            if (predicate.test(item)) {
                List<E> children = getChildren.apply(item);
                if (children != null && !children.isEmpty()) {
                    filter(children, predicate, getChildren);
                }
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }



    /**
     * 对树形结构进行排序
     *
     * @param tree         要排序的树形结构，表示为节点列表。
     * @param comparator   用于节点比较的比较器。
     * @param getChildren  提供一种方法来获取每个节点的子节点列表。
     * @param <E>          元素的类型。
     * @return 排序后的节点列表。
     */
    public static <E> List<E> sort(List<E> tree, Comparator<? super E> comparator, Function<E, List<E>> getChildren) {
        // 对树的每个节点进行迭代处理
        for (E item : tree) {
            // 获取当前节点的子节点列表
            List<E> childList = getChildren.apply(item);
            // 如果子节点列表不为空，则递归调用 sort 方法对其进行排序
            if (childList != null && !childList.isEmpty()) {
                sort(childList, comparator, getChildren);
            }
        }
        // 对当前层级的节点列表进行排序
        // 这一步确保了所有直接子节点在递归返回后都已排序，然后对当前列表进行排序
        tree.sort(comparator);
        // 返回排序后的节点列表
        return tree;
    }
}
