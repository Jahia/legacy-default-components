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
import java.util.Collections;
import java.util.List;
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
    private static final Pattern ACTION_TAG_OPENING = Pattern.compile("<\\w+:\\w+\\b");

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
            String source = read(view);
            Matcher expression = USER_NAME_EXPRESSION.matcher(source);
            while (expression.find()) {
                expressions++;
                if (!isEncoded(source, expression.start())) {
                    unencoded.add(root.relativize(view) + " -> " + expression.group());
                }
            }
        }

        assertFalse("no views were scanned under " + root, views.isEmpty());
        assertTrue("no expression reading a user name property was scanned", expressions > 0);
        assertEquals("every user name a view prints is expected to go through an encoder",
                Collections.emptyList(), unencoded);
    }

    // An expression inside a JSP action tag, such as <c:out value="..."/> or <c:set>, reaches the
    // page only through that tag. One in template text or in an HTML tag is printed as it stands.
    private static boolean isEncoded(String source, int start) {
        if (ENCODED_EXPRESSION.matcher(source).region(start, source.length()).lookingAt()) {
            return true;
        }
        int tagStart = source.lastIndexOf('<', start);
        boolean insideTag = tagStart > source.lastIndexOf('>', start);
        return insideTag && ACTION_TAG_OPENING.matcher(source).region(tagStart, start).lookingAt();
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
