package com.github.javaparser.resolution.declarations;

public interface ResolvedAnnotationExpr {
    Object getValue(String name);

    String getName();
}
