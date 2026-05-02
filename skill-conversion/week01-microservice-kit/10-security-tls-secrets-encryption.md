# Security Week 1 Essentials — TLS, Secrets, Encryption & Spring Hygiene

**Non-negotiable:** **Secrets never Git**–even private repos leak via forks, screenshots, SaaS scanners.

---

## 1. TLS vs SSL nomenclature

Colloquially "SSL certificates" persists—**enforce TLS ≥ 1.2** realistically **1.3** onward.

Handshake (simplified):

```text
ClientHello -> ServerHello + certificate chain
<- Key exchange + Finished messages
Symmetric session keys negotiated
HTTP encrypted over record layer
```

**Engineer tasks:**

| Task | Responsibility |
|------|------------------|
| Cert renewal automation | infra / platform owners |
| **SNI**/hostname correctness | misconfig ⇒ browser trust errors |
| Cipher suite modernization | forbid legacy EXPORT ciphers |

Spring Boot behind reverse proxy terminates TLS externally often—trust forwarded headers (**careful spoofing**) only when proxies strip external direct access.

---

## 2. Secret management patterns

| Tier | Fits |
|------|------|
| **Environment variables injected at runtime** | containers / k8s pod env referencing secret store |
| **HashiCorp Vault** | central dynamic credentials + auditing |
| **Cloud secret managers** (AWS SM, GCP SM) | tight IAM tying |
| **Kubernetes Secret** | cluster-local; etcd encryption posture matters |

YAML anti-pattern ❌:

```yaml
spring:
  datasource:
    password: PlainTextOMG   # NEVER
```

Preferred ✅:

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

Compose / k8s inject `DB_PASSWORD` from vault or `.env.local` (**gitignored template** `.env.example` without values).

---

## 3. Hashing vs Encryption

| Operation | Goal | Examples |
|-----------|------|----------|
| **Hashing (one-way)** | verify without storing raw secret | bcrypt/argon2 user passwords |
| **Encryption (two-way)** | recover original with key | AES for field-level at rest needing decrypt |
| **Digital signature** | integrity + authenticity asymmetrically | JWT signing conceptual |

Never roll custom crypto primitives—reuse audited libraries/protocols (`java.security`/BouncyCastle if justified).

---

## 4. Symmetric vs asymmetric

| Type | Behavior | Scenario |
|------|----------|-----------|
| **Symmetric (AES)** | same key encrypt/decrypt | bulk data confidentiality |
| **Asymmetric (RSA/ECDSA)** | public encrypt/sign verify ops | handshake / JWT signing asymmetric keys |

Hybrid TLS uses asymmetric for key negotiation + symmetric bulk encryption.

---

## 5. Password storage example (delegating security to Spring Security Crypto)

Typically:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

var encoder = new BCryptPasswordEncoder();
String hash = encoder.encode(rawPasswordFromRegistration);
boolean ok = encoder.matches(rawPasswordLoginAttempt, storedHash); // timing-safe-ish modern impl
```

**Rules:**

- unique random salt inherent to bcrypt iterations
- tune **work factor** upward as CPUs improve (`strength` param)
- **never** reversible store raw passwords—even “internal admins” tooling

---

## 6. Application configuration hardening snippets

Disable dev-only endpoints:

```yaml
management:
  server:
    port: ${ACTUATOR_PORT:8081}       # segregate actuator from primary port optionally
```

HTTP security starter (minimal conceptual):

```java
@Bean
SecurityFilterChain http(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable()) // reconsider for cookie sessions
              .authorizeHttpRequests(reg -> reg
                  .requestMatchers("/actuator/health/**").permitAll()
                  .anyRequest().authenticated())
              .httpBasic(Customizer.withDefaults()) // illustrative only Week1
              .build();
}
```

Iterate later to OAuth2/JWT resource server patterns aligned with Gateway.

---

## 7. Threat modeling teaser (tie OWASP mindset)

Identify:

| Surface | Abuse |
|---------|-------|
| Public POST `/users` | mass registration / credential stuffing |
| Mass assignment | unintended field updates bypassing whitelist DTO layering |
| Verbose errors | oracle enumeration |
