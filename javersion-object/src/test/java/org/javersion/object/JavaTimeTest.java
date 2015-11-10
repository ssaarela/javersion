package org.javersion.object;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;

public class JavaTimeTest {

    @Versionable
    public static class DT {
        private final Instant instant = Instant.now();
        private final LocalDate localDate = LocalDate.now();
        private final LocalDateTime localDateTime = LocalDateTime.now();
    }

    private ObjectSerializer<DT> serializer = new ObjectSerializer<>(DT.class);

    @Test
    public void write_read() {
        final DT dt = new DT();
        DT dtCopy = serializer.fromPropertyMap(serializer.toPropertyMap(dt));
        assertThat(dtCopy.instant).isEqualTo(dt.instant);
        assertThat(dtCopy.localDate).isEqualTo(dt.localDate);
        assertThat(dtCopy.localDateTime).isEqualTo(dt.localDateTime);
    }
}
