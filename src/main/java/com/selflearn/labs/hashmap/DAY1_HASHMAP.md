# Day 1: Java Collections & Hash Internals — `java.util.HashMap`

Self-learn notes (performance, concurrency, distributed-adjacent trade-offs). Companion lab: `HashMapInternalsLab.java`, run `./gradlew runHashMapLab`.

**New to hashing / treeify / reflection?** Read **`PREREQUISITES.md`** in this package first.

**Correction (common misconception):** `TREEIFY_THRESHOLD = 8` does **not** mean “treeify as soon as the bucket has 8 entries.” In OpenJDK `HashMap.putVal`, treeify runs when appending after a collision-chain walk with `binCount >= TREEIFY_THRESHOLD - 1` (i.e. **7**), which happens when the chain **already has eight nodes**—so the **ninth** colliding insert triggers `treeifyBin` (if the table is large enough). The lab output matches this.

---

## Architecture & internals

### Array + per-bucket structure

`HashMap` is a **power-of-two** array of bins. Each bin is either `null`, a **`Node` chain** (`next` pointers), or a **`TreeNode` red–black tree** when a bin becomes pathological.

### Indexing (why power-of-two)

Index is `(n - 1) & hash` instead of `hash % n`. That is a **mask**: one subtract, one bitwise AND—fast on hot paths and friendly to superscalar CPUs. Resize is **`n <<= 1`**, which **splits** each bin by one more bit of hash (`e.hash & oldCap`), so rehashing stays **O(bin size)** per old slot, not a full global recompute.

### Hash spread

`static final int hash(Object key)` mixes `key.hashCode()` with `h ^ (h >>> 16)` so **high bits participate** in the masked index. Without this, keys whose `hashCode()` differs only in high bits would **cluster** in the same buckets when `n` is small—**latency spikes** under load until resize spreads them.

### Load factor and resize

Default load factor **0.75** trades **space vs. probe/chain length**. When `size > threshold` (`threshold ≈ capacity * loadFactor`), **`resize()`** doubles the table (until `MAXIMUM_CAPACITY`). Resize is **stop-the-world for that map instance** (single-threaded semantics): allocate new array, **relink** nodes—CPU cache pressure and allocation burst.

### Treeify (the full gate, not “8”)

1. **Per-bin chain path:** In `putVal`, when following `p.next`, on append the code checks `binCount >= TREEIFY_THRESHOLD - 1` (comment: *“-1 for 1st”*). That fires when adding the **ninth** entry to an **eight-node** chain. Source: [OpenJDK `HashMap.java` — `putVal` / `treeifyBin`](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/HashMap.java).
2. **Table size gate:** `treeifyBin` **refuses** to treeify if `tab.length < MIN_TREEIFY_CAPACITY` (**64**); it **`resize()`s** instead. So **small tables grow by doubling** until `n ≥ 64`, avoiding **RB-tree overhead** when the real problem is **too few buckets**.

### Why a tree at all?

A long chain makes **`get`/`put` O(n)** in the worst case. An **RB-tree** caps single-bin operations at **O(log n)** with **bounded rotations**—a **latency insurance policy** against **adversarial or accidental hash collisions**, not the common case.

### Untreeify

`UNTREEIFY_THRESHOLD = 6`: after deletes or resize **split**, if a tree bin is small enough, it **flattens** back to a list—trees are **not** a permanent commitment for that slot.

### Memory layout (heap)

Entire structure is **on-heap**: `table` array, `Node`/`TreeNode` objects, `Entry` for iterators. **No direct memory**; GC tracks all links. **Indirect cost:** pointer chasing on chains/trees → **cache misses**; tree nodes are **heavier** than list nodes (parent/child/color).

### Disk I/O

`HashMap` itself is **in-memory**; it does not define sequential vs random disk access. **Persistence** layers built *around* maps (e.g. write-ahead logs, SSTables) are where **sequential append** vs **random read** trade-offs matter.

---

## Failure mode analysis

