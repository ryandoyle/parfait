package io.pcp.parfait;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestMonitorableRegistry extends MonitorableRegistry {

    private final Map<String, Monitorable<?>> monitorables = new HashMap<>();

    @Override
    public synchronized <T> void register(Monitorable<T> monitorable) {
        monitorables.put(monitorable.getName(), monitorable);
    }

    @Override
    public synchronized <T> T registerOrReuse(Monitorable<T> monitorable) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public synchronized Collection<Monitorable<?>> getMonitorables() {
        return monitorables.values();
    }

    @Override
    public void addRegistryListener(MonitorableRegistryListener monitorableRegistryListener) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void removeRegistryListener(MonitorableRegistryListener listener) {
        throw new RuntimeException("not implemented");
    }

    @Override
    boolean containsMetric(String name) {
        return monitorables.containsKey(name);
    }

    @Override
    Monitorable<?> getMetric(String name) {
        return monitorables.get(name);
    }
}
