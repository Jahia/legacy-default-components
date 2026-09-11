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
 * The views render every JCR property value through a bound variable, so the value reaches the
 * page through a tag that encodes it.
 */
public class JcrPropertyOutputTest {

    private static final Pattern NODE_PROPERTY_TAG =
            Pattern.compile("<jcr:nodeProperty\\b[^>]*?/?>", Pattern.DOTALL);
    private static final Pattern VAR_ATTRIBUTE = Pattern.compile("\\bvar\\s*=");

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
