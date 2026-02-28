package com.original.security.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * XssUtils 单元测试。
 * 测试 HTML 转义工具类的各种场景，包括复杂 XSS 攻击模式。
 *
 * @author Naulu
 * @since 1.0.0
 */
class XssUtilsTest {

    @Test
    void testEscapeHtml_SimpleScriptTag_EscapesCorrectly() {
        String input = "<script>alert('xss');</script>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<script>"));
        assertTrue(escaped.contains("&lt;script&gt;"));
    }

    @Test
    void testEscapeHtmlNull_ReturnsNull() {
        assertNull(XssUtils.escapeHtml(null));
    }

    @Test
    void testEscapeHtmlEmptyString_ReturnsEmptyString() {
        assertEquals("", XssUtils.escapeHtml(""));
    }

    @Test
    void testEscapeHtml_ImageTagWithOnload_EscapesCorrectly() {
        String input = "<img src=x onerror=alert(1)>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<img"));
        assertTrue(escaped.contains("&lt;img"));
    }

    @Test
    void testEscapeHtml_HrefWithJavascript_EscapesCorrectly() {
        String input = "<a href=\"javascript:alert(1)\">Click</a>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<a"));
        assertTrue(escaped.contains("&lt;a"));
    }

    @Test
    void testEscapeHtml_SvgOnload_EscapesCorrectly() {
        String input = "<svg onload=alert(1)>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<svg"));
        assertTrue(escaped.contains("&lt;svg"));
    }

    @Test
    void testEscapeHtml_MixedContent_EscapesHtmlOnly() {
        String input = "Hello <b>World</b> <script>alert(1)</script>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertTrue(escaped.contains("Hello"));
        assertTrue(escaped.contains("World"));
        assertFalse(escaped.contains("<b>"));
        assertFalse(escaped.contains("<script>"));
    }

    @Test
    void testEscapeHtml_SpecialCharacters_EscapesCorrectly() {
        String input = "Test & \" ' < >";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&quot;"));
        assertTrue(escaped.contains("&#39;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
    }

    @Test
    void testEscapeHtml_Iframe_EscapesCorrectly() {
        String input = "<iframe src=\"malicious.com\"></iframe>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<iframe"));
        assertTrue(escaped.contains("&lt;iframe"));
    }

    @Test
    void testEscapeHtml_InputTagWithAutofocus_EscapesCorrectly() {
        String input = "<input autofocus onfocus=alert(1)>";
        String escaped = XssUtils.escapeHtml(input);

        assertNotNull(escaped);
        assertFalse(escaped.contains("<input"));
        assertTrue(escaped.contains("&lt;input"));
    }

    // JavaScript 转义测试
    @Test
    void testEscapeJavaScript_SingleQuote_EscapesCorrectly() {
        String input = "'; alert('xss'); //";
        String escaped = XssUtils.escapeJavaScript(input);

        assertNotNull(escaped);
        assertTrue(escaped.contains("\\'"));
        assertFalse(escaped.contains("alert('xss')"));
    }

    @Test
    void testEscapeJavaScriptNull_ReturnsNull() {
        assertNull(XssUtils.escapeJavaScript(null));
    }

    @Test
    void testEscapeJavaScript_SpecialCharacters_EscapesCorrectly() {
        String input = "Test\\\"'\n\t";
        String escaped = XssUtils.escapeJavaScript(input);

        assertNotNull(escaped);
        assertTrue(escaped.contains("\\\\"));
        assertTrue(escaped.contains("\\\""));
    }

    // URL 编码测试
    @Test
    void testEncodeUrl_SpecialCharacters_EncodesCorrectly() {
        String input = "user<script>alert(1)</script>";
        String encoded = XssUtils.encodeUrl(input);

        assertNotNull(encoded);
        assertFalse(encoded.contains("<script>"));
        assertTrue(encoded.contains("%3Cscript%3E"));
    }

    @Test
    void testEncodeUrlNull_ReturnsNull() {
        assertNull(XssUtils.encodeUrl(null));
    }

    @Test
    void testEncodeUrl_Space_EncodesToPlus() {
        String encoded = XssUtils.encodeUrl("hello world");
        assertEquals("hello+world", encoded);
    }

    // HTML 清理测试
    @Test
    void testSanitizeHtml_ScriptTag_RemovesScript() {
        String input = "<p>Hello <script>alert('xss')</script> World</p>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("alert('xss')"));
        assertTrue(sanitized.contains("<p>"));
        assertTrue(sanitized.contains("Hello"));
    }

    @Test
    void testSanitizeHtmlNull_ReturnsNull() {
        assertNull(XssUtils.sanitizeHtml(null));
    }

    @Test
    void testSanitizeHtml_OnClickEvent_RemovesEvent() {
        String input = "<div onclick=\"alert(1)\">Click me</div>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("onclick"));
        assertTrue(sanitized.contains("<div>"));
    }

    @Test
    void testSanitizeHtml_JavascriptProtocol_RemovesProtocol() {
        String input = "<a href=\"javascript:alert(1)\">Click</a>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("javascript:"));
    }

    // XSS 检测测试
    @Test
    void testContainsXss_ScriptTag_ReturnsTrue() {
        String input = "<script>alert('xss')</script>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXssSafeText_ReturnsFalse() {
        String input = "Hello, world!";
        assertFalse(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXssNull_ReturnsFalse() {
        assertFalse(XssUtils.containsXss(null));
    }

    @Test
    void testContainsXss_OnErrorEvent_ReturnsTrue() {
        String input = "<img src=x onerror=alert(1)>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXss_JavascriptProtocol_ReturnsTrue() {
        String input = "<a href=\"javascript:alert(1)\">Click</a>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXss_EvalFunction_ReturnsTrue() {
        String input = "eval('alert(1)')";
        assertTrue(XssUtils.containsXss(input));
    }

    // 新增：改进后的 sanitizeHtml 测试
    @Test
    void testSanitizeHtml_StyleTag_RemovesStyle() {
        String input = "<style>body{background:url('javascript:alert(1)')}</style><p>Hello</p>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("<style"));
        assertTrue(sanitized.contains("<p>"));
    }

    @Test
    void testSanitizeHtml_VbscriptProtocol_RemovesProtocol() {
        String input = "<a href=\"vbscript:alert(1)\">Click</a>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("vbscript:"));
    }

    @Test
    void testSanitizeHtml_DataHtmlProtocol_RemovesProtocol() {
        String input = "<iframe src=\"data:text/html,<script>alert(1)</script>\"></iframe>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        // data:text/html 应该被移除
        assertFalse(sanitized.toLowerCase().contains("data:text/html"));
    }

    @Test
    void testSanitizeHtml_OnEventWithoutQuotes_RemovesEvent() {
        String input = "<img src=x onerror=alert(1)>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("onerror"));
    }

    // 新增：改进后的 containsXss 测试
    @Test
    void testContainsXss_DangerousTags_ReturnsTrue() {
        assertTrue(XssUtils.containsXss("<iframe src=\"evil.com\">"));
        assertTrue(XssUtils.containsXss("<object data=\"malicious.swf\">"));
        assertTrue(XssUtils.containsXss("<embed src=\"bad.swf\">"));
    }

    @Test
    void testContainsXss_SvgAndMathTags_ReturnsTrue() {
        // SVG 标签可以包含脚本
        assertTrue(XssUtils.containsXss("<svg onload=alert(1)>"));
        assertTrue(XssUtils.containsXss("<svg><script>alert(1)</script></svg>"));
        // MathML 标签也可以被利用
        assertTrue(XssUtils.containsXss("<math><mtext><script>alert(1)</script></mtext></math>"));
    }

    @Test
    void testContainsXss_VbscriptProtocol_ReturnsTrue() {
        String input = "<a href=\"vbscript:alert(1)\">Click</a>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXss_JavascriptWithSpaces_ReturnsTrue() {
        String input = "<a href=\"javascript :alert(1)\">Click</a>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXss_ExpressionFunction_ReturnsTrue() {
        String input = "<div style=\"width:expression(alert(1))\">Test</div>";
        assertTrue(XssUtils.containsXss(input));
    }

    @Test
    void testContainsXss_NormalText_ReturnsFalse() {
        // 正常文本不应被误报
        assertFalse(XssUtils.containsXss("Click on load button to continue"));
        assertFalse(XssUtils.containsXss("The key down event is triggered"));
        assertFalse(XssUtils.containsXss("Please submit your feedback"));
        assertFalse(XssUtils.containsXss("Evaluation of the project"));
    }

    @Test
    void testContainsXss_EmptyString_ReturnsFalse() {
        assertFalse(XssUtils.containsXss(""));
    }

    @Test
    void testContainsXss_ClosingScriptTag_ReturnsTrue() {
        assertTrue(XssUtils.containsXss("</script>"));
    }

    // 新增：测试改进后的 sanitizeHtml 正则表达式
    @Test
    void testSanitizeHtml_OnEventWithSlash_RemovesEvent() {
        // 测试使用 / 分隔符的事件处理器
        String input = "<img/onerror=alert(1) src=x>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("onerror"));
    }

    @Test
    void testSanitizeHtml_OnEventWithTab_RemovesEvent() {
        // 测试使用制表符分隔的事件处理器
        String input = "<img\tonerror=alert(1) src=x>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertFalse(sanitized.toLowerCase().contains("onerror"));
    }

    // 二次安全检查测试
    @Test
    void testSanitizeHtml_ResidualScriptTag_GetsEscaped() {
        // 测试清理后仍有可疑内容时的安全回退
        String input = "<script>alert(1)</script>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        // script 标签被移除后应该不包含原始脚本
        assertFalse(sanitized.contains("alert(1)"));
    }

    @Test
    void testSanitizeHtml_ResidualIframeTag_GetsEscaped() {
        // 测试残留的 iframe 标签会被转义
        String input = "<iframe src=\"evil.com\">";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        // iframe 应该被转义
        assertTrue(sanitized.contains("&lt;iframe") || !sanitized.contains("<iframe"));
    }

    @Test
    void testSanitizeHtml_ResidualJavascriptProtocol_GetsEscaped() {
        // 测试残留的 javascript: 协议会被转义
        String input = "<a href=\"javascript:alert(1)\">Click</a>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        // javascript: 应该被移除或转义
        assertFalse(sanitized.toLowerCase().contains("javascript:") && sanitized.toLowerCase().contains("alert"));
    }

    @Test
    void testSanitizeHtml_SafeHtml_Preserved() {
        // 测试安全的 HTML 内容被保留
        String input = "<p>Hello <strong>World</strong></p>";
        String sanitized = XssUtils.sanitizeHtml(input);

        assertNotNull(sanitized);
        assertTrue(sanitized.contains("<p>"));
        assertTrue(sanitized.contains("Hello"));
        assertTrue(sanitized.contains("<strong>"));
        assertTrue(sanitized.contains("World"));
    }
}
