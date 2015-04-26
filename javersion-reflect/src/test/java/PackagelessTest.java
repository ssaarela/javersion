import static org.assertj.core.api.Assertions.assertThat;

import org.javersion.reflect.TypeDescriptor;
import org.javersion.reflect.TypeDescriptors;
import org.junit.Test;

public class PackagelessTest {

    @Test
    public void simple_name() {
        TypeDescriptor type = TypeDescriptors.DEFAULT.get(PackagelessTest.class);
        assertThat(type.getSimpleName()).isEqualTo("PackagelessTest");
    }

}
