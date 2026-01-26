package test.util.reflectx;

import io.github.timemachinelab.reflectx.Reflector;
import io.github.timemachinelab.reflectx.invoker.Invoker;
import io.github.timemachinelab.reflectx.invoker.SetFieldInvoker;

// 模拟业务对象
class User {
    private long id;
    private String name;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; } // No setName()
}

public class ReflectXCoreTest {
    
    public static void main(String[] args) throws Exception {
        System.out.println("========== 第一章：内核地基验收 ==========");
        
        // 1. 初始化 (第一次访问，会触发 Reflector 解析并缓存)
        Reflector reflector = Reflector.forClass(User.class);
        User user = new User();

        // 2. 测试标准 Setter (应该走 MethodInvoker)
        System.out.print("[Test 1] Standard Setter: ");
        Invoker setId = reflector.getSetInvoker("id");
        setId.invoke(user, new Object[]{10086L});
        System.out.println(user.getId() == 10086L ? "PASS" : "FAIL");

        // 3. 测试字段兜底写入 (User 没有 setName 方法，框架应自动降级为 SetFieldInvoker)
        System.out.print("[Test 2] Field Fallback Write: ");
        try {
            Invoker setName = reflector.getSetInvoker("name");
            // 验证类型是否为 SetFieldInvoker
            if (!(setName instanceof SetFieldInvoker)) {
                 System.out.println("FAIL (Wrong Invoker Type)");
            } else {
                setName.invoke(user, new Object[]{"Architect"});
                System.out.println("Architect".equals(user.getName()) ? "PASS" : "FAIL");
            }
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
        }
        
        // 4. 验证缓存 (第二次获取应当非常快且为同一实例)
        Reflector reflector2 = Reflector.forClass(User.class);
        System.out.println("[Test 3] Cache Hit: " + (reflector == reflector2 ? "PASS" : "FAIL"));

        System.out.println("==========================================");
    }
}