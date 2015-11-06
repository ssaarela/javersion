package org.javersion.object;

import static org.assertj.core.api.Assertions.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

public class JodaTest {

    @Versionable
    public static class DT {
        final DateTime dateTime = new DateTime();
        final LocalDate localDate = new LocalDate();
    }

    private ObjectSerializer<DT> serializer = new ObjectSerializer<>(DT.class);

    @Test
    public void write_read() {
        final DT dt = new DT();
        DT dt2 = serializer.fromPropertyMap(serializer.toPropertyMap(dt));
        assertThat(dt2.dateTime).isEqualTo(dt.dateTime);
        assertThat(dt2.localDate).isEqualTo(dt.localDate);
    }

}
