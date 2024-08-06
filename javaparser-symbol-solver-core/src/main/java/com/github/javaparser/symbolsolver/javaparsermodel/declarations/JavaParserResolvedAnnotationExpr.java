package com.github.javaparser.symbolsolver.javaparsermodel.declarations;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationExpr;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.Optional;

public class JavaParserResolvedAnnotationExpr implements ResolvedAnnotationExpr {
    private final AnnotationExpr wrappedNode;
    private final TypeSolver typeSolver;

    public JavaParserResolvedAnnotationExpr(AnnotationExpr wrappedNode, TypeSolver typeSolver) {
        this.wrappedNode = wrappedNode;
        this.typeSolver = typeSolver;
    }

    @Override
    public Object getValue(String name) {
        if (wrappedNode.isMarkerAnnotationExpr()) {
            return null;
        }

        if (wrappedNode.isSingleMemberAnnotationExpr()) {
            final SingleMemberAnnotationExpr smae = wrappedNode.asSingleMemberAnnotationExpr();
            final Expression expression = smae.getMemberValue();
            if (name.equals("value")) {
                return getValue(expression);
            } else {
                return null;
            }
        }

        if (wrappedNode.isNormalAnnotationExpr()) {
            final NormalAnnotationExpr nae = wrappedNode.asNormalAnnotationExpr();
            final NodeList<MemberValuePair> pairs = nae.getPairs();
            final Optional<MemberValuePair> pair = pairs.stream().filter(mvp -> mvp.getNameAsString().equals(name)).findFirst();
            if (pair.isPresent()) {
                return getValue(pair.get().getValue());
            } else {
                return null;
            }
        }
        throw new UnsupportedAnnotationExprType(wrappedNode.getClass().getCanonicalName());
    }

    @Override
    public String getName() {
        final Optional<ResolvedAnnotationDeclaration> optional = JavaParserFacade.get(typeSolver).solve(wrappedNode).getDeclaration();
        if (optional.isPresent()) {
            return optional.get().getQualifiedName();
        } else {
            return wrappedNode.getName().getIdentifier();
        }
    }

    private Object getValue(Expression expression) {
        if (expression.isBooleanLiteralExpr()) {
            return expression.asBooleanLiteralExpr().getValue();
        }
        if (expression.isCharLiteralExpr()) {
            return expression.asCharLiteralExpr().getValue();
        }
        if (expression.isDoubleLiteralExpr()) {
            return expression.asDoubleLiteralExpr().getValue();
        }
        if (expression.isIntegerLiteralExpr()) {
            return expression.asIntegerLiteralExpr().getValue();
        }
        if (expression.isLiteralStringValueExpr()) {
            return expression.asLiteralStringValueExpr().getValue();
        }
        if (expression.isLongLiteralExpr()) {
            return expression.asLongLiteralExpr().getValue();
        }
        if (expression.isNullLiteralExpr()) {
            return null;
        }
        if (expression.isStringLiteralExpr()) {
            return expression.asStringLiteralExpr().getValue();
        }
        if (expression.isTextBlockLiteralExpr()) {
            return expression.asTextBlockLiteralExpr().getValue();
        }
        if (expression.isCastExpr()) {
            return expression.asClassExpr().getType().resolve();
        }
        if (expression.isArrayInitializerExpr()) {
            final NodeList<Expression> values = expression.asArrayInitializerExpr().getValues();
            Object o[] = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                o[i] = getValue(values.get(i));
            }
            return o;
        }
        return expression;
    }
}