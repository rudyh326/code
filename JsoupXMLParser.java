package com.epam.healenium.treecomparing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

public class JsoupXMLParser implements DocumentParser {

  @Override
  public Node parse(InputStream inputStream) {
    try {
      Document document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.toString(), "/", Parser.xmlParser()).normalise();
      org.jsoup.nodes.Node xml = findRoot(Collections.singletonList(document.root()));
      int startIndex = 0;
      Deque<NodeBuilder> treeDepth = new ArrayDeque<>();
      return traverse(xml, startIndex, treeDepth);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

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
        if (!" ".equals(text)) {
          for (NodeBuilder parentBuilder : treeDepth) {
            parentBuilder.addContent(text);
          }
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
  private org.jsoup.nodes.Node findRoot(List<org.jsoup.nodes.Node> nodes) {
    for (org.jsoup.nodes.Node node : nodes) {
      if (node.nodeName().equals("hierarchy")) {
        return node;
      }
    }
    List<org.jsoup.nodes.Node> newNodes = new LinkedList<>();
    for (org.jsoup.nodes.Node node : nodes) {
      newNodes.addAll(node.childNodes());
    }
    return findRoot(newNodes);
  }

}
