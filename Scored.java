package com.epam.healenium.treecomparing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class Scored<T> {

    private final double score;
    private final T value;
}
