package generics_project.singleton;

import java.util.HashMap;
import java.util.Map;

public class SingletonMap {
    private static SingletonMap singletonMap;

    protected Map<Class, Map<Long, ?>> map;

    public SingletonMap() {
        this.map = new HashMap<>();
    }

    public static SingletonMap getInstance() {
        if (singletonMap == null) {
            singletonMap = new SingletonMap();
        }
        return singletonMap;
    }

    public Map<Class, Map<Long, ?>> getMap() {
        return this.map;
    }
}
