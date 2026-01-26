package io.github.timemachinelab.reflectx.parser;

import java.util.Iterator;

/**
 * 描述: 属性分词器
 * <p>
 * 职责：将复杂的属性路径（如 "orders[0].items[2].name"）解析为链式结构。
 * 特点：
 * 1. <b>高性能</b>：纯字符串位移操作，零正则，零多余对象创建。
 * 2. <b>不可变</b>：对象创建后状态锁定，线程安全。
 * 3. <b>易遍历</b>：支持 foreach 循环遍历完整属性链。
 * </p>
 * @author suifeng
 * 日期: 2026/1/26
 */
public class PropertyTokenizer implements Iterable<PropertyTokenizer> {

    /** 当前节点的完整名称 (如: "orders[0]") */
    private final String indexedName;

    /** 纯净的属性名，去除下标 (如: "orders") -> 用于查找 JavaBean 属性 */
    private final String name;

    /** 提取出的下标/Key (如: "0" 或 "key")，非集合属性则为 null -> 用于 List/Map 取值 */
    private final String index;

    /** 剩余的未解析子路径 (如: "items[2].name")，无子路径则为 null */
    private final String children;

    public PropertyTokenizer(String fullname) {
        // 1. 寻找分隔符 "." (区分当前节点与子节点)
        int delim = fullname.indexOf('.');
        String tempName;

        if (delim > -1) {
            // Case: "orders[0].items..." -> 截取当前段 "orders[0]"
            tempName = fullname.substring(0, delim);
            this.children = fullname.substring(delim + 1);
        } else {
            // Case: "orders[0]" (到达末尾)
            tempName = fullname;
            this.children = null;
        }

        this.indexedName = tempName;

        // 2. 解析下标逻辑 (检查是否存在 "[")
        int indexStart = tempName.indexOf('[');
        if (indexStart > -1) {
            // 提取中括号内的内容：orders[0] -> 0
            this.index = tempName.substring(indexStart + 1, tempName.length() - 1);
            // 截取纯属性名：orders[0] -> orders
            this.name = tempName.substring(0, indexStart);
        } else {
            this.index = null;
            this.name = tempName;
        }
    }

    public String getName() { return name; }
    public String getIndexedName() { return indexedName; }
    public String getIndex() { return index; }
    public String getChildren() { return children; }

    /**
     * 是否有子节点
     */
    public boolean hasNext() {
        return children != null;
    }

    /**
     * 解析下一层级
     */
    public PropertyTokenizer next() {
        if (children == null) {
            return null;
        }
        return new PropertyTokenizer(children);
    }

    /**
     * 实现 Iterable 接口，支持 foreach 循环
     */
    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return new Iterator<PropertyTokenizer>() {
            private PropertyTokenizer current = PropertyTokenizer.this;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public PropertyTokenizer next() {
                PropertyTokenizer result = current;
                // 指针下移：如果有儿子，就解析儿子；否则置空结束迭代
                current = result.hasNext() ? result.next() : null;
                return result;
            }
        };
    }

    @Override
    public String toString() {
        return String.format("Token{name='%s', index='%s', children='%s'}", name, index, children);
    }
}
