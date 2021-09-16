package me.xd;

import kotlin.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public interface IKnowMyType2<K, V> {

    final class ClassNode<T> {

        public final Class<T> mRoot;
        public final ArrayList<ClassNode<?>> mChildren = new ArrayList<>();

        ClassNode(Class<T> root) {
            mRoot = root;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClassNode)) return false;
            final ClassNode<?> other = (ClassNode<?>) o;
            return mRoot.equals(other.mRoot) && mChildren.equals(other.mChildren);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mRoot, mChildren);
        }

        @Override
        public String toString() {
            return "ClassNode{mRoot=" + mRoot + ", mChildren=" + mChildren + '}';
        }
    }

    default Pair<ClassNode<K>, ClassNode<V>> getGenericTypes() {
        for (final Type interfaceType : getClass().getGenericInterfaces()) {
            if (!(interfaceType instanceof ParameterizedType)) continue;
            final ParameterizedType parameterizedInterface = (ParameterizedType) interfaceType;
            if (parameterizedInterface.getRawType() != IKnowMyType2.class) continue;
            final Type[] actualTypeArguments = parameterizedInterface.getActualTypeArguments();
            return new Pair<>(getClassTree(actualTypeArguments[0]), getClassTree(actualTypeArguments[1]));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <T> ClassNode<T> getClassTree(Type type) {
        if (type instanceof Class<?>) return new ClassNode<>((Class<T>) type);
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final ClassNode<T> tree = getClassTree(parameterizedType.getRawType());
            Arrays.stream(parameterizedType.getActualTypeArguments())
                  .map(IKnowMyType2::getClassTree)
                  .forEach(tree.mChildren::add);
            return tree;
        }
        if (type instanceof WildcardType) return getClassTree(((WildcardType) type).getUpperBounds()[0]);
        if (type instanceof TypeVariable<?>) return getClassTree(((TypeVariable<?>) type).getBounds()[0]);
        if (type instanceof GenericArrayType) {
            final ClassNode<?> componentTree = getClassTree(((GenericArrayType) type).getGenericComponentType());
            final ClassNode<T> tree = new ClassNode<>((Class<T>) getArrayType(componentTree.mRoot));
            tree.mChildren.add(componentTree);
            return tree;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T[]> getArrayType(Class<T> componentType) {
        return (Class<T[]>) Array.newInstance(componentType, 0).getClass();
    }

    static void main(String... args) {
        class Clazz extends HashMap<Integer, String> implements IKnowMyType2<Integer, String> {}
        class Parameterized extends HashMap<List<Integer>, Set<String>>
                implements IKnowMyType2<List<Integer>, Set<String>> {}
        class Wildcard extends HashMap<List<? extends Integer>, Set<? extends String>>
                implements IKnowMyType2<List<? extends Integer>, Set<? extends String>> {}
        class Variable<K extends Integer & Serializable, V extends String & Serializable>
                extends HashMap<K, V> implements IKnowMyType2<K, V> {}
        class ParameterizedArray extends HashMap<List<Integer>[][], Set<String>[][]>
                implements IKnowMyType2<List<Integer>[][], Set<String>[][]> {}
        class VariableArray<K extends Integer & Serializable, V extends String & Serializable>
                extends HashMap<K[][], V[][]> implements IKnowMyType2<K[][], V[][]> {}
        class OneTypeParameter extends ArrayList<Integer> implements IKnowMyType2<Integer, Void> {}
        class Nested<T extends List<Integer> & IKnowMyType2<Integer, Void>>
                extends ArrayList<T> implements IKnowMyType2<T, Void> {}
        class MoreNested<T extends List<List<Integer>>> extends ArrayList<T> implements IKnowMyType2<T, Void> {}
        System.out.println(new Clazz().getGenericTypes());
        System.out.println(new Parameterized().getGenericTypes());
        System.out.println(new Wildcard().getGenericTypes());
        System.out.println(new Variable<Integer, String>().getGenericTypes());
        System.out.println(new ParameterizedArray().getGenericTypes());
        System.out.println(new VariableArray<Integer, String>().getGenericTypes());
        System.out.println(new OneTypeParameter().getGenericTypes().getFirst());
        System.out.println(new Nested<OneTypeParameter>().getGenericTypes().getFirst());
        System.out.println(new MoreNested<>().getGenericTypes().getFirst());
    }
}
