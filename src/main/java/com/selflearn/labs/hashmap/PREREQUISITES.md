# Prerequisites — before `HashMapInternalsLab`

Read this first if you are new to how `HashMap` works under the hood. After this, open `HashMapInternalsLab.java` and `DAY1_HASHMAP.md`.

### Supporting example classes (same package)

| Topic | Class | Run alone in IntelliJ |
|--------|--------|------------------------|
| §1 Hashing & collisions | `HashingCollisionsExample` | ✓ |
| §2 List vs tree lookup cost | `LookupCostComparisonExample` | ✓ |
| §3 Threshold constants | `HashMapJvmConstantsExample` | needs `--add-opens java.base/java.util=ALL-UNNAMED` |
| §4 Reflection basics | `PrivateFieldReflectionExample` | ✓ (no `HashMap` access) |
| All in order | `PrerequisitesRunner` | needs `--add-opens` (for §3) |

**Gradle (opens configured):** `./gradlew runPrerequisitesExamples`

---

## 1. Hashing & collisions

**Example:** `HashingCollisionsExample.java` — spread hash, `(n-1)&h`, same `hashCode` vs index collision, real `HashMap` with two distinct keys.

### What “hashing” means here

A **hash function** turns a key of arbitrary size into a **fixed-size integer** (in Java, a 32-bit `int`). For `HashMap`, the relevant value is **`key.hashCode()`**, possibly **mixed** again inside `HashMap` so that **all bits** of that `int` can influence the bucket index (see `DAY1_HASHMAP.md` — “hash spread”).

### Buckets and the array

Internally, `HashMap` keeps an **array** of **slots** (often called **bins** or **buckets**). Each slot can hold **nothing** (`null`), or **one entry** that may **chain** or **tree** more entries when keys collide.

To choose an index, `HashMap` uses the array length `n` (a power of two) and the hash:

```text
index = (n - 1) & hash   // same idea as hash % n when n is a power of two
```

So the hash is **mapped to a valid array index**. That is the **bucket** for this key (for that table size).

### What a collision is

Two **different** keys are **distinct** according to `equals(...)`, but they end up in the **same bucket** because their **hash-derived index is the same** (or they share the same hash path for that table size).

- **Same `hashCode()`** often implies **same bucket** (not always across resizes, but often for a given `n`).
- **Different `hashCode()`** can still **collide** on the index: `(n-1) & h1` can equal `(n-1) & h2`.

So: **collision** = “different keys, same bucket,” **not** necessarily “same `hashCode()`.”

### Why collisions matter

In the **best case**, each bucket has **0 or 1** entries: lookup is roughly **constant time** — go to the slot, compare the key once.

When a bucket holds **many** entries, the map must **search inside that bucket** (list or tree). Cost grows with **how many keys landed in that one slot** — that is why **bad hash functions** or **attack patterns** hurt **worst-case** performance.

### Mental model before the lab

- **`hashCode()`** → helps pick **which bucket**.
- **`equals(...)`** → decides **which key inside that bucket** is the right one.
- **Contract:** if two objects are **equal**, they **must** have the **same** `hashCode()`. The converse is **not** required (different hash codes can still collide on the index).

---

## 2. Linked list vs red–black tree (and “treeify”)

**Example:** `LookupCostComparisonExample.java` — `ArrayList#contains` (linear scan) vs `TreeSet#contains` (tree path); models the cost difference inside one overcrowded bucket.

### Traditional picture: linked list in one bucket

For a long time, the simple story was: **each bucket** is a **linked list** of `Node`s. Each node stores `key`, `value`, `hash`, and **`next`** (pointer to the next node in **that bucket**).

- **Insert** at the end or in the middle of that chain: walk `next` until you find an equal key or reach the end.
- **Lookup**: same walk.

If the chain length is **`k`**, that walk is **O(k)** in the worst case for **that bucket**.

### Why Java 8 added trees

If **`k`** becomes **large** (many colliding keys in **one** bucket), **linear scan** of a list becomes expensive. **Red–black trees** are a kind of **balanced binary search tree** with **guaranteed height** on the order of **log(size of tree)**. Operations in that bucket become **O(log k)** instead of **O(k)**.

So since **Java 8**, a bucket can be:

- **`Node` chain** (linked list along `next`), or
- **`TreeNode` red–black tree** (left/right/parent, color bit, ordering by hash/key).

**Treeify** = “replace this bucket’s linked structure with a red–black tree” when the implementation decides the list has become **too long** **and** the table is **large enough** (see section 3).

### Untreeify (short note)

Sometimes the tree becomes **small** again (removals, or **resize** splitting a bucket). The map can convert back to a **linked list** — that keeps **memory and pointer overhead** down when the tree is no longer worth it.

### Big-O shorthand (what “fast” means here)

| Structure inside one bucket | Typical worst-case search in that bucket |
|----------------------------|------------------------------------------|
| Linked list                | **O(k)** — `k` = number of entries in that bucket |
| Red–black tree             | **O(log k)** |

This does **not** change the fact that **resize** or **very large maps** still have their own costs; it **protects one hot bucket** from degrading to a **long linear scan**.

---

## 3. Thresholds (constants that gate treeify)

**Example:** `HashMapJvmConstantsExample.java` — reflection reads `TREEIFY_THRESHOLD`, `MIN_TREEIFY_CAPACITY`, `DEFAULT_LOAD_FACTOR`, etc. from `HashMap` (same VM flags as `HashMapInternalsLab`).

OpenJDK’s `HashMap` uses **named constants**. Two matter most for the lab.

### `MIN_TREEIFY_CAPACITY` (64)

**Meaning:** Do **not** treeify a bucket unless the **underlying array** has length **at least 64**.

If a bucket’s chain gets “long” but the table is **still small**, the problem is often **too few buckets**, not “we need a tree.” So the map **`resize()`s** (doubles the array, **redistributes** entries) instead of paying for **tree bookkeeping** on a tiny table.

**Fresh mental model:** “**First widen the highway** (bigger array); **only then** consider **trees** for a single jammed exit ramp.”

### `TREEIFY_THRESHOLD` (8)

**Meaning (intuitive):** The implementation uses **8** as the **collision-chain length** at which **treeify becomes eligible** once the table is big enough.

**Precise behavior (important):** In the actual `putVal` loop, treeify runs when a **counter** along the chain reaches **`TREEIFY_THRESHOLD - 1`** at the moment a **new** node is linked — which works out to converting when the **ninth** colliding key is added to a bucket that **already** had **eight** nodes in one chain (with a table size **≥ 64**). Many short tutorials say “more than 8 nodes” loosely; the **lab output** and `DAY1_HASHMAP.md` show the **exact** behavior.

**Do not confuse with:** **`load factor` (default 0.75)** and **`resize`** when `size > threshold` — that controls **when the whole array grows**, not treeify alone.

### `UNTREEIFY_THRESHOLD` (6) — optional detail

If a tree bin shrinks to **6 or fewer** elements (after removes / splits), it may become a **list** again.

### How this ties to `HashMapInternalsLab`

- **Lab A:** small initial capacity → you see **resize** instead of jumping straight to **trees**.
- **Lab B:** capacity **64** from the start → long collision chain → head of bucket becomes **`TreeNode`**.

---

## 4. Reflection (why the lab can “see inside” `HashMap`)

**Example:** `PrivateFieldReflectionExample.java` — `setAccessible` on a **private field of your own class** (no `--add-opens`). Then compare with `HashMapInternalsLab` / `HashMapJvmConstantsExample` for JDK `java.util` access.

### What reflection is

**Reflection** is the JVM’s ability to **inspect** and sometimes **invoke** code and data that are **not** exposed through normal public APIs: **private fields**, **private methods**, **constructors**, **annotations**, etc.

At runtime you can:

- **`Class.getDeclaredField("table")`** — get a `Field` object for the **internal array** of `HashMap`.
- **`field.setAccessible(true)`** — allow your code to **read** (or write) that field even though it is **private**.

That is how `HashMapInternalsLab` prints whether bucket `0` holds a plain **`Node`** or a **`TreeNode`**.

### Why `HashMap` hides `table`

The field is **private** on purpose:

- **Encapsulation** — callers should use **`put` / `get` / `forEach`**, not depend on layout.
- **Freedom to change** — JDK authors may **rename fields** or **change algorithms** between releases; **reflection-based hacks** can **break** on upgrade.

So: **reflection here is for learning and debugging**, not for production design.

### Modules and `--add-opens` (Java 9+)

Strong **encapsulation** of the **`java.base`** module can block illegal access to JDK internals. Reading **`HashMap`’s private `table`** from your app may require opening the package to your code. The Gradle task **`runHashMapLab`** adds:

```text
--add-opens java.base/java.util=ALL-UNNAMED
```

so the **JVM allows** deep reflection into **`java.util`** from **unnamed-module** application code.

**IntelliJ:** if you run `main` without Gradle, add the same line under **Run → Edit Configurations → VM options**.

### Ethics of reflection in real systems

- **Frameworks** (Spring, Hibernate, serialization libraries) use reflection heavily — with **supported** APIs and **clear contracts**.
- **Random `setAccessible` on JDK classes** in business code → **fragile**, often **forbidden** by security policy.

For this repo: **use reflection only to observe** `HashMap` for **learning**.

---

## Suggested order

1. This file (**prerequisites**).
2. Run **`./gradlew runPrerequisitesExamples`** (or each `*Example` class from IntelliJ).
3. Run **`./gradlew runHashMapLab`** and read the printed lines.
4. **`HashMapInternalsLab.java`** — follow the methods **Lab A** / **Lab B**.
5. **`DAY1_HASHMAP.md`** — staff-level depth, failure modes, `ConcurrentHashMap`, interviews.

---

## Quick checklist (can you answer these?)

- [ ] Why do two unequal keys sometimes land in the **same bucket**?
- [ ] Why is **same bucket** worse when the bucket holds **many** entries?
- [ ] Why might the JDK **resize** instead of **treeify** when the table is **small**?
- [ ] What is the difference between **“threshold for treeify”** and **“load factor resize”**?
- [ ] Why does the lab need **`--add-opens`** or Gradle’s **`jvmArgs`**?

When these are clear, you are ready to step through the code line by line.
