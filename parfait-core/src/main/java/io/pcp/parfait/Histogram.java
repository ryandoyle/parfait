package io.pcp.parfait;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.pcp.parfait.Histogram.WithNextIterator.withCurrentAndNext;

public class Histogram<T extends Comparable<T>> {

    private final String name;
    private final String description;
    private final SortedSet<T> bins;
    private MonitorableRegistry monitorableRegistry;
    private final Map<T, Counter> binCounters = new ConcurrentHashMap<>();
    private final Counter lessThanBucket;
    private final Counter greaterThanBucket;

    public Histogram(String name, String description, SortedSet<T> bins, MonitorableRegistry monitorableRegistry, Function<T, String> bucketingName) {
        this.name = name;
        this.description = description;
        this.bins = bins;
        this.lessThanBucket = new MonitoredCounter(name + "[< " + bucketingName.apply(bins.first()) + "].count", description, monitorableRegistry);
        this.greaterThanBucket = new MonitoredCounter(name + "[" + bucketingName.apply(bins.last()) + " >].count", description, monitorableRegistry);
        withCurrentAndNext(bins.iterator(), (bin, nextBin) ->
                nextBin.ifPresent(b ->
                        binCounters.put(bin, new MonitoredCounter(name + "[" + bucketingName.apply(bin) + " - <" + bucketingName.apply(b) + "]", description, monitorableRegistry))
                )
        );
    }


    public void registerOccurrence(T value) {
        value.compareTo(bins.first());

        withCurrentAndNext(bins, (bin, nextBin) -> {
                if (value.compareTo(bin))
        });
        Collections.binarySearch(binCounters.keySet())

    }

    static final class WithNextIterator {

        private WithNextIterator() {
        }

        public static <T> void withCurrentAndNext(Iterator<T> iterator, BiConsumer<T, Optional<T>> currentAndNext) {
            if (!iterator.hasNext()) {
                return;
            }
            withCurrentAndNext(iterator, iterator.next(), currentAndNext);
        }

        private static <T> void withCurrentAndNext(Iterator<T> iterator, T current, BiConsumer<T, Optional<T>> currentAndNext) {
            if (!iterator.hasNext()) {
                currentAndNext.accept(current, Optional.empty());
                return;
            }

            T next = iterator.next();
            currentAndNext.accept(current, Optional.of(next));

            withCurrentAndNext(iterator, next, currentAndNext);
        }

    }


}