| Failure | What goes wrong |
|--------|------------------|
| **Bad `hashCode()` / `equals()` contract** | Broken symmetry/transitivity → **lost entries**, silent corruption, or infinite loops in custom structures. `HashMap` assumes **consistent** `hashCode` with `equals`. |
| **Mutable keys** | If `hashCode`/`equals` change after `put`, lookups **miss** or hit the wrong bin → “ghost” entries and **memory leaks** until resize clears semantics. |
| **Collision storms** | Many keys in one bin → list **or** tree; tree helps **worst-case** time but **not** cache locality; still **hot lock** in `ConcurrentHashMap` for that bin. |
| **Resize storms** | Bulk `put` on a map sized too small → **repeated doubles**, each **O(n)** work and allocation → **GC + pause** interaction under load. |
| **Over-trusting “O(1)”** | Under attack or skewed keys, behavior is **O(n)** or **O(log n)** per op in a bin; **SLIs** should assume tail latency, not mean only. |
| **Iterator fail-fast** | `ConcurrentModificationException` if structurally modified (unless iterator API says otherwise); **not** a concurrency primitive. |
| **CHM misuse** | **Composite actions** (`if (!map.containsKey(k)) map.put(k,v)`) are **not atomic**; need **`putIfAbsent`**, **`compute`**, etc. |

---

## Trade-off matrix

| | **`HashMap`** | **`LinkedHashMap`** | **`ConcurrentHashMap`** |
|--|---------------|---------------------|---------------------------|
| **Time (avg / worst)** | get/put **O(1)** / **O(n)** bin (tree: **O(log n)** in bin) | Same as `HashMap` + **linked-list maintenance** | get **O(1)** typical; contended bin **serialized**; tree bins similar |
| **Space** | **Lowest** extra metadata | **Extra** `before/after` links (and optionally **order** bookkeeping) | **Volatile/CAS** fields, **forwarding** during resize, tree nodes under collision |
| **Throughput vs latency** | **Highest** single-thread throughput | Slightly **lower** (link updates on access if LRU) | **Higher** concurrent throughput than `Hashtable`; **latency** can spike on **bin contention** |
| **Scalability (threads)** | **Not safe** | **Not safe** | **Designed** for many threads; **fine-grained** (not one global lock) |
| **Iteration order** | **Undefined** | **Insertion** or **access** order | **Weakly consistent** iterators; **no** `ConcurrentModificationException` from self in same way |

### `Hashtable` vs `ConcurrentHashMap` (staff-level nuance)

- **`Hashtable`:** **`synchronized`** on **public methods** → effectively **coarse locking** on the whole table for each op → **poor multi-thread scalability**.
- **Legacy `ConcurrentHashMap` (Java 7):** **Segment** array (segment = portion of bins) → **N-way** lock striping; still **coarser** than ideal under skew.
- **Modern `ConcurrentHashMap` (Java 8+):** **No segments** in the sense of the old implementation; **`CAS`** on empty bins, **`synchronized` on the first node** of a bin for updates, **red-black bins** under heavy collision, and **forwarding nodes** during resize. Interview answer **“segments vs class-level lock”** is **historically** fair; for **current** JDK, say **“CAS + per-bin synchronization vs global synchronized methods.”**

---

## Java ecosystem footprint

### JDK

`java.util.HashMap`, `LinkedHashMap`, `IdentityHashMap`, `WeakHashMap`, `ConcurrentHashMap`; `HashSet` / `LinkedHashSet` **delegate** to these maps; `Collections` wrappers (`Collections.synchronizedMap` still **one lock**). **`Object.hashCode` / `Arrays.hashCode`** feed map quality. **`Map.copyOf`** (immutable) avoids sharing mutable maps.

### Spring Boot / Cloud

Request **attribute maps**, **bean** metadata maps, **`@RequestParam` / header** multi-value maps; **`ConcurrentHashMap`** for caches and registries in many starters; **session** and **security** filters use map-like structures (often **linked** or **concurrent** depending on scope).

### OSS

