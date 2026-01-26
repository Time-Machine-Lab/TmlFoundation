package test.util.reflectx;


import io.github.timemachinelab.reflectx.parser.PropertyTokenizer;

public class ParserTest {

    public static void main(String[] args) {
        System.out.println("========== 第二章：分词器验收 ==========");

        // 模拟一个极其复杂的路径：
        // 1. 访问 classes 集合的第 0 个元素
        // 2. 访问 students 集合(Map) key为 'leader' 的元素
        // 3. 访问 score 属性
        String complexPath = "classes[0].students[leader].score";
        
        System.out.println("解析路径: " + complexPath);
        System.out.println("----------------------------------------");

        // 使用 foreach 循环优雅遍历 (得益于 Iterable 接口实现)
        int depth = 0;
        for (PropertyTokenizer prop : new PropertyTokenizer(complexPath)) {
            depth++;
            System.out.printf("Level %d:%n", depth);
            System.out.printf("  Raw Name    : %s%n", prop.getIndexedName());
            System.out.printf("  Clean Name  : %s%n", prop.getName());
            System.out.printf("  Index       : %s%n", prop.getIndex());
            System.out.printf("  Has Children: %b%n", prop.hasNext());
            System.out.println("      ↓");
        }
        System.out.println("End of Path");
        System.out.println("========================================");
    }
}