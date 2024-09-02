package com.example.examplemod.filter;

public class FilterManager {
    private static final ThreadLocal<Integer> filterMode = ThreadLocal.withInitial(() -> 0); // Default filter mode is 0 (all items)

    public static int getFilterMode() {
        return filterMode.get();
    }

    public static void setFilterMode(int mode) {
        filterMode.set(mode);
    }
}