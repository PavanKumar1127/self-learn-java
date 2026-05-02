# Exception Handling & Standardized API Errors

**Critical production principle:** **`5xx` only for unexpected internal faults**; **`4xx` for caller-correctable violations** with machine-readable codes when possible.

---

## 1. Checked vs unchecked in service code

Trend in microservices stacks:

```java
// Domain unchecked fault -> mapped deliberately in advice
public class EmailAlreadyRegisteredException extends RuntimeException {
    private final String email;

    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered");
        this.email = email;
    }

    public String email() {
        return email;
    }
}
```

Checked exceptions exploding across layers hinder composition—wrap only at integration boundaries consciously.

---

## 2. Reusable JSON error body (minimal standard)

RFC 7807 **Problem Details** inspires shape—adapt to org standard:

```json
{
  "timestamp": "2026-05-01T10:15:30.123Z",
  "status": 409,
  "error": "Conflict",
  "code": "USER_EMAIL_TAKEN",
  "message": "Email already registered",
  "path": "/api/v1/users",
  "traceId": "7f3c2b1a9e8d"
}
```

**Why `code`:** stable for clients to branch logic without fragile string matching on `message`.

---

## 3. Structured error payload type

```java
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId
) {
}
```

Retrieve trace ID via MDC/filter (tie-in with logging guide).

---

## 4. Global `@ControllerAdvice`

Snippet imports (conceptual):

```java
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiError problem(HttpServletRequest req, HttpStatus status, String code, String message) {
        String traceId = Optional.ofNullable(MDC.get("traceId")).orElse("n/a");
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req.getRequestURI(),
                traceId
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError onValidation(HttpServletRequest req, MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(req, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", msg);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError duplicateEmail(HttpServletRequest req, EmailAlreadyRegisteredException ex) {
        return problem(req, HttpStatus.CONFLICT, "USER_EMAIL_TAKEN",
                "Email already registered: " + ex.email());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError catchAll(HttpServletRequest req, Exception ex) {
        // log stack trace server-side ONLY
        // never leak internals to clients beyond generic wording
        return problem(req, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Unexpected error");
    }
}
```

---

## 5. When **not** to translate to JSON

Certain security-sensitive paths may intentionally return **`401`** without leaking enumeration details (`username exists?`). Organization policy drives obfuscation severity.

---

## 6. Correlation discipline

Logs must include **`traceId` / `correlation id`** aligning with gateways & downstream calls—customers paste trace IDs during incidents—make them **first-class** in error payloads.
