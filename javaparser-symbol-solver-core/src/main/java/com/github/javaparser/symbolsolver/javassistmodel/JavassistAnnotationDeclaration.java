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
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.logic.AbstractTypeDeclaration;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import java.lang.annotation.Inherited;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Malte Skoruppa
 */
public class JavassistAnnotationDeclaration extends AbstractTypeDeclaration implements ResolvedAnnotationDeclaration {

    private CtClass ctClass;
    private TypeSolver typeSolver;
    private JavassistTypeDeclarationAdapter javassistTypeDeclarationAdapter;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "ctClass=" + ctClass.getName() +
                ", typeSolver=" + typeSolver +
                '}';
    }

    public JavassistAnnotationDeclaration(CtClass ctClass, TypeSolver typeSolver) {
        if (!ctClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation: " + ctClass.getName());
        }
        this.ctClass = ctClass;
        this.typeSolver = typeSolver;
        this.javassistTypeDeclarationAdapter = new JavassistTypeDeclarationAdapter(ctClass, typeSolver, this);
    }

    @Override
    public String getPackageName() {
        return ctClass.getPackageName();
    }

    @Override
    public String getClassName() {
        String qualifiedName = getQualifiedName();
        if (qualifiedName.contains(".")) {
            return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1, qualifiedName.length());
        }
        return qualifiedName;
    }

    @Override
    public String getQualifiedName() {
        return ctClass.getName().replace('$', '.');
    }

    @Override
    public boolean isAssignableBy(ResolvedType type) {
        // TODO #1836
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ResolvedFieldDeclaration> getAllFields() {
        return javassistTypeDeclarationAdapter.getDeclaredFields();
    }

    @Override
    public boolean isAssignableBy(ResolvedReferenceTypeDeclaration other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ResolvedReferenceType> getAncestors(boolean acceptIncompleteList) {
        return javassistTypeDeclarationAdapter.getAncestors(acceptIncompleteList);
    }

    @Override
    public Set<ResolvedReferenceTypeDeclaration> internalTypes() {
        return javassistTypeDeclarationAdapter.internalTypes();
    }

    @Override
    public Set<ResolvedMethodDeclaration> getDeclaredMethods() {
        // TODO #1838
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDirectlyAnnotation(String canonicalName) {
        return ctClass.hasAnnotation(canonicalName);
    }

    @Override
    public String getName() {
        return getClassName();
    }

    /**
     * Annotation declarations cannot have type parameters and hence this method always returns an empty list.
     *
     * @return An empty list.
     */
    @Override
    public List<ResolvedTypeParameterDeclaration> getTypeParameters() {
        // Annotation declarations cannot have type parameters - i.e. we can always return an empty list.
        return Collections.emptyList();
    }

    @Override
    public Optional<ResolvedReferenceTypeDeclaration> containerType() {
        // TODO #1841
        throw new UnsupportedOperationException("containerType() is not supported for " + this.getClass().getCanonicalName());
    }

    @Override
    public List<ResolvedConstructorDeclaration> getConstructors() {
        return Collections.emptyList();
    }

    @Override
    public List<ResolvedAnnotationMemberDeclaration> getAnnotationMembers() {
        return Stream.of(ctClass.getDeclaredMethods())
                .map(m -> new JavassistAnnotationMemberDeclaration(m, typeSolver))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInheritable() {
        try {
            return ctClass.getAnnotation(Inherited.class) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Optional<Javadoc> getJavadoc() {
        return Optional.empty();
    }

    @Override
    public List<ResolvedAnnotationExpr> getAnnotations() {
        List<ResolvedAnnotationExpr> result = new ArrayList<>(3);
        AnnotationsAttribute visibleAnnoAttr = (AnnotationsAttribute) ctClass.getClassFile2().getAttribute(AnnotationsAttribute.visibleTag);
        if (null != visibleAnnoAttr) {
            Annotation[] an = visibleAnnoAttr.getAnnotations();
            if (null != an) {
                for (Annotation annotation : an) {
                    result.add(new JavassistResolvedAnnotationExpr(annotation));
                }
            }
        }
        AnnotationsAttribute inVisibleAnnoAttr = (AnnotationsAttribute) ctClass.getClassFile2().getAttribute(AnnotationsAttribute.invisibleTag);
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

    @Override
    public Optional<List<ResolvedAnnotationExpr>> getAnnotation(String typeName) {
        List<ResolvedAnnotationExpr> result = new ArrayList<>(3);
        ClassFile classFile2 = ctClass.getClassFile2();
        AnnotationsAttribute visibleAttr = (AnnotationsAttribute) classFile2.getAttribute(AnnotationsAttribute.visibleTag);
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
        AnnotationsAttribute inVisibleAttr = (AnnotationsAttribute) classFile2.getAttribute(AnnotationsAttribute.invisibleTag);
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
    public boolean setJavadoc(Javadoc javadoc) {
        return false;
    }
}
