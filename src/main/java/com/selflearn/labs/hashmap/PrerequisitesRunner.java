package com.selflearn.labs.hashmap;

/**
 * Runs all prerequisite examples in order. Gradle: {@code ./gradlew runPrerequisitesExamples}
 * <p>
 * VM options (IntelliJ): {@code --add-opens java.base/java.util=ALL-UNNAMED}
 * (required for {@link HashMapJvmConstantsExample}; harmless for the others).
 */
public final class PrerequisitesRunner {

    public static void main(String[] args) throws Exception {
        System.out.println("Prerequisites examples (see PREREQUISITES.md)\n");
        HashingCollisionsExample.main(new String[0]);
        System.out.println();
        LookupCostComparisonExample.main(new String[0]);
        System.out.println();
        PrivateFieldReflectionExample.main(new String[0]);
        System.out.println();
        HashMapJvmConstantsExample.main(new String[0]);
        System.out.println();
        System.out.println("Next: HashMapInternalsLab + ./gradlew runHashMapLab");
    }
}
