/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2024 The JavaParser Team.
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
package com.github.javaparser.resolution.declarations;

/**
 * A generic declaration.
 *
 * @author Federico Tomassetti
 */
public interface ResolvedDeclaration extends AssociableToAST {

    /**
     * Anonymous classes do not have a name, for example.
     */
    default boolean hasName() {
        return true;
    }

    /**
     * Should return the name or return null if the name is not available.
     */
    String getName();

    /**
     * Does this declaration represents a class field?
     */
    default boolean isField() {
        return false;
    }

    /**
     * Does this declaration represents a variable?
     */
    default boolean isVariable() {
        return false;
    }

    /**
     * Does this declaration represents an enum constant?
     */
    default boolean isEnumConstant() {
        return false;
    }

    /**
     * Does this declaration represents a pattern declaration?
     */
    default boolean isTypePattern() {
        return false;
    }

    /**
     * Does this declaration represents a method parameter?
     */
    default boolean isParameter() {
        return false;
    }

    /**
     * Does this declaration represents a type?
     */
    default boolean isType() {
        return false;
    }

    /**
     * Does this declaration represents a method?
     * // FIXME: This is never overridden.
     */
    default boolean isMethod() {
        return false;
    }

    /**
     * Return this as a FieldDeclaration or throw an UnsupportedOperationException
     */
    default ResolvedFieldDeclaration asField() {
        throw new UnsupportedOperationException(String.format("%s is not a FieldDeclaration", this));
    }

    /**
     * Return this as a ParameterDeclaration or throw an UnsupportedOperationException
     */
    default ResolvedParameterDeclaration asParameter() {
        throw new UnsupportedOperationException(String.format("%s is not a ParameterDeclaration", this));
    }

    /**
     * Return this as a TypeDeclaration or throw an UnsupportedOperationException
     */
    default ResolvedTypeDeclaration asType() {
        throw new UnsupportedOperationException(String.format("%s is not a TypeDeclaration", this));
    }

    /**
     * Return this as a MethodDeclaration or throw an UnsupportedOperationException
     * // FIXME: This is never overridden.
     */
    default ResolvedMethodDeclaration asMethod() {
        throw new UnsupportedOperationException(String.format("%s is not a MethodDeclaration", this));
    }

    /**
     * Return this as a EnumConstantDeclaration or throw an UnsupportedOperationException
     */
    default ResolvedEnumConstantDeclaration asEnumConstant() {
        throw new UnsupportedOperationException(String.format("%s is not an EnumConstantDeclaration", this));
    }

    /**
     * Return this as a PatternDeclaration or throw an UnsupportedOperationException
     */
    default ResolvedTypePatternDeclaration asTypePattern() {
        throw new UnsupportedOperationException(String.format("%s is not a Pattern", this));
    }

    //TODO spearwang 2024/5/24: change signature and move to xxx
    static boolean isMatch(String test, String target) {
        if (test.equals(target)) {
            return true;
        }
        if (test.contains(".") && target.contains(".")) {
            return false;
        }
        if (test.contains(".")) {
            test = test.substring(test.lastIndexOf('.') + 1);
        }
        if (target.contains(".")) {
            target = target.substring(target.lastIndexOf('.') + 1);
        }
        return target.equals(test);
    }
}
