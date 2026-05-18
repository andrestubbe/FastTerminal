package fastterminal;

import java.lang.reflect.Method;
import java.net.URL;

public class TestLoad {
    public static void main(String[] args) {
        System.out.println("=== DECLARED METHODS OF FastMouseImpl ===");
        try {
            Class<?> clazz = Class.forName("fastmouse.FastMouseImpl");
            for (Method m : clazz.getDeclaredMethods()) {
                System.out.print("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(");
                Class<?>[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    System.out.print(params[i].getSimpleName());
                    if (i < params.length - 1) System.out.print(", ");
                }
                System.out.println(")");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("=========================================");
    }
}
