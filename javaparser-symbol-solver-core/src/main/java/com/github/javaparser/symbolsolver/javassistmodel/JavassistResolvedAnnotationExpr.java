package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.resolution.declarations.ResolvedAnnotationExpr;
import javassist.bytecode.annotation.*;

import java.util.Arrays;

public class JavassistResolvedAnnotationExpr implements ResolvedAnnotationExpr {
    private final Annotation wrappedNode;

    public JavassistResolvedAnnotationExpr(Annotation wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    @Override
    public Object getValue(String name) {
        final MemberValue memberValue = wrappedNode.getMemberValue(name);
        return getValue(memberValue);
    }

    private static Object getValue(MemberValue memberValue) {
        if (null == memberValue) {
            return null;
        }
        if (memberValue instanceof StringMemberValue) {
            return ((StringMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof ArrayMemberValue) {
            final MemberValue[] value = ((ArrayMemberValue) memberValue).getValue();
            return Arrays.stream(value).map(JavassistResolvedAnnotationExpr::getValue).toArray();
        }
        if (memberValue instanceof BooleanMemberValue) {
            return ((BooleanMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof ByteMemberValue) {
            return ((ByteMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof CharMemberValue) {
            return ((CharMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof ClassMemberValue) {
            return ((ClassMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof DoubleMemberValue) {
            return ((DoubleMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof EnumMemberValue) {
            return ((EnumMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof FloatMemberValue) {
            return ((FloatMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof IntegerMemberValue) {
            return ((IntegerMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof LongMemberValue) {
            return ((LongMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof ShortMemberValue) {
            return ((ShortMemberValue) memberValue).getValue();
        }
        if (memberValue instanceof AnnotationMemberValue) {
            return ((AnnotationMemberValue) memberValue).getValue();
        }
        return null;
    }

    @Override
    public String getName() {
        return wrappedNode.getTypeName();
    }
}
