package org.javersion.reflect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;

public abstract class AbstractTypeDescriptors<F extends AbstractFieldDescriptor<F, T>, T extends AbstractTypeDescriptor<F, T>> {

    public static final Predicate<Field> NON_STATIC_OR_SYNTETHIC_FIELD = new Predicate<Field>() {
        @Override
        public boolean apply(Field field) {
            int mod = field.getModifiers();
            return !(Modifier.isStatic(mod) || field.isSynthetic());
        }
    };
    
    
    private final ConcurrentMap<TypeToken<?>, T> cache = new ConcurrentHashMap<>();

    protected final Predicate<? super Field> fieldFilter;
    
    
    public AbstractTypeDescriptors() {
        this(NON_STATIC_OR_SYNTETHIC_FIELD);
    }
    
    public AbstractTypeDescriptors(Predicate<? super Field> fieldFilter) {
        this.fieldFilter = checkNotNull(fieldFilter);
    }
    
    
    public T get(Class<?> clazz) {
        return get(TypeToken.of(clazz));
    }

    public T get(Type type) {
        return get(TypeToken.of(type));
    }
    
    public T get(TypeToken<?> typeToken) {
        T descriptor = cache.get(typeToken);
        if (descriptor == null) {
            descriptor = newTypeDescriptor(typeToken);
            cache.putIfAbsent(typeToken, descriptor);
        }
        return descriptor;
    }

    protected abstract F newFieldDescriptor(Field field);
    
    protected abstract T newTypeDescriptor(TypeToken<?> typeToken);

}
