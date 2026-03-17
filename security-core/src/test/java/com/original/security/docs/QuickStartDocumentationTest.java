package com.original.security.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies repository-level quick start documentation remains discoverable
 * and aligned with the current security bootstrap entry point.
 */
public class QuickStartDocumentationTest {

    private static final Pattern CODE_FENCE_PATTERN =
            Pattern.compile("(?s)```(xml|java|properties|bash)\\R.*?```");
    private static final Path REPOSITORY_ROOT = locateRepositoryRoot();
    private static final Path README_PATH = REPOSITORY_ROOT.resolve("README.md");
    private static final Path QUICK_START_PATH = REPOSITORY_ROOT.resolve("docs").resolve("quick-start.md");
    private static final Path TESTING_SUPPORT_PATH = REPOSITORY_ROOT.resolve("docs").resolve("testing-support.md");

    @Test
    void testQuickStartDocumentation_ExistsAndContainsRequiredSections() throws IOException {
        String readme = readFile(README_PATH);
        String quickStart = readFile(QUICK_START_PATH);

        assertAll(
                () -> assertTrue(Files.exists(README_PATH), "README.md should exist at repository root"),
                () -> assertTrue(Files.exists(QUICK_START_PATH), "docs/quick-start.md should exist"),
                () -> assertTrue(readme.contains("[30 分钟快速开始](docs/quick-start.md)"),
                        "README should expose a visible markdown link to quick start"),
                () -> assertTrue(quickStart.contains("@EnableSecurityBoot"), "Quick start should document @EnableSecurityBoot"),
                () -> assertTrue(quickStart.contains("spring.datasource.url"), "Quick start should include datasource configuration"),
                () -> assertTrue(quickStart.contains("security.network.cors.allowed-origins"), "Quick start should include required CORS configuration"),
                () -> assertTrue(quickStart.contains("mvn spring-boot:run"), "Quick start should include startup command"),
                () -> assertTrue(quickStart.contains("## 1. 适用对象与目标"), "Quick start should include the goal section"),
                () -> assertTrue(quickStart.contains("## 2. 前置条件"), "Quick start should include prerequisites"),
                () -> assertTrue(quickStart.contains("## 3. 添加依赖"), "Quick start should include dependency instructions"),
                () -> assertTrue(quickStart.contains("## 4. 添加配置"), "Quick start should include configuration instructions"),
                () -> assertTrue(quickStart.contains("## 5. 启用 `@EnableSecurityBoot`"), "Quick start should include bootstrap annotation"),
                () -> assertTrue(quickStart.contains("## 6. 创建第一个示例接口"), "Quick start should include example interface creation"),
                () -> assertTrue(quickStart.contains("## 7. 启动并验证"), "Quick start should include verification steps")
        );
    }

    @Test
    void testQuickStartDocumentation_LinksToTestingSupport() throws IOException {
        String quickStart = readFile(QUICK_START_PATH);
        assertAll(
                () -> assertTrue(Files.exists(TESTING_SUPPORT_PATH),
                        "docs/testing-support.md should exist for the quick start relative link"),
                () -> assertTrue(quickStart.contains("[测试支持工具文档](testing-support.md)"),
                        "Quick start should link to docs/testing-support.md using a relative path")
        );
    }

    @Test
    void testQuickStartDocumentation_UsesExpectedMarkdownCodeFenceLanguages() throws IOException {
        String quickStart = readFile(QUICK_START_PATH);
        long codeFenceCount = countCodeFences(quickStart);

        assertAll(
                () -> assertTrue(codeFenceCount >= 5,
                        "Quick start should contain multiple code fences with explicit languages"),
                () -> assertTrue(quickStart.contains("```xml"), "Quick start should include xml snippets"),
                () -> assertTrue(quickStart.contains("```java"), "Quick start should include java snippets"),
                () -> assertTrue(quickStart.contains("```properties"), "Quick start should include properties snippets"),
                () -> assertTrue(quickStart.contains("```bash"), "Quick start should include bash snippets")
        );
    }

    private String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private long countCodeFences(String content) {
        long count = 0;
        java.util.regex.Matcher matcher = CODE_FENCE_PATTERN.matcher(content);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static Path locateRepositoryRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();

        while (current != null) {
            Path readme = current.resolve("README.md");
            Path quickStart = current.resolve("docs").resolve("quick-start.md");
            Path rootPom = current.resolve("pom.xml");

            if (Files.exists(rootPom) && Files.exists(readme) && Files.exists(quickStart)) {
                return current;
            }
            current = current.getParent();
        }

        throw new IllegalStateException("Unable to locate repository root containing pom.xml, README.md and docs/quick-start.md");
    }
}
