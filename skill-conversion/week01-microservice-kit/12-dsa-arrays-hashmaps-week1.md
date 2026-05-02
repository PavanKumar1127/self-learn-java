# DSA Week 1 — Arrays, Prefix Sums, Sliding Window, Two Pointers, Hashing

Map each pattern to microservice intuition (rate counters, duplicate detection dedup tables, sliding rate windows).

---

## 1. Pattern → When to apply

| Pattern | Recognize by | Complexity sweet spot |
|---------|--------------|-----------------------|
| **HashMap frequency** | Count occurrences / complement search | Insert avg `O(1)` |
| **Prefix sum** | Range sums / counts after preprocessing | Prep `O(n)` query `O(1)` |
| **Sliding window** | Contiguous optimize with shrink/expand two indices | Usually `O(n)` |
| **Two pointers sorted** | Pair sums on sorted arrays | `O(n)` |
| **Two pointers unsorted** | partition / palindrome style | `O(n)` |

---

## 2. Prefix sum template

Given array `a[0..n-1]`, define:

```text
pref[0] = a[0]
pref[i] = pref[i-1] + a[i]
```

Range sum `l..r` inclusive:

```text
sum(l,r) = pref[r] - (l==0 ? 0 : pref[l-1])
```

**Variant — binary indexed tree / segment tree** later for point updates (Weeks beyond 1).

### Subarray sum equals K (LeetCode 560 idea)

Key idea: transform to **prefix difference** using frequency map of prefix sums.

---

## 3. Sliding window — longest substring without repeating (LeetCode 3)

Invariant: window `[left,right]` always **unique** characters via `Map<Character,Integer>` last index.

Expand `right`, if duplicate inside window jump `left` beyond prior index.

---

## 4. Two Sum (LeetCode 1)

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> index = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int need = target - nums[i];
        if (index.containsKey(need)) {
            return new int[] { index.get(need), i };
        }
        index.put(nums[i], i);
    }
    throw new IllegalArgumentException("no solution");
}
```

Edge: **duplicate values** allowed if indices differ—map overwrites strategy must align (store first index only works if pair uses earlier index—validate reasoning).

---

## 5. Contains Duplicate (LeetCode 217)

Use `HashSet` insert—`O(n)` time `O(n)` space.

Bitset style micro-optimization only if bounded small integer domain (rare interviews).

---

## 6. Product of Array Except Self (LeetCode 238)

Prefix products left-to-right, suffix right-to-left **without division**—two pass `O(n)` `O(1)` extra if output array not counted.

Zeros require branch discipline.

---

## 7. Subarray Sum Equals K (560) — sketch

Maintain running prefix `cur` and map `freq` counting occurrences prefix sums encountered.

Relationship:

```java
cur += nums[i];
// need prior prefix such that cur - prev = K -> prev = cur - K
```

Edge: empty prefix (`0`) frequency seeding.

---

## 8. Complexity discipline checklist post-solve

1. Sorted vs unsorted input assumptions?
2. Mutations vs static array?
3. Integer overflow intermediate sums/products?
4. Empty input / singleton?
5. Duplicates permissible?

---

## 9. System design linkage (Week1 bridge)

| DSA Idea | Backend analogy |
|-----------|----------------|
| Sliding window counters | Sliding **rate limiting** buckets |
| Prefix sums | Histogram aggregation micro-batching |
| Hash frequency | **Idempotency** key dedupe map |
| Range query static | CQRS read model precomputations |

Track **10 solves** checklist in README deliverables numbering pass/fail rework columns.
