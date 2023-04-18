package com.epam.healenium.treecomparing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsoupHTMLParser implements DocumentParser {

    @Override
    public Node parse(InputStream inputStream) {
        try {
            Document document = Jsoup.parse(inputStream, "UTF-8", "/").normalise();
            org.jsoup.nodes.Node html = findHtml(Collections.singletonList(document.root()));
            int startIndex = 0;
            Deque<NodeBuilder> treeDepth = new ArrayDeque<>();
            return traverse(html, startIndex, treeDepth);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Walkthrough entire DOM document with building full path, each node will be converted to the inner type
     * @param node - document
     * @param index - start index
     * @param treeDepth - resulting path
     * @return
     */
    private Node traverse(org.jsoup.nodes.Node node, Integer index, Deque<NodeBuilder> treeDepth) {
        Map<String, String> attributesMap = node.attributes().asList()
            .stream()
            .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));

        NodeBuilder builder = new NodeBuilder()
            .setTag(node.nodeName())
            .setIndex(index)
            .setAttributes(attributesMap);

        treeDepth.push(builder);
        int indexCounter = 0;

        for (org.jsoup.nodes.Node child : node.childNodes()) {
            if (child instanceof Element) {
                builder.addChild(traverse(child, indexCounter++, treeDepth));
            } else if (child instanceof TextNode) {
                String text = ((TextNode) child).text();
                for (NodeBuilder parentBuilder: treeDepth) {
                    parentBuilder.addContent(text);
                }
            }
        }

        treeDepth.pop();
        return builder.build();
    }

    /**
     * Search HTML node in given node collection and it's children
     * @param nodes
     * @return
     */
    private org.jsoup.nodes.Node findHtml(List<org.jsoup.nodes.Node> nodes) {
        for (org.jsoup.nodes.Node node : nodes) {
            if (node.nodeName().equals("html")) {
                return node;
            }
        }
        List<org.jsoup.nodes.Node> newNodes = new LinkedList<>();
        for (org.jsoup.nodes.Node node : nodes) {
            newNodes.addAll(node.childNodes());
        }
        return findHtml(newNodes);
    }
}
