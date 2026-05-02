package com.selflearn.labs.hashmap;

import java.lang.reflect.Field;

/**
 * Supports {@code PREREQUISITES.md} §4 — reflection basics without touching {@link java.util.HashMap} yet.
 * <p>
 * Reading a private field on <em>your own</em> class does not require {@code --add-opens}.
 * Run: {@code ./gradlew runPrerequisitesExamples} or this {@code main} from IntelliJ.
 */
public final class PrivateFieldReflectionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Reflection on a private field (your own class) ---\n");

        var box = new SecretBox("bucket-index-7");

        Field f = SecretBox.class.getDeclaredField("label");
        f.setAccessible(true);
        Object read = f.get(box);

        System.out.println("  SecretBox.toString(): " + box);
        System.out.println("  Reflected private field 'label': " + read);
        System.out.println();
        System.out.println("Same pattern is used in HashMapInternalsLab on HashMap's private 'table' field,");
        System.out.println("which needs --add-opens java.base/java.util=ALL-UNNAMED for JDK module access.");
    }

    static final class SecretBox {
        @SuppressWarnings("unused")
        private final String label;

        SecretBox(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "SecretBox(???)";
        }
    }
}
