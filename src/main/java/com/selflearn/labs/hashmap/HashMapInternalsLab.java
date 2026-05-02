package com.selflearn.labs.hashmap;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Observes {@link HashMap} bucket shape (linked nodes vs red-black tree) via reflection.
 * <p>
 * Start here if new: {@code PREREQUISITES.md}. Staff depth: {@code DAY1_HASHMAP.md}.
 * <p>
 * Run: {@code ./gradlew runHashMapLab} (opens {@code java.base/java.util} for field access).
 * IntelliJ: open this class → run {@code main}; add VM option:
 * {@code --add-opens java.base/java.util=ALL-UNNAMED}
 */
public final class HashMapInternalsLab {

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + Runtime.version());
        System.out.println();

        demonstrateResizeBeforeTreeifyWhenSmallCapacity();
        System.out.println();
        demonstrateTreeifyOnNinthCollidingPutWithCapacitySixtyFour();
    }

    /** Keys that collide in bucket 0 for default HashMap (power-of-two mask). */
    private static final class CollisionKey {
        private final int id;

        CollisionKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof CollisionKey ck && ck.id == id;
        }

        @Override
        public String toString() {
            return "K" + id;
        }
    }

    private static void demonstrateResizeBeforeTreeifyWhenSmallCapacity() throws Exception {
        System.out.println("=== Lab A: capacity < 64 → resize instead of treeify ===");
        var map = new HashMap<CollisionKey, String>(16, 0.75f);
        for (int i = 1; i <= 9; i++) {
            map.put(new CollisionKey(i), "v" + i);
            System.out.printf("after put %d: tableLength=%d, bucket0=%s%n",
                    i, tableLength(map), describeBucketHead(map));
        }
    }

    /**
     * OpenJDK: in {@code putVal}, when following a collision chain, {@code binCount} hits
     * {@code TREEIFY_THRESHOLD - 1} (7) at the append site only if the chain already has 8 nodes —
     * i.e. the <em>9th</em> distinct colliding key triggers {@code treeifyBin} (if {@code n >= 64}).
     */
    private static void demonstrateTreeifyOnNinthCollidingPutWithCapacitySixtyFour() throws Exception {
        System.out.println("=== Lab B: capacity 64 → 9th colliding put treeifies (TREEIFY_THRESHOLD=8, binCount gate) ===");
        var map = new HashMap<CollisionKey, String>(64, 0.75f);
        for (int i = 1; i <= 9; i++) {
            map.put(new CollisionKey(i), "v" + i);
            System.out.printf("after put %d: tableLength=%d, bucket0=%s, listDepth=%d%n",
                    i, tableLength(map), describeBucketHead(map), linkedListDepth(map));
        }
    }

    private static int tableLength(HashMap<?, ?> map) throws Exception {
        Object[] table = extractTable(map);
        return table == null ? 0 : table.length;
    }

    private static String describeBucketHead(HashMap<?, ?> map) throws Exception {
        Object[] table = extractTable(map);
        if (table == null || table.length == 0) {
            return "null";
        }
        Object head = table[0];
        if (head == null) {
            return "empty";
        }
        return head.getClass().getSimpleName();
    }

    private static int linkedListDepth(HashMap<?, ?> map) throws Exception {
        Object[] table = extractTable(map);
        if (table == null || table.length == 0) {
            return 0;
        }
        Object node = table[0];
        if (node == null) {
            return 0;
        }
        String name = node.getClass().getName();
        if (name.contains("TreeNode")) {
            return -1;
        }
        int depth = 1;
        Field next = node.getClass().getDeclaredField("next");
        next.setAccessible(true);
        while (true) {
            Object n = next.get(node);
            if (n == null) {
                return depth;
            }
            depth++;
            node = n;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object[] extractTable(HashMap<?, ?> map) throws Exception {
        Field f = HashMap.class.getDeclaredField("table");
        f.setAccessible(true);
        return (Object[]) f.get(map);
    }
}
