package com.selflearn.labs.hashmap;

import java.util.HashMap;
import java.util.Objects;

/**
 * Supports prerequisite doc {@code PREREQUISITES.md} §1 — hashing, bucket index, and collisions.
 * <p>
 * Run this class from IntelliJ, or: {@code ./gradlew runPrerequisitesExamples}
 */
public final class HashingCollisionsExample {

    /**
     * Same spread {@link HashMap} applies to {@code key.hashCode()} before masking (simplified path).
     */
    static int spreadHash(int h) {
        return h ^ (h >>> 16);
    }

    static int bucketIndex(int tableLengthPowerOfTwo, int hashCode) {
        int n = tableLengthPowerOfTwo;
        int h = spreadHash(hashCode);
        return (n - 1) & h;
    }

    public static void main(String[] args) {
        System.out.println("--- Hashing & collisions ---\n");

        int n = 16;
        System.out.println("Assume HashMap table length n = " + n + " (power of two).");
        System.out.println("Bucket index = (n - 1) & spread(hashCode)\n");

        System.out.println("A) Same hashCode → same bucket (for this n):");
        var a = new Key("alice", 42);
        var b = new Key("bob", 42);
        System.out.println("  key " + a + " hashCode=" + a.hashCode() + " → index " + bucketIndex(n, a.hashCode()));
        System.out.println("  key " + b + " hashCode=" + b.hashCode() + " → index " + bucketIndex(n, b.hashCode()));
        System.out.println("  equals? " + a.equals(b) + " (collision: same bucket, different keys)\n");

        System.out.println("B) Different hashCode → can still share a bucket (index collision):");
        int h1 = 1;
        int h2 = 17;
        System.out.println("  hashCode " + h1 + " → index " + bucketIndex(n, h1));
        System.out.println("  hashCode " + h2 + " → index " + bucketIndex(n, h2));
        System.out.println("  (n-1) & spread(h) is " + ((n - 1) & spreadHash(h1)) + " for both here.\n");

        System.out.println("C) Putting colliding keys in a real HashMap (still works; bucket chains internally):");
        var map = new HashMap<Key, String>();
        map.put(a, "A");
        map.put(b, "B");
        System.out.println("  map.get(a)=" + map.get(a) + ", map.get(b)=" + map.get(b));
        System.out.println("  size=" + map.size());
    }

    /** Key with configurable hashCode for pedagogy (not for production keys). */
    static final class Key {
        private final String name;
        private final int forcedHash;

        Key(String name, int forcedHash) {
            this.name = name;
            this.forcedHash = forcedHash;
        }

        @Override
        public int hashCode() {
            return forcedHash;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && forcedHash == k.forcedHash && Objects.equals(name, k.name);
        }

        @Override
        public String toString() {
            return name + "(hash=" + forcedHash + ")";
        }
    }
}
