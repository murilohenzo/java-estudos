package exercicio.annotation;
import java.lang.reflect.Method;

public class TableReflections {
    public static void main(String args[]) {
        ClassTable classTable = new ClassTable();

        Method[] methods = classTable.getClass().getMethods();

        for (Method method: methods) {
            Table table = method.getAnnotation(Table.class);
            if (table != null) {
                try {
                    method.invoke(classTable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
