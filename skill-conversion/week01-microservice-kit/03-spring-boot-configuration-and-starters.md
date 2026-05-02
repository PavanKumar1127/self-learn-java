# Spring Boot — Auto-Configuration, Starters & Externalized Config

**Goal:** reproducible runnable service per environment (**dev/stage/prod**) without rewriting code.

---

## 1. What `@SpringBootApplication` aggregates

Roughly expands to configuration enabling:

```java
@SpringBootApplication
public class OrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersApplication.class, args);
    }
}
```

Behind the curtain:

| Concern | Description |
|---------|--------------|
| **Component scan** default | Packages under application's package |
| **Auto-configuration import** conditional beans | Adds Tomcat/DataSource/actuator/etc if classpath matches |
| **Configuration properties processors** (`spring-boot-configuration-processor`) compile-time hints for IDE autocomplete |

Disable specific auto-configuration only when needed:

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BatchOnlyApp { }
```

---

## 2. Starter dependencies (why they exist)

**Problem:** manual dependency version alignment across Spring Framework, Jackson, Tomcat, logging bridges.

**Starter example — web API + JPA + Flyway + validation + actuator:**

```kotlin
// build.gradle.kts (illustrative)
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
}
```

**Best practice:** prefer official starters for consistent stack versions (BOM managed by Spring Boot Gradle plugin).

---

## 3. Embedded Tomcat

Spring Boot bundles **embedded servlet container** launching on **classpath bootstrap**.

**Tune thread pool/backlog** carefully under load—the defaults are instructional, not global truth.

YAML reference example:

```yaml
server:
  port: ${PORT:8080}
  tomcat:
    threads:
      max: ${TOMCAT_MAX_THREADS:200}
```

---

## 4. Profiles — environment separation without `if chaos`

Activate:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

Use **profile-specific overlays**:

```
application.yml               # baseline
application-dev.yml           # local docker-compose DB
application-staging.yml
application-prod.yml
```

Merge rules: baseline + profile-specific override.

---

## 5. `application.yml` — patterns

```yaml
spring:
  application:
    name: order-service

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/orders}
    username: ${DB_USER:orders_app}
    password: ${DB_PASSWORD} # NEVER commit real password file

  jpa:
    hibernate:
      ddl-auto: validate   # NEVER create-drop in prod
    open-in-view: false     # disables lazy loading accidentally across HTTP lifecycle

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      probes:
        enabled: true   # facilitates k8s liveness/readiness integration
```

**Principle:**

- **`ddl-auto=validate`** in production migrations belong to Flyway—not Hibernate schema delta surprises.

---

## 6. `@ConfigurationProperties` — typed safe config bean

Define:

```java
@ConfigurationProperties(prefix = "app.orders")
public record OrderProperties(
        int maxRetries,
        Duration defaultTimeout,
        RateLimit limits
) {
    public record RateLimit(int perMinutePerUser, int burst) { }
}
```

Enable strict binding bean:

```java
@Configuration
@EnableConfigurationProperties(OrderProperties.class)
public class AppConfig {}
```

YAML:

```yaml
app:
  orders:
    max-retries: 3
    default-timeout: 2s
    limits:
      per-minute-per-user: 60
      burst: 20
```

**Best practice:** centralize magic numbers as properties so load tests adjust without code changes.

---

## 7. Real-world scenario: multi-container local dev

**docker-compose** supplies env variables:

```yaml
services:
  orders:
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_URL: jdbc:postgresql://db:5432/orders
      DB_USER: orders_app
      DB_PASSWORD: supersecret
```

Spring reads via `${DB_PASSWORD}` — **rotate** values per environment; **never** mirror prod secrets into developer laptops long-term.

---

## 8. Failure patterns

| Symptom | Likely cause |
|---------|--------------|
| Random port binding failure | another process using `8080` |
| Hibernate surprises | `ddl-auto` not validated + stray schema drift |
| Actuator unsecured | exposing sensitive endpoints inadvertently—narrow exposure + authenticate |
