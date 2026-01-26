package com.example.SpringMate.Shared.Helper;

import com.google.common.html.HtmlEscapers;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.util.StringUtils;

/**
 * Centralized input/output sanitization utility.
 */
public final class InputSanitizer {

    private InputSanitizer() {
    }

    /**
     * Policy for strictly plain text.
     */
    private static final PolicyFactory PLAIN_TEXT_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    /**
     * safe rich text (descriptions, content).
     * Includes common formatting but strips dangerous script-related tags.
     */
    private static final PolicyFactory SAFE_HTML_POLICY = new HtmlPolicyBuilder()
            .allowCommonInlineFormattingElements() // b, i, em, strong, etc.
            .allowCommonBlockElements()           // p, div, h1-h6
            .allowElements("a", "ul", "ol", "li", "br")
            .allowAttributes("href").onElements("a")
            .allowStandardUrlProtocols() // http, https, mailto
            .requireRelNofollowOnLinks()
            .disallowElements("style", "noscript", "script", "iframe", "object")
            .toFactory();

    /**
     * For names, titles, and search text.
     * Removes all HTML tags and then escapes special characters.
     */
    public static String sanitizePlainText(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        // First strip any attempted tags
        String stripped = PLAIN_TEXT_POLICY.sanitize(input);
        // Then escape any leftover special characters (like <, >, &)
        return HtmlEscapers.htmlEscaper().escape(stripped).trim();
    }

    /**
     * For rich content fields (e.g., blog posts, descriptions).
     * Allows safe HTML tags and attributes while stripping dangerous ones.
     */
    public static String sanitizeHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return html;
        }
        return SAFE_HTML_POLICY.sanitize(html).trim();
    }

    /**
     * Escape characters for safe rendering in HTML.
     * Use this when you want to display raw user input as literal text.
     */
    public static String escapeForHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        return HtmlEscapers.htmlEscaper().escape(input);
    }
}