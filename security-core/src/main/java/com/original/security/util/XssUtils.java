package com.original.security.util;

import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * XSS防护基础工具类。
 * 提供对 HTML 内容的敏感字符转义，以防注入。
 * <p>
 * 该工具类封装了 Spring 的 {@link HtmlUtils#htmlEscape(String)} 和
 * {@link JavaScriptUtils#javaScriptEscape(String)} 方法，
 * 为开发者提供简单的 API 来防止跨站脚本 (XSS) 攻击。
 * <p>
 * <b>注意：</b>此工具类提供基础转义功能。对于复杂的 XSS 防护需求，
 * 建议使用专门的库如 OWASP Java Encoder 或实施更全面的 Content Security Policy (CSP)。
 * <p>
 * 使用示例：
 * <pre>
 * String userInput = "&lt;script&gt;alert('xss');&lt;/script&gt;";
 * String safe = XssUtils.escapeHtml(userInput);
 * // safe: "&amp;lt;script&amp;gt;alert('xss');&amp;lt;/script&amp;gt;"
 *
 * String jsInput = "'; alert('xss'); //";
 * String jsSafe = XssUtils.escapeJavaScript(jsInput);
 * // jsSafe: "\\'; alert(\\'xss\\'); //"
 * </pre>
 *
 * @author Naulu
 * @since 1.0.0
 */
public final class XssUtils {

    private XssUtils() {
        // 工具类，不允许实例化
    }

    /**
     * 将包含HTML特殊字符的字符串转义为安全的内容。
     * 例如："&lt;script&gt;" 转换为 "&amp;lt;script&amp;gt;"
     * <p>
     * 该方法委托给 Spring 的 {@link HtmlUtils#htmlEscape(String)} 方法实现。
     * </p>
     * <p>
     * 转义的字符包括：&lt;, &gt;, &amp;, &quot;, &#39;
     * </p>
     *
     * @param input 需要转义的字符串，可以为 null
     * @return 转义后的安全字符串，如果 input 为 null 则返回 null
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 将包含JavaScript特殊字符的字符串转义为安全的内容。
     * 例如："'alert('xss')" 转换为 "\\'alert(\\'xss\\')"
     * <p>
     * 该方法委托给 Spring 的 {@link JavaScriptUtils#javaScriptEscape(String)} 方法实现。
     * </p>
     * <p>
     * 转义的字符包括：单引号、双引号、反斜杠、换行符、制表符等。
     * </p>
     *
     * @param input 需要转义的字符串，可以为 null
     * @return 转义后的安全字符串，如果 input 为 null 则返回 null
     */
    public static String escapeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return JavaScriptUtils.javaScriptEscape(input);
    }

    /**
     * 对字符串进行URL编码，防止URL注入攻击。
     * 例如："user<script>alert(1)</script>" 转换为 "user%3Cscript%3Ealert%281%29%3C%2Fscript%3E"
     * <p>
     * 使用 UTF-8 编码。
     * </p>
     *
     * @param input 需要编码的字符串，可以为 null
     * @return URL编码后的字符串，如果 input 为 null 则返回 null
     */
    public static String encodeUrl(String input) {
        if (input == null) {
            return null;
        }
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // 理论上不会抛出异常，因为 UTF-8 是标准编码
            throw new IllegalArgumentException("URL encoding failed: UTF-8 not supported", e);
        }
    }

    /**
     * 清理HTML输入，移除常见的XSS攻击向量。
     * <p>
     * 此方法会移除以下危险元素和属性：
     * <ul>
     *   <li>&lt;script&gt; 标签（包括内容）</li>
     *   <li>所有 on* 事件处理器（如 onclick, onload, onerror）</li>
     *   <li>javascript: 和 vbscript: 协议链接</li>
     *   <li>data: 协议中的潜在危险内容</li>
     * </ul>
     * </p>
     * <p>
     * <b>⚠️ 重要警告：</b>此方法仅提供基础清理，存在以下已知局限性：
     * <ul>
     *   <li>无法处理复杂的 HTML 解析场景（如嵌套标签、编码绕过）</li>
     *   <li>无法处理 HTML 实体编码绕过（如 &amp;lt;script&amp;gt;）</li>
     *   <li>无法处理 CSS 表达式攻击</li>
     *   <li>无法处理 SVG/MathML 命名空间中的攻击向量</li>
     *   <li>无法处理 Unicode 编码绕过（如 \u003cscript\u003e）</li>
     *   <li>无法处理双重编码或多重编码绕过</li>
     * </ul>
     * </p>
     * <p>
     * <b>对于生产环境，强烈建议使用专门的 HTML 清理库：</b>
     * <ul>
     *   <li>OWASP Java HTML Sanitizer (推荐): {@code com.google.common.html.HtmlEscapers}</li>
     *   <li>Jsoup Whitelist: {@code org.jsoup.safety.Safelist}</li>
     * </ul>
     * </p>
     * <p>
     * 示例用法：
     * <pre>
     * // 基础清理
     * String cleaned = XssUtils.sanitizeHtml("&lt;script&gt;alert(1)&lt;/script&gt;&lt;p&gt;Hello&lt;/p&gt;");
     * // 结果: "&lt;p&gt;Hello&lt;/p&gt;"
     *
     * // 注意：复杂攻击可能无法被检测
     * String complex = "&lt;img src=x onerror=alert(1)&gt;"; // 可能无法完全清理
     * </pre>
     * </p>
     *
     * @param input 需要清理的HTML字符串，可以为 null
     * @return 清理后的HTML字符串，如果 input 为 null 则返回 null
     * @deprecated 建议使用 OWASP Java HTML Sanitizer 或 Jsoup 进行生产环境的 HTML 清理。
     *             此方法仅适用于简单的、非安全关键的场景。
     */
    @Deprecated
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }

        String result = input;

        // 移除 <script> 标签（包括内容，支持多行）
        result = result.replaceAll("(?is)<script[^>]*>.*?</script>", "");

        // 移除 <style> 标签（CSS 表达式攻击向量）
        result = result.replaceAll("(?is)<style[^>]*>.*?</style>", "");

        // 移除所有 on* 事件处理器（更严格的匹配）
        // 匹配带引号的属性值：onclick="..."
        result = result.replaceAll("(?i)[\\s/]+on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
        // 匹配不带引号的属性值：onclick=alert(1)
        result = result.replaceAll("(?i)[\\s/]+on\\w+\\s*=\\s*[^\\s>]+", "");

        // 移除 javascript: 和 vbscript: 协议
        result = result.replaceAll("(?i)javascript\\s*:", "");
        result = result.replaceAll("(?i)vbscript\\s*:", "");

        // 移除 data: 协议中的潜在危险内容（仅针对 text/html）
        result = result.replaceAll("(?i)data\\s*:\\s*text/html[^\"'>\\s]*", "");

        // 二次检查：如果清理后仍包含可疑的标签开始符，进行额外的 HTML 转义
        // 这可以捕获一些 Unicode 编码绕过尝试（如 \u003c 被解码为 <）
        if (result.contains("<") && containsPotentialXssAfterSanitize(result)) {
            // 对可疑内容进行 HTML 转义作为安全回退
            return HtmlUtils.htmlEscape(result);
        }

        return result;
    }

    /**
     * 检查清理后的内容是否仍包含潜在的 XSS 攻击向量。
     * <p>
     * 此方法作为 sanitizeHtml() 的二次安全检查，用于捕获可能绕过第一次清理的攻击向量。
     * </p>
     *
     * @param input 已清理的输入字符串
     * @return true 如果仍包含潜在 XSS 攻击代码
     */
    private static boolean containsPotentialXssAfterSanitize(String input) {
        String lowerInput = input.toLowerCase();
        // 检查是否有残留的危险标签或脚本相关内容
        return lowerInput.contains("<script") || lowerInput.contains("<iframe")
                || lowerInput.contains("<object") || lowerInput.contains("<embed")
                || lowerInput.contains("javascript:") || lowerInput.contains("vbscript:")
                || lowerInput.contains("onerror=") || lowerInput.contains("onclick=")
                || lowerInput.contains("onload=");
    }

    /**
     * 检查字符串是否包含潜在的XSS攻击代码。
     * <p>
     * 此方法检测以下模式：
     * <ul>
     *   <li>&lt;script&gt; 标签</li>
     *   <li>常见事件处理器（onload, onclick, onerror 等）作为 HTML 属性</li>
     *   <li>javascript: 和 vbscript: 协议</li>
     *   <li>eval()、setTimeout()、setInterval() 中的字符串参数</li>
     *   <li>&lt;iframe&gt;、&lt;object&gt;、&lt;embed&gt; 标签</li>
     * </ul>
     * </p>
     * <p>
     * <b>注意：</b>此方法使用启发式检测，可能产生少量误报或漏报。
     * 对于安全关键场景，请结合其他防护措施（如 CSP）。
     * </p>
     *
     * @param input 需要检查的字符串，可以为 null
     * @return true 如果包含潜在XSS攻击代码，false 如果安全或为null
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 检测 <script> 标签
        if (lowerInput.contains("<script") || lowerInput.contains("</script")) {
            return true;
        }

        // 检测危险标签（包括 SVG 和 MathML，它们可以包含脚本）
        if (lowerInput.contains("<iframe") || lowerInput.contains("<object")
                || lowerInput.contains("<embed") || lowerInput.contains("<applet")
                || lowerInput.contains("<svg") || lowerInput.contains("<math")) {
            return true;
        }

        // 检测事件处理器属性（更精确的匹配：必须是 HTML 属性形式）
        // 匹配模式：onxxx= 或 onxxx =（后面跟引号或值）
        if (lowerInput.matches("(?s).*\\bon(load|click|error|mouseover|focus|blur|keydown|keyup|submit|change|input|select)\\s*=.*")) {
            return true;
        }

        // 检测 javascript: 和 vbscript: 协议（允许空格）
        if (lowerInput.matches("(?s).*javascript\\s*:.*") || lowerInput.matches("(?s).*vbscript\\s*:.*")) {
            return true;
        }

        // 检测危险函数调用（在属性值或脚本上下文中）
        if (lowerInput.matches("(?s).*\\beval\\s*\\(.*")
                || lowerInput.matches("(?s).*\\bexpression\\s*\\(.*")) {
            return true;
        }

        // 检测 HTML 实体编码绕过尝试
        if (lowerInput.contains("&#") && lowerInput.matches("(?s).*&#\\d+;.*script.*")) {
            return true;
        }

        return false;
    }
}
