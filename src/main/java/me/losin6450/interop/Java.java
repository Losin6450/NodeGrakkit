package me.losin6450.interop;

public class Java {

    public static Class<?> type(String name) throws ClassNotFoundException {
        return type(name, ClassLoader.getSystemClassLoader());
    }

    public static Class<?> type(String name, ClassLoader loader) throws ClassNotFoundException {
        return loader.loadClass(name);
    }
}
