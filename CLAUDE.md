# CLAUDE.md - AI Agent Guidelines

This file contains critical rules for AI agents working on the spring-security-boot project.

## Communication Language

- ✅ **使用中文与用户交互** - 所有面向用户的响应和交流必须使用中文
- ✅ 代码注释和文档可以使用中英文混合
- ✅ 技术术语保持英文（如 Maven, Spring Boot, JWT 等）

## Quick Reference

- **Language:** Java 1.8
- **Framework:** Spring Boot 2.7.18, Spring Security 5.7.11
- **Build Tool:** Maven 3.x
- **Detailed Rules:** See `_bmad-output/project-context.md`

## Critical Rules

### Dependency Injection
- ✅ **Use constructor injection** with `final` fields
- ❌ **Never use field injection** (`@Autowired` on fields)

### Import Statement Style
- ✅ **Use import statements** for all classes/interfaces
- ❌ **Never use fully qualified names in class declarations**

**When dealing with same-named classes/interfaces:**
```java
// ✅ CORRECT: Import the primary one, use FQN only for the secondary
import org.springframework.security.authentication.AuthenticationProvider;

public class DaoAuthenticationProvider
        implements AuthenticationProvider,
                   com.original.security.core.authentication.AuthenticationProvider {
    // ...
}

// ❌ WRONG: Using FQN for both
public class DaoAuthenticationProvider implements
        org.springframework.security.authentication.AuthenticationProvider,
        com.original.security.core.authentication.AuthenticationProvider {
    // ...
}
```

### Code Style
- Use SLF4J for logging (`log.info()`, `log.error()`)
- Never use `System.out.println()` or `printStackTrace()`
- All public APIs must have JavaDoc with `@author` and `@since`
- No magic values - define constants

### Response Format
- Success: `Response.successBuilder(data).build()`
- Error: `Response.errorBuilder(data).build()` or `Response.withBuilder(code).msg(message).build()`

## Build Commands

```bash
mvn clean install              # Build entire project
mvn test -pl security-core     # Test specific module
mvn compile                    # Compile only
```

## Test Naming Convention

Pattern: `test{MethodName}_{Scenario}_{ExpectedResult}`

Examples:
- `testAuthenticate_ValidCredentials_ReturnsAuthentication`
- `testAuthenticate_InvalidPassword_ThrowsBadCredentialsException`
- `testSupports_NullClass_ReturnsFalse`

---
Last updated: 2026-02-28
