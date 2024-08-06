package com.github.javaparser.symbolsolver.reflectionmodel;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationExpr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ReflectionResolvedAnnotationExpr implements ResolvedAnnotationExpr {
    private final Annotation wrappedNode;

    public ReflectionResolvedAnnotationExpr(Annotation wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    @Override
    public Object getValue(String name) {
        if (null == name || name.isEmpty()) {
            return null;
        }
        for (Method method : wrappedNode.getClass().getDeclaredMethods()) {
            String memberName = method.getName();
            if(memberName.equals(name)) {
                try {
                    return method.invoke(wrappedNode); // Might throw an exception if the method has arguments
                } catch (Exception e) {
                    // Handle exceptions gracefully (e.g., ignore methods with arguments)
                }
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return wrappedNode.annotationType().getCanonicalName();
    }
}
