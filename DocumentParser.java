package com.epam.healenium.treecomparing;

import java.io.InputStream;

public interface DocumentParser {

    Node parse(InputStream inputStream);

}
