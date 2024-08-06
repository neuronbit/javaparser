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

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;
import javassist.CtField;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Federico Tomassetti
 */
public class JavassistEnumConstantDeclaration implements ResolvedEnumConstantDeclaration {

    private CtField ctField;
    private TypeSolver typeSolver;
    private ResolvedType type;

    public JavassistEnumConstantDeclaration(CtField ctField, TypeSolver typeSolver) {
        if (ctField == null) {
            throw new IllegalArgumentException();
        }
        if ((ctField.getFieldInfo2().getAccessFlags() & AccessFlag.ENUM) == 0) {
            throw new IllegalArgumentException(
                    "Trying to instantiate a JavassistEnumConstantDeclaration with something which is not an enum field: "
                            + ctField.toString());
        }
        this.ctField = ctField;
        this.typeSolver = typeSolver;
    }


    @Override
    public String getName() {
        return ctField.getName();
    }

    @Override
    public ResolvedType getType() {
        if (type == null) {
            type = new ReferenceTypeImpl(new JavassistEnumDeclaration(ctField.getDeclaringClass(), typeSolver));
        }
        return type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "ctField=" + ctField.getName() +
                ", typeSolver=" + typeSolver +
                '}';
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
    public Optional<Javadoc> getJavadoc() {
        return Optional.empty();
    }

}
