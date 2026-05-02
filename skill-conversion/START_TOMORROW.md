# Starting tomorrow — daily operating system

Assume **tomorrow is Day 1** of Skill Conversion (reset your counter if needed; consistency beats backdating).

## The 2-hour block (non-negotiable structure)

| Block | Minutes | Deliverable |
|-------|---------|-------------|
| **DSA** | 60 | 1 timed attempt + 1 refactor pass OR 2 shorter problems |
| **Backend / platform / SD** | 60 | See rotation in **`ROADMAP_90_DAY.md`** (Wed DB, Thu Docker/K8s, Fri system design sketch) |

**Rule:** end each session with **one sentence** written in your notebook: *what you would fix on retry*.

## Session opener (paste to your mentor / AI)

```text
Week X, Day Y | DSA topic: ___ | Backend topic: ___ | Platform topic (Docker/K8s/Vault/etc.): ___ | AI track row (optional): ___ 
```

You get a **2-sentence Industry Perspective**, then tasks — not lectures.

## Your first backend micro-goal (Day 1)

Pick **one** Spring feature (existing toy project or minimal module):

1. **`@RestController`** only: HTTP mapping, validation annotations, HTTP status semantics.
2. **`@Service`**: orchestration only — no persistence imports.
3. **`@Repository`/JPA**: queries + transactional boundary at **service** layer.

Deliverable for Day 1 backend hour: **one vertical slice** (e.g., `POST /items` → service → repo) plus a **written list**: which exceptions become which HTTP codes, and **where** `@Transactional` sits and why — **plus** a minimal **`Dockerfile`** that runs that service (non-root preferred).

**Bonus same week:** **`AI_DAILY_TRACK.md` Week 1** one row (~15 min).

## DSA opener (Day 1) — Prefix sum / hashing

Before any code: **name the invariant** (“prefix[i] means …”) **and** when prefix sum fails (mutations? negative-only constraints?).

Timed: ~25–30 min max on one problem class; remainder for cleanup and complexity note.

## Weekly rhythm (aligned with full roadmap)

- **Mon–Tue:** application code + DSA.
- **Wed:** database design / migrations / explains.
- **Thu:** Docker / Compose / Kubernetes nugget aligned to current week (`ROADMAP_90_DAY.md`).
- **Fri:** **system design** sketch + retrospective + Mistakes Log.
- **`AI_DAILY_TRACK.md`:** optional **15 min/day** (week column matches roadmap week).

**Weekend (optional):** whiteboard depth + redeploy-from-scratch drill.

## When you miss a day

Do **half block** next day (30+30): never zero; maintain streak of *touching* both tracks.
