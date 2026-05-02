# Docker — Images, Layers, Compose & Secure Spring Containers

Deliverable mindset: reproducible **`docker compose up`** for teammates & CI—not "works on my laptop".

---

## 1. Core vocabulary

| Term | Meaning |
|------|---------|
| **Image** | Immutable artifact built from Dockerfile layers |
| **Container** | Running instance of image + writable layer |
| **Layer** | Cached filesystem delta—order matters (`COPY pom` caching) |
| **Volume** | Persistent data outside ephemeral container filesystem |
| **Network** | Bridge enabling service DNS (`db`, `kafka`, ...) |

---

## 2. Multi-stage Dockerfile pattern (Gradle or Maven)

Gradle conceptual:

```dockerfile
# ---------- build ----------
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon clean bootJar

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS="-XX:+UseContainerSupport"
WORKDIR /opt/app

# non-root safety
RUN useradd --create-home spring
USER spring

COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080

# align with actuator /health probes
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/app/app.jar"]
```

Notes:

- Prefer **`jlink` / distroless / slim variants** advanced later—Temurin JRE pragmatic Week 1.
- Install **`curl`** in runtime image minimally or switch health check to **`wget`/pure Java** tooling.

---

## 3. `docker-compose.yml` illustrating DB wiring

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_USER: orders
      POSTGRES_PASSWORD: orders
      POSTGRES_DB: orders
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  orders:
    build: .
    depends_on:
      - db
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_URL: jdbc:postgresql://db:5432/orders
      DB_USER: orders
      DB_PASSWORD: orders
      JAVA_OPTS: -Xmx512m
    ports:
      - "8080:8080"

volumes:
  pgdata:
```

**Network DNS:** hostname `db` resolves inside compose network (**not `localhost`** from container vantage).

---

## 4. Security practices checklist

| Practice | Reason |
|---------|-------|
| **Non-root USER** | container breakout risk reduction |
| **Read-only root FS** (`read_only: true` + writable tmp volume) advanced | restricts tampering |
| **No secrets in image layers** | history extraction leaks |
| **Pin base image digest** CI later | reproducible patches |
| **Health checks** aligned | prevents routing traffic to uninitialized JVM |

---

## 5. Essential commands recap

```bash
docker compose build orders
docker compose up -d
docker compose logs -f orders
docker exec -it <container_id> bash
docker image ls
docker system prune       # CARE: removes unused caches
```

---

## 6. Real-world pitfalls

| Symptom | Typical cause |
|---------|---------------|
| `Connection refused jdbc:postgresql://localhost` | Compose service must reach `host db` |
| Slow rebuilds every time | copy order invalid—dependencies not cached separately |
| OOM kills | JVM `-Xmx` higher than cgroup memory limit unrealistic |
