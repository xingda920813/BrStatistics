package me.xd;

import kotlin.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public interface IKnowMyType2<K, V> {

    default Pair<Class<K>, Class<V>> getGenericTypes() {
        for (final Type interfaceType : getClass().getGenericInterfaces()) {
            if (!(interfaceType instanceof ParameterizedType)) continue;
            final ParameterizedType parameterizedInterface = (ParameterizedType) interfaceType;
            if (parameterizedInterface.getRawType() != IKnowMyType2.class) continue;
            final Type[] actualTypeArguments = parameterizedInterface.getActualTypeArguments();
            return new Pair<>(getClass(actualTypeArguments[0]), getClass(actualTypeArguments[1]));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T> getClass(Type type) {
        if (type instanceof Class<?>) return (Class<T>) type;
        if (type instanceof ParameterizedType) return (Class<T>) ((ParameterizedType) type).getRawType();
        if (type instanceof WildcardType) return getClass(((WildcardType) type).getUpperBounds()[0]);
        if (type instanceof TypeVariable<?>) return getClass(((TypeVariable<?>) type).getBounds()[0]);
        if (type instanceof GenericArrayType) return (Class<T>) getArrayClass(getClass(((GenericArrayType) type).getGenericComponentType()));
        return null;
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T[]> getArrayClass(Class<T> componentType) {
        return (Class<T[]>) Array.newInstance(componentType, 0).getClass();
    }

    static void main(String... args) {
        class Clazz extends HashMap<Integer, String> implements IKnowMyType2<Integer, String> {}
        class Parameterized extends HashMap<List<Integer>, Set<String>> implements IKnowMyType2<List<Integer>, Set<String>> {}
        class Wildcard extends HashMap<List<? extends Integer>, Set<? extends String>>
                implements IKnowMyType2<List<? extends Integer>, Set<? extends String>> {}
        class Variable<K extends Integer & Serializable, V extends String & Serializable> extends HashMap<K, V> implements IKnowMyType2<K, V> {}
        class ParameterizedArray extends HashMap<List<Integer>[][], Set<String>[][]> implements IKnowMyType2<List<Integer>[][], Set<String>[][]> {}
        class VariableArray<K extends Integer & Serializable, V extends String & Serializable>
                extends HashMap<K[][], V[][]> implements IKnowMyType2<K[][], V[][]> {}
        class OneTypeParameter extends ArrayList<Integer> implements IKnowMyType2<Integer, Void> {}
        System.out.println(new Clazz().getGenericTypes());
        System.out.println(new Parameterized().getGenericTypes());
        System.out.println(new Wildcard().getGenericTypes());
        System.out.println(new Variable<Integer, String>().getGenericTypes());
        System.out.println(new ParameterizedArray().getGenericTypes());
        System.out.println(new VariableArray<Integer, String>().getGenericTypes());
        System.out.println(new OneTypeParameter().getGenericTypes().getFirst());
    }
}
