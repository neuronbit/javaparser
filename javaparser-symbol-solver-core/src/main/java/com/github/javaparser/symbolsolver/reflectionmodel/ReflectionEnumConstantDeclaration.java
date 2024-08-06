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

package com.github.javaparser.symbolsolver.reflectionmodel;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedEnumConstantDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionEnumConstantDeclaration implements ResolvedEnumConstantDeclaration {

    private Field enumConstant;
    private TypeSolver typeSolver;

    public ReflectionEnumConstantDeclaration(Field enumConstant, TypeSolver typeSolver) {
        if (!enumConstant.isEnumConstant()) {
            throw new IllegalArgumentException("The given field does not represent an enum constant");
        }
        this.enumConstant = enumConstant;
        this.typeSolver = typeSolver;
    }

    @Override
    public String getName() {
        return enumConstant.getName();
    }

    @Override
    public ResolvedType getType() {
        Class<?> enumClass = enumConstant.getDeclaringClass();
        ResolvedReferenceTypeDeclaration typeDeclaration = new ReflectionEnumDeclaration(enumClass, typeSolver);
        return new ReferenceTypeImpl(typeDeclaration);
    }

    public Optional<List<ResolvedAnnotationExpr>> getAnnotation(String typeName) {
        if (typeName.contains(".")) {
            String simpleName = typeName.substring(typeName.lastIndexOf('.') + 1);
            return Optional.of(Arrays.stream(this.enumConstant.getAnnotations())
                    .filter(a -> a.annotationType().getName().equals(simpleName) || a.annotationType().getCanonicalName().equals(typeName))
                    .map(ReflectionResolvedAnnotationExpr::new)
                    .collect(Collectors.toList()));
        } else {
            return Optional.of(Arrays.stream(this.enumConstant.getAnnotations())
                    .filter(a -> a.annotationType().getName().equals(typeName))
                    .map(ReflectionResolvedAnnotationExpr::new)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public Optional<Javadoc> getJavadoc() {
        return Optional.empty();
    }
}
