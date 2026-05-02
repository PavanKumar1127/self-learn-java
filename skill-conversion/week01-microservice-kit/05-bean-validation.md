# Jakarta Bean Validation (Week 1 depth)

**Purpose:** reject bad input **before** domain logic & DB—return **structured 4xx**.

---

## 1. Core constraints (most used)

```java
public record CreateUserRequest(
        @NotBlank @Email String email,
        @Size(max = 120) String displayName,
        @Pattern(regexp = "^[A-Z]{2}$") String countryCode,
        @Positive Integer age
) { }
```

Semantics quick table:

| Annotation | Empty string? | Whitespace-only? | Collections |
|------------|---------------|------------------|-------------|
| `@NotNull` | allowed if not null | yes | size ignored |
| `@NotEmpty` | rejects | rejects if length 0 | size>0 |
| `@NotBlank` | rejects | rejects | N/A for collections |

---

## 2. Activate validation on controller inputs

```java
@PostMapping
public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
    return users.create(req); // only reached if constraints pass
}
```

**Principle:** annotate the **outer** DTO—cascade into nested objects explicitly.

---

## 3. Nested validation

```java
public record CreateOrderRequest(
        @NotBlank String customerId,
        @Valid @NotNull List<@NotNull LineItem> items
) {
    public record LineItem(
            @NotBlank String sku,
            @Positive int quantity
    ) { }
}
```

**Common miss:** forgetting `@Valid` on nested field → child constraints silently skipped.

---

## 4. Validation groups (create vs update)

Markers:

```java
public interface OnCreate { }

public interface OnUpdate { }

public record PatchUserRequest(
        @Null(groups = OnCreate.class)
        @NotNull(groups = OnUpdate.class)
        UUID id,

        @Size(min = 1, max = 120) String displayName
) { }
```

Controller:

```java
@PatchMapping("/{id}")
public UserResponse patch(@PathVariable UUID id,
                         @Validated(OnUpdate.class) @RequestBody PatchUserRequest body) {
    return users.patch(id, body);
}
```

**Real scenario:** same DTO struct reused but different field nullability rules depending on operation.

---

## 5. Custom class-level validator

Requirement: `endDate` must not precede `startDate`.

```java
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidDateRange {
    String message() default "end must be >= start";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingWindow> {

    @Override
    public boolean isValid(BookingWindow value, ConstraintValidatorContext ctx) {
        if (value.start() == null || value.end() == null) {
            return true; // let @NotNull handle nulls
        }
        return !value.end().isBefore(value.start());
    }
}
```

```java
@ValidDateRange
public record BookingWindow(Instant start, Instant end) { }
```

---

## 6. Service-layer validation (beyond annotations)

Use annotations for **structural** validity; use explicit code for **business invariants** duplicates across aggregates:

```java
public User create(CreateUserRequest req) {
    if (repository.existsByEmailNormalized(normalize(req.email()))) {
        throw new EmailAlreadyRegisteredException(req.email());
    }
    // persist
}
```

Those usually become **`409 Conflict`** via global handler.

---

## 7. Performance note

Deep object graphs validated per request—**keep DTOs shallow**; hydrate heavy graphs through services with batched queries—not giant nested JSON trees validated synchronously.
