package io.pcp.parfait;

import com.google.common.collect.ImmutableSortedSet;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;

public class HistogramTest {

    private static final String DESCRIPTION = "My histogram";
    private static final String NAME = "my.histogram";
    private MonitorableRegistry monitorableRegistry;
    private Histogram<Duration> histogram;

    @Before
    public void setUp() throws Exception {
        monitorableRegistry = new TestMonitorableRegistry();
        histogram = new Histogram<>(
                NAME,
                DESCRIPTION,
                ImmutableSortedSet.of(Duration.ofSeconds(10), Duration.ofSeconds(20), Duration.ofSeconds(30)),
                monitorableRegistry,
                duration -> Long.valueOf(duration.getSeconds()).toString()
        );
    }

    @Test
    public void registerOccurrence_shouldIncrementTheCounterForTheCorrectBin() {

        histogram.registerOccurrence(Duration.ofSeconds(15));

        Object o = monitorableRegistry.getMetric("my.histogram[10 - <20]").get();
        assertThat(o, Matchers.is(1));

    }

}