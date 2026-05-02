package com.selflearn.labs.hashmap;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Supports {@code PREREQUISITES.md} §2 — why a long linked chain is O(k) but a balanced tree is O(log k).
 * <p>
 * This is not {@link java.util.HashMap}'s internal code; it isolates the <em>lookup cost inside one pile of keys</em>.
 * Run: {@code ./gradlew runPrerequisitesExamples} or this {@code main} from IntelliJ.
 */
public final class LookupCostComparisonExample {

    private static final int SIZE = 50_000;
    private static final int WARMUP = 2_000;
    private static final int REPS = 5_000;

    public static void main(String[] args) {
        System.out.println("--- Linked scan vs balanced-tree lookup (conceptual) ---\n");

        List<Integer> list = new ArrayList<>(SIZE);
        TreeSet<Integer> tree = new TreeSet<>();
        for (int i = 0; i < SIZE; i++) {
            list.add(i);
            tree.add(i);
        }
        int target = SIZE - 1;

        for (int i = 0; i < WARMUP; i++) {
            list.contains(target);
            tree.contains(target);
        }

        long t0 = System.nanoTime();
        for (int i = 0; i < REPS; i++) {
            list.contains(target);
        }
        long listNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (int i = 0; i < REPS; i++) {
            tree.contains(target);
        }
        long treeNs = System.nanoTime() - t0;

        System.out.println("Size n = " + SIZE + ", lookups (last element) × " + REPS);
        System.out.printf("  ArrayList#contains (O(n) scan):     %8.2f ms%n", listNs / 1_000_000.0);
        System.out.printf("  TreeSet#contains  (O(log n) tree):   %8.2f ms%n", treeNs / 1_000_000.0);
        System.out.println();
        System.out.println("HashMap buckets: many colliding keys → long Node chain behaves like the list case;");
        System.out.println("after treeify, that bucket behaves more like the tree case for searches in that bin.");
    }
}
