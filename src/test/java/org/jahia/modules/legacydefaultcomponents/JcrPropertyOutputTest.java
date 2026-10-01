package org.jahia.modules.legacydefaultcomponents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * The views render JCR property values through a tag or a function that encodes them.
 */
public class JcrPropertyOutputTest {

    private static final Pattern NODE_PROPERTY_TAG =
            Pattern.compile("<jcr:nodeProperty\\b[^>]*?/?>", Pattern.DOTALL);
    private static final Pattern VAR_ATTRIBUTE = Pattern.compile("\\bvar\\s*=");
    private static final Pattern USER_NAME_EXPRESSION =
            Pattern.compile("\\$\\{[^}]*'(?:jcr:createdBy|jcr:lastModifiedBy|j:lastPublishedBy)'[^}]*}");
    private static final Pattern ENCODED_EXPRESSION = Pattern.compile("\\$\\{\\s*fn:escapeXml\\(");
    private static final Pattern TAGLIB_PREFIX = Pattern.compile("<%@\\s*taglib\\b[^>]*?\\bprefix\\s*=\\s*\"(\\w+)\"");
    private static final Pattern JSP_COMMENT = Pattern.compile("<%--.*?--%>", Pattern.DOTALL);
    private static final Pattern ACTION_TAG_NAME = Pattern.compile("<(\\w+):(\\w+)\\b");
    private static final Pattern ESCAPE_XML_OFF = Pattern.compile("\\bescapeXml\\s*=\\s*\"false\"");
    // These tags hand a value on without encoding it: c:set stores it for a later print, and
    // fmt:param is substituted into the message as it stands. An expression that calls a function
    // hands on what the function returns, such as the user path authorDisplay.jsp looks up.
    private static final Set<String> PASS_THROUGH_TAGS = new HashSet<>(Arrays.asList("c:set", "fmt:param"));
    private static final Pattern FUNCTION_CALL = Pattern.compile("\\b\\w+:\\w+\\s*\\(");

    @Test
    public void everyNodePropertyTagBindsItsValueToAVariable() throws Exception {
        Path root = viewRoot();
        List<Path> views = views(root);
        List<String> unbound = new ArrayList<>();
        int tags = 0;

        for (Path view : views) {
            Matcher tag = NODE_PROPERTY_TAG.matcher(read(view));
            while (tag.find()) {
                tags++;
                if (!VAR_ATTRIBUTE.matcher(tag.group()).find()) {
                    unbound.add(root.relativize(view) + " -> " + tag.group().replaceAll("\\s+", " "));
                }
            }
        }

        // A scan that reaches nothing reports the same "clean" as one that passes, so the
        // assertions below only mean something once both counts are non-zero.
        assertFalse("no views were scanned under " + root, views.isEmpty());
        assertTrue("no <jcr:nodeProperty> tags were scanned", tags > 0);
        assertEquals("every <jcr:nodeProperty> is expected to carry a var attribute",
                Collections.emptyList(), unbound);
    }

    @Test
    public void everyUserNameIsEncodedWhereTheViewPrintsIt() throws Exception {
        Path root = viewRoot();
        List<Path> views = views(root);
        List<String> unencoded = new ArrayList<>();
        int expressions = 0;

        for (Path view : views) {
            String source = JSP_COMMENT.matcher(read(view)).replaceAll("");
            Set<String> prefixes = taglibPrefixes(source);
            Matcher expression = USER_NAME_EXPRESSION.matcher(source);
            while (expression.find()) {
                expressions++;
                if (!isEncoded(source, expression.group(), expression.start(), prefixes)) {
                    unencoded.add(root.relativize(view) + " -> " + expression.group());
                }
            }
        }

        assertFalse("no views were scanned under " + root, views.isEmpty());
        assertTrue("no expression reading a user name property was scanned", expressions > 0);
        assertEquals("every user name a view prints is expected to go through an encoder",
                Collections.emptyList(), unencoded);
    }

    // An expression inside the attribute of a JSP action tag, such as <c:out value="..."/>,
    // reaches the page through that tag, which encodes it. One in template text, in an HTML tag,
    // in a pass-through tag or in a tag with escapeXml="false" is printed as it stands.
    private static boolean isEncoded(String source, String expression, int start, Set<String> prefixes) {
        if (ENCODED_EXPRESSION.matcher(source).region(start, source.length()).lookingAt()) {
            return true;
        }
        int tagStart = source.lastIndexOf('<', start);
        if (tagStart < 0 || tagStart < source.lastIndexOf('>', start)) {
            return false;
        }
        Matcher tag = ACTION_TAG_NAME.matcher(source).region(tagStart, start);
        if (!tag.lookingAt() || !prefixes.contains(tag.group(1))) {
            return false;
        }
        int tagEnd = source.indexOf('>', start);
        String tagText = source.substring(tagStart, tagEnd < 0 ? source.length() : tagEnd);
        if (ESCAPE_XML_OFF.matcher(tagText).find()) {
            return false;
        }
        return !PASS_THROUGH_TAGS.contains(tag.group(1) + ":" + tag.group(2))
                || FUNCTION_CALL.matcher(expression).find();
    }

    private static Set<String> taglibPrefixes(String source) {
        Set<String> prefixes = new HashSet<>();
        Matcher prefix = TAGLIB_PREFIX.matcher(source);
        while (prefix.find()) {
            prefixes.add(prefix.group(1));
        }
        return prefixes;
    }

    private static Path viewRoot() throws URISyntaxException {
        Path codeSource = Paths.get(JcrPropertyOutputTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        Path packaged = codeSource.resolveSibling("classes");
        return Files.isDirectory(packaged) ? packaged : Paths.get("src", "main", "resources");
    }

    private static List<Path> views(Path root) throws IOException {
        try (Stream<Path> tree = Files.walk(root)) {
            return tree.filter(JcrPropertyOutputTest::isView).sorted().collect(Collectors.toList());
        }
    }

    private static boolean isView(Path path) {
        String name = path.getFileName().toString();
        return Files.isRegularFile(path) && (name.endsWith(".jsp") || name.endsWith(".jspf"));
    }

    // ISO-8859-1 maps every byte to a char, so a view in any encoding is readable and the
    // ASCII-only patterns above still match.
    private static String read(Path view) throws IOException {
        return new String(Files.readAllBytes(view), StandardCharsets.ISO_8859_1);
    }
}
