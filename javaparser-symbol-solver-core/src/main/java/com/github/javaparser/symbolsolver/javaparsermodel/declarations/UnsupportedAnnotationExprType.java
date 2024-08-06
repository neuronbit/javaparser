package com.github.javaparser.symbolsolver.javaparsermodel.declarations;

public class UnsupportedAnnotationExprType extends RuntimeException {
    public UnsupportedAnnotationExprType(String typeName) {
        super(typeName);
    }
}
