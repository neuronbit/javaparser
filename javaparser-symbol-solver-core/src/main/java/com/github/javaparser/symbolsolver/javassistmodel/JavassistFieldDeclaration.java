/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2024 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.symbolsolver.javassistmodel;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Federico Tomassetti
 */
public class JavassistFieldDeclaration implements ResolvedFieldDeclaration {
    private CtField ctField;
    private TypeSolver typeSolver;

    public JavassistFieldDeclaration(CtField ctField, TypeSolver typeSolver) {
        this.ctField = ctField;
        this.typeSolver = typeSolver;
    }

    @Override
    public ResolvedType getType() {
        try {
            String signature = ctField.getGenericSignature();
            if (signature == null) {
                signature = ctField.getSignature();
            }
            SignatureAttribute.Type genericSignatureType = SignatureAttribute.toTypeSignature(signature);
            return JavassistUtils.signatureTypeToType(genericSignatureType, typeSolver, (ResolvedTypeParametrizable) declaringType());
        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(ctField.getModifiers());
    }
    
    @Override
    public boolean isVolatile() {
        return Modifier.isVolatile(ctField.getModifiers());
    }

    @Override
    public String getName() {
        return ctField.getName();
    }

    @Override
    public boolean isField() {
        return true;
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public AccessSpecifier accessSpecifier() {
        return JavassistFactory.modifiersToAccessLevel(ctField.getModifiers());
    }

    @Override
    public ResolvedTypeDeclaration declaringType() {
        return JavassistFactory.toTypeDeclaration(ctField.getDeclaringClass(), typeSolver);
    }

    @Override
    public boolean isTransient() {
        return Modifier.isTransient(ctField.getModifiers());
    }

    @Override
    public boolean isEnumConstant() {
        return ctField.getDeclaringClass().isEnum();
    }

    @Override
    public Optional<List<ResolvedAnnotationExpr>> getAnnotation(String typeName) {
        List<ResolvedAnnotationExpr> result = new ArrayList<>(3);
        FieldInfo minfo = ctField.getFieldInfo();
        AnnotationsAttribute visibleAttr = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);
        if (null != visibleAttr) {
            Annotation[] an = visibleAttr.getAnnotations();
            if (null != an) {
                for (Annotation annotation : an) {
                    if (ResolvedDeclaration.isMatch(typeName, annotation.getTypeName())) {
                        result.add(new JavassistResolvedAnnotationExpr(annotation));
                    }
                }
            }
        }
        AnnotationsAttribute inVisibleAttr = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.invisibleTag);
        if (null != inVisibleAttr) {
            Annotation[] an = inVisibleAttr.getAnnotations();
            if (null != an) {
                for (Annotation annotation : an) {
                    if (ResolvedDeclaration.isMatch(typeName, annotation.getTypeName())) {
                        result.add(new JavassistResolvedAnnotationExpr(annotation));
                    }
                }
            }
        }
        return Optional.of(result);
    }

    @Override
    public Object getInitialValue() {
        //TODO spearwang 2024/5/27: check
        return ctField.getFieldInfo();
    }

    @Override
    public Optional<Javadoc> getJavadoc() {
        return Optional.empty();
    }

    @Override
    public List<ResolvedAnnotationExpr> getAnnotations() {
        List<ResolvedAnnotationExpr> result = new ArrayList<>(3);
        FieldInfo finfo = ctField.getFieldInfo();
        AnnotationsAttribute visibleAnnoAttr = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.visibleTag);
        if (null != visibleAnnoAttr) {
            Annotation[] an = visibleAnnoAttr.getAnnotations();
            if (null != an) {
                for (Annotation annotation : an) {
                    result.add(new JavassistResolvedAnnotationExpr(annotation));
                }
            }
        }
        AnnotationsAttribute inVisibleAnnoAttr = (AnnotationsAttribute) finfo.getAttribute(AnnotationsAttribute.invisibleTag);
        if (null != inVisibleAnnoAttr) {
            Annotation[] an = inVisibleAnnoAttr.getAnnotations();
            if (null != an) {
                for (Annotation annotation : an) {
                    result.add(new JavassistResolvedAnnotationExpr(annotation));
                }
            }
        }
        return result;
    }
}
