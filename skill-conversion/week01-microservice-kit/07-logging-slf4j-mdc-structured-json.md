# Logging — SLF4J, Logback, MDC & Structured Logs

Observable systems **start with consistent logs**.

---

## 1. Layers

| Library | Responsibility |
|---------|----------------|
| **SLF4J** | Facade API (`Logger`) |
| **Logback / Log4j2** | Implementation backing appenders/layouts |

Spring Boot starters wire **SLF4J → Logback** by default unless intentionally replaced.

---

## 2. Logger acquisition pattern

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public void place(UUID id) {
        log.atInfo().addKeyValue("orderId", id).log("placed");
    }
}
```

**Naming:** bind logger to enclosing class—not string free-form names.

---

## 3. MDC (`MappedDiagnosticContext`)

Thread-local-ish map injecting contextual fields (**trace id**, **`userId`**, tenant).

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
                .orElseGet(() -> UUID.randomUUID().toString());
        try (var ignored = MDC.putCloseable("traceId", traceId)) {
            response.setHeader("X-Trace-Id", traceId);
            chain.doFilter(request, response);
        }
    }
}
```

**Production bug:** servlet async / reactive flows require **careful** MDC propagation—thread shifts drop context unless propagated (`TaskDecorator` reactive variant).

Week 1 path: servlet stack + **`try`-with-resources** on `MDC.putCloseable` (SLF4J **2.x**) clears automatically at block exit — still verify async paths later.

---

## 4. JSON structured logging snippet (`logback-spring.xml` idea)

```xml
<!-- pseudo-sample: use logstash-logback-encoder dependency in real project -->
<configuration>
    <springProperty scope="context" name="SERVICE" source="spring.application.name"/>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <mdc/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

Benefit: ingestion pipelines (**ELK/OpenSearch/Loki**) index fields cleanly.

---

## 5. What to log vs not

| OK | Avoid |
|----|-------|
| business ids (orderId) | passwords, tokens, PAN, secrets |
| decision outcomes | entire raw payloads with PII |
| latency deltas | gigantic serialized objects each request |

Introduce **`DEBUG`** guard with sampling for deep traces (never default prod `TRACE` universally).
