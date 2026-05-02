package com.selflearn.labs.hashmap;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Supports {@code PREREQUISITES.md} §3 — {@code TREEIFY_THRESHOLD}, {@code MIN_TREEIFY_CAPACITY}, load factor.
 * <p>
 * Reads package-private static fields from {@link HashMap} via reflection (same need for
 * {@code --add-opens java.base/java.util=ALL-UNNAMED} as {@link HashMapInternalsLab}).
 */
public final class HashMapJvmConstantsExample {

    public static void main(String[] args) throws Exception {
        System.out.println("--- HashMap JVM constants (reflection) ---\n");

        printIntField("DEFAULT_INITIAL_CAPACITY");
        printIntField("MAXIMUM_CAPACITY");
        printFloatField("DEFAULT_LOAD_FACTOR");
        printIntField("TREEIFY_THRESHOLD");
        printIntField("UNTREEIFY_THRESHOLD");
        printIntField("MIN_TREEIFY_CAPACITY");

        System.out.println();
        System.out.println("Resize threshold for a new map ≈ capacity × load factor (see HashMap javadoc / source).");
        System.out.println("Treeify: long chain in one bin + table length ≥ MIN_TREEIFY_CAPACITY (see PREREQUISITES.md).");
    }

    private static void printIntField(String name) throws Exception {
        Field f = HashMap.class.getDeclaredField(name);
        f.setAccessible(true);
        Object v = f.get(null);
        System.out.printf("  HashMap.%s = %s%n", name, v);
    }

    private static void printFloatField(String name) throws Exception {
        Field f = HashMap.class.getDeclaredField(name);
        f.setAccessible(true);
        Object v = f.get(null);
        System.out.printf("  HashMap.%s = %s%n", name, v);
    }
}