**Kafka** (client metadata, partition maps, metrics registries), **Redis** (server: **dict** = array + chain; **rehash** incremental—**different** from `HashMap` but same **collision** vocabulary), **Cassandra / Scylla** (partition keys → **token** distribution; **not** JDK `HashMap` but **hash partitioning** shows up in client drivers’ metadata maps), **Elasticsearch / Lucene** (term dictionaries on disk—**other** structures, but JVM side full of **maps** for cluster state).

---

## Hands-on IntelliJ lab

**Gradle:**

```bash
cd /path/to/self-learn
./gradlew runHashMapLab
```

**IntelliJ:** open `HashMapInternalsLab.java` → Run `main`. If not using Gradle, VM options:

`--add-opens java.base/java.util=ALL-UNNAMED`

**What you’ll see:** Lab **A** shows **`MIN_TREEIFY_CAPACITY`**: with a small table, **resize** happens instead of treeify. Lab **B** with **initial capacity 64** shows the head of bucket `0` flip from **`Node`** (linked list) to **`TreeNode`** after enough **identical-hash** keys.

**Also:** `./gradlew run --args hashmap` delegates to this lab from `Main`.

---

## “Architect” interview challenge (HLD)

**Design a multi-tenant rate limiter** (HTTP API) at **500k RPS** per region: **key** = `(tenantId, routeClass)`, **value** = counters + window metadata, **sub-ms** read path on hot keys, **no global lock** for reads.

**Probe:** Why is a **single giant `ConcurrentHashMap`** possibly **wrong**? (mega-bin on **popular tenant**, **resize** visibility, **GC** of `Long` keys.) What **sharding** strategy (e.g. **fixed shard count**, **consistent hashing**, **striped CHMs**) matches **CHM’s** per-bin locking model? Where do you **avoid** `hashCode` **hotspots** on shard keys?

---

## Blind 75 connection

- **[Two Sum](https://leetcode.com/problems/two-sum/)** — **Hash map** from **value → index** (or **complement** lookup); same **O(1) amortized** expectation as `HashMap`, same **worst-case** awareness if **hash** degrades.
- **[Contains Duplicate](https://leetcode.com/problems/contains-duplicate/)** — **`Set`** (usually **`HashSet`** → **`HashMap`**) for **membership** in **O(n)** time, **O(n)** space.

---

## Deep-dive sources

- [OpenJDK `HashMap` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/HashMap.java) (ground truth for **treeify**, **resize**, **MIN_TREEIFY_CAPACITY**).
- [OpenJDK `ConcurrentHashMap` source](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/ConcurrentHashMap.java).
- **Classic paper (distributed hash tables, vocabulary):** [Chord: A Scalable Peer-to-peer Lookup Service](https://pdos.csail.mit.edu/papers/chord:sigcomm01/chord_sigcomm.pdf).
- **Engineering blogs (hashing / scale adjacent):** [Instagram Engineering](https://instagram-engineering.com/) (site search: **sharding** / **PostgreSQL**); [Uber Engineering](https://eng.uber.com/) (site search: **Cassandra** / **hot partition**).

---

## 60-second vlog snippet: “Treeify”

*`HashMap` is an array of buckets. Normally collisions are a **linked list** of `Node`s—fast when the list is short. If **one bucket** gets **too many** colliding keys, lookups stop being constant-time and become a **linear scan**. OpenJDK sets two knobs: **`TREEIFY_THRESHOLD`** and **`MIN_TREEIFY_CAPACITY`**. Until the table has at least **64** slots, it **doubles the array** instead of paying for trees. Once the table is big enough, if a single bucket’s chain grows past the threshold, that bucket becomes a **red–black tree**: **O(log n)** in that bucket instead of **O(n)**. It’s **insurance** against bad hashes or attacks—not the happy path. After shrinks or splits, small trees can **untreeify** back to a list. That’s treeify: **list for speed, tree for worst-case safety**—but only when the table is **wide enough** to deserve it.*

---

## One-line summary

Power-of-two masking + **hash spread** optimize the common case; **treeify** is **gated** by **both** collision depth **and** `n ≥ 64`; **`ConcurrentHashMap`** today is **CAS + per-bin locks**, not merely “segments vs `Hashtable`.”
