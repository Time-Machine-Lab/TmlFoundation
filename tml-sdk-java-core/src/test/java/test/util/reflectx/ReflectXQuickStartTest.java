package test.util.reflectx;

import io.github.timemachinelab.reflectx.MetaObject;
import io.github.timemachinelab.reflectx.SystemMetaObject;

import java.util.*;

// ==========================================
// 1. 准备测试用的业务对象 (Domain Objects)
// ==========================================
class Order {
    private String orderNo;
    private User user; // 嵌套对象
    
    // Getter/Setter
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

class User {
    private String name;
    private Address address; // 二层嵌套
    private Map<String, Object> attributes = new HashMap<>(); // 混合结构
    private String[] tags; // 数组测试

    // Getter/Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
}

class Address {
    private String city;
    private String street;

    // Getter/Setter
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}

// ==========================================
// 2. 测试主程序
// ==========================================
public class ReflectXQuickStartTest {

    public static void main(String[] args) {
        System.out.println("🚀 ReflectX 全功能验收启动...\n");

        testLevel1_BasicReadWrite();
        testLevel2_DeepPathNavigation();
        testLevel3_MixedCollectionAccess();
        testLevel4_AutoInstantiation(); // 上帝模式

        System.out.println("\n🎉 全部测试通过！ReflectX 核心功能验收完毕。");
    }

    /**
     * 第一关：新手村 (基础读写)
     * 目标：验证普通的属性读写
     */
    private static void testLevel1_BasicReadWrite() {
        System.out.print("[Level 1] 基础读写测试... ");
        
        User user = new User();
        MetaObject meta = SystemMetaObject.forObject(user);

        // 1. 写值
        meta.setValue("name", "ReflectX Master");
        
        // 2. 读值
        String name = (String) meta.getValue("name");

        // 3. 验证
        check("ReflectX Master".equals(name), "读写值不匹配");
        check("ReflectX Master".equals(user.getName()), "原始对象未更新");
        
        System.out.println("✅ PASS");
    }

    /**
     * 第二关：进阶之路 (深层穿透)
     * 目标：验证 a.b.c 这种点号路径
     */
    private static void testLevel2_DeepPathNavigation() {
        System.out.print("[Level 2] 深层路径导航... ");

        // 手动构造一个完整的对象树
        Order order = new Order();
        User user = new User();
        Address address = new Address();
        address.setCity("Shanghai");
        user.setAddress(address);
        order.setUser(user);

        MetaObject meta = SystemMetaObject.forObject(order);

        // 1. 读取深层属性
        String city = (String) meta.getValue("user.address.city");
        check("Shanghai".equals(city), "读取深层属性失败");

        // 2. 修改深层属性
        meta.setValue("user.address.street", "Nanjing Road");
        check("Nanjing Road".equals(order.getUser().getAddress().getStreet()), "写入深层属性失败");

        System.out.println("✅ PASS");
    }

    /**
     * 第三关：高手试炼 (List 与 Map 混合)
     * 目标：验证 a.b[0] 和 map[key] 的混合使用
     * 注意：配合简洁版 PropertyTokenizer 时，连续下标之间需要加点号。
     */
    private static void testLevel3_MixedCollectionAccess() {
        System.out.print("[Level 3] List/Map 混合访问... ");

        User user = new User();
        // 构造复杂结构: attributes(Map) -> scores(List) -> Integer
        List<Integer> scores = new ArrayList<>();
        scores.add(90);
        scores.add(100);
        scores.add(85);
        user.getAttributes().put("scores", scores);

        // 构造数组结构
        user.setTags(new String[]{"Coder", "Architect"});

        MetaObject meta = SystemMetaObject.forObject(user);

        // 1. 访问 Map 中的 List 下标
        // ⚠️ 重点修改：在 [scores] 和 [1] 之间加上点号 "."
        // 你的 PropertyTokenizer 依赖点号来切分层级
        Object score = meta.getValue("attributes[scores].[1]");

        // 验证结果
        if (!Integer.valueOf(100).equals(score)) {
            throw new RuntimeException("Map嵌套List取值错误, 期望 100, 实际 " + score);
        }

        // 2. 访问数组下标 (这里本身就只有一级，无需点号)
        meta.setValue("tags[0]", "Java God");
        if (!"Java God".equals(user.getTags()[0])) {
            throw new RuntimeException("数组写入失败");
        }

        System.out.println("✅ PASS");
    }

    /**
     * 第四关：上帝模式 (自动修路)
     * 目标：从 null 开始自动构建对象树
     */
    private static void testLevel4_AutoInstantiation() {
        System.out.print("[Level 4] 上帝模式(自动修路)... ");

        // 1. 准备一个完全空白的对象 (user 是 null)
        Order emptyOrder = new Order();
        MetaObject meta = SystemMetaObject.forObject(emptyOrder);

        // 2. 直接赋值最深层
        // 预期框架行为：
        // - 发现 user 是 null -> new User() -> set
        // - 发现 address 是 null -> new Address() -> set
        // - 最后 setCity("Beijing")
        try {
            meta.setValue("user.address.city", "Beijing");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("自动创建失败: " + e.getMessage());
        }

        // 3. 验证结构是否被创建
        check(emptyOrder.getUser() != null, "User 对象未创建");
        check(emptyOrder.getUser().getAddress() != null, "Address 对象未创建");
        check("Beijing".equals(emptyOrder.getUser().getAddress().getCity()), "最终值未写入");

        System.out.println("✅ PASS");
    }

    // 简单的断言工具
    private static void check(boolean condition, String msg) {
        if (!condition) {
            System.out.println("❌ FAIL");
            throw new RuntimeException(msg);
        }
    }
}