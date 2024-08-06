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

package com.github.javaparser.symbolsolver.model.typesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.resolution.types.ResolvedWildcard;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

class WildcardUsageTest {

    class Foo {
    }

    class Bar extends Foo {
    }

    private TypeSolver typeSolver;
    private ReferenceTypeImpl foo;
    private ReferenceTypeImpl bar;
    private ReferenceTypeImpl object;
    private ReferenceTypeImpl string;
    private ResolvedWildcard unbounded = ResolvedWildcard.UNBOUNDED;
    private ResolvedWildcard superFoo;
    private ResolvedWildcard superBar;
    private ResolvedWildcard extendsFoo;
    private ResolvedWildcard extendsBar;
    private ResolvedWildcard superA;
    private ResolvedWildcard extendsA;
    private ResolvedWildcard superString;
    private ResolvedWildcard extendsString;
    private ResolvedTypeVariable a;

    @BeforeEach
    void setup() {
        typeSolver = new ReflectionTypeSolver();
        foo = new ReferenceTypeImpl(new ReflectionClassDeclaration(Foo.class, typeSolver));
        bar = new ReferenceTypeImpl(new ReflectionClassDeclaration(Bar.class, typeSolver));
        object = new ReferenceTypeImpl(new ReflectionClassDeclaration(Object.class, typeSolver));
        string = new ReferenceTypeImpl(new ReflectionClassDeclaration(String.class, typeSolver));
        superFoo = ResolvedWildcard.superBound(foo);
        superBar = ResolvedWildcard.superBound(bar);
        extendsFoo = ResolvedWildcard.extendsBound(foo);
        extendsBar = ResolvedWildcard.extendsBound(bar);
        a = new ResolvedTypeVariable(ResolvedTypeParameterDeclaration.onType("A", "foo.Bar", Collections.emptyList()));
        superA = ResolvedWildcard.superBound(a);
        extendsA = ResolvedWildcard.extendsBound(a);
        superString = ResolvedWildcard.superBound(string);
        extendsString = ResolvedWildcard.extendsBound(string);
    }

    @Test
    void testIsArray() {
        assertEquals(false, unbounded.isArray());
        assertEquals(false, superFoo.isArray());
        assertEquals(false, superBar.isArray());
        assertEquals(false, extendsFoo.isArray());
        assertEquals(false, extendsBar.isArray());
    }

    @Test
    void testIsPrimitive() {
        assertEquals(false, unbounded.isPrimitive());
        assertEquals(false, superFoo.isPrimitive());
        assertEquals(false, superBar.isPrimitive());
        assertEquals(false, extendsFoo.isPrimitive());
        assertEquals(false, extendsBar.isPrimitive());
    }

    @Test
    void testIsNull() {
        assertEquals(false, unbounded.isNull());
        assertEquals(false, superFoo.isNull());
        assertEquals(false, superBar.isNull());
        assertEquals(false, extendsFoo.isNull());
        assertEquals(false, extendsBar.isNull());
    }

    @Test
    void testIsReference() {
        assertEquals(true, unbounded.isReference());
        assertEquals(true, superFoo.isReference());
        assertEquals(true, superBar.isReference());
        assertEquals(true, extendsFoo.isReference());
        assertEquals(true, extendsBar.isReference());
    }

    @Test
    void testIsReferenceType() {
        assertEquals(false, unbounded.isReferenceType());
        assertEquals(false, superFoo.isReferenceType());
        assertEquals(false, superBar.isReferenceType());
        assertEquals(false, extendsFoo.isReferenceType());
        assertEquals(false, extendsBar.isReferenceType());
    }

    @Test
    void testIsVoid() {
        assertEquals(false, unbounded.isVoid());
        assertEquals(false, superFoo.isVoid());
        assertEquals(false, superBar.isVoid());
        assertEquals(false, extendsFoo.isVoid());
        assertEquals(false, extendsBar.isVoid());
    }

    @Test
    void testIsTypeVariable() {
        assertEquals(false, unbounded.isTypeVariable());
        assertEquals(false, superFoo.isTypeVariable());
        assertEquals(false, superBar.isTypeVariable());
        assertEquals(false, extendsFoo.isTypeVariable());
        assertEquals(false, extendsBar.isTypeVariable());
    }

    @Test
    void testIsWildcard() {
        assertEquals(true, unbounded.isWildcard());
        assertEquals(true, superFoo.isWildcard());
        assertEquals(true, superBar.isWildcard());
        assertEquals(true, extendsFoo.isWildcard());
        assertEquals(true, extendsBar.isWildcard());
    }

    @Test
    void testAsArrayTypeUsage() {
        assertThrows(UnsupportedOperationException.class, () -> unbounded.asArrayType());
    }

    @Test
    void testAsReferenceTypeUsage() {
        assertThrows(UnsupportedOperationException.class, () -> unbounded.asReferenceType());
    }

    @Test
    void testAsTypeParameter() {
        assertThrows(UnsupportedOperationException.class, () -> unbounded.asTypeParameter());
    }

    @Test
    void testAsPrimitive() {
        assertThrows(UnsupportedOperationException.class, () -> unbounded.asPrimitive());
    }

    @Test
    void testAsWildcard() {
        assertTrue(unbounded == unbounded.asWildcard());
        assertTrue(superFoo == superFoo.asWildcard());
        assertTrue(superBar == superBar.asWildcard());
        assertTrue(extendsFoo == extendsFoo.asWildcard());
        assertTrue(extendsBar == extendsBar.asWildcard());
    }

    @Test
    void testAsDescribe() {
        assertEquals("?", unbounded.describe());
        assertEquals("? super com.github.javaparser.symbolsolver.model.typesystem.WildcardUsageTest.Foo", superFoo.describe());
        assertEquals("? super com.github.javaparser.symbolsolver.model.typesystem.WildcardUsageTest.Bar", superBar.describe());
        assertEquals("? extends com.github.javaparser.symbolsolver.model.typesystem.WildcardUsageTest.Foo", extendsFoo.describe());
        assertEquals("? extends com.github.javaparser.symbolsolver.model.typesystem.WildcardUsageTest.Bar", extendsBar.describe());
    }

    @Test
    void testReplaceParam() {
        ResolvedTypeParameterDeclaration tpA = ResolvedTypeParameterDeclaration.onType("A", "foo.Bar", Collections.emptyList());
        ResolvedTypeParameterDeclaration tpB = ResolvedTypeParameterDeclaration.onType("B", "foo.Bar", Collections.emptyList());
        assertTrue(unbounded == unbounded.replaceTypeVariables(tpA, string));
        assertTrue(superFoo == superFoo.replaceTypeVariables(tpA, string));
        assertTrue(extendsFoo == extendsFoo.replaceTypeVariables(tpA, string));
        assertEquals(superString, superA.replaceTypeVariables(tpA, string));
        assertEquals(extendsString, extendsA.replaceTypeVariables(tpA, string));
        assertTrue(superA == superA.replaceTypeVariables(tpB, string));
        assertTrue(extendsA == extendsA.replaceTypeVariables(tpB, string));
    }

    @Test
    void testIsAssignableBySimple() {
        assertEquals(false, unbounded.isAssignableBy(object));
        assertEquals(true, object.isAssignableBy(unbounded));
        assertEquals(false, string.isAssignableBy(unbounded));
        assertEquals(true, superFoo.isAssignableBy(foo));
        assertEquals(false, foo.isAssignableBy(superFoo));
        assertEquals(false, extendsFoo.isAssignableBy(foo));
        assertEquals(true, foo.isAssignableBy(extendsFoo));
    }

    /*@Test
    public void testIsAssignableByGenerics() {
        assertEquals(false, listOfStrings.isAssignableBy(listOfWildcardExtendsString));
        assertEquals(false, listOfStrings.isAssignableBy(listOfWildcardExtendsString));
        assertEquals(true,  listOfWildcardExtendsString.isAssignableBy(listOfStrings));
        assertEquals(false, listOfWildcardExtendsString.isAssignableBy(listOfWildcardSuperString));
        assertEquals(true,  listOfWildcardSuperString.isAssignableBy(listOfStrings));
        assertEquals(false, listOfWildcardSuperString.isAssignableBy(listOfWildcardExtendsString));
    }

    @Test
    public void testIsAssignableByGenericsInheritance() {
        assertEquals(true, collectionOfString.isAssignableBy(collectionOfString));
        assertEquals(true, collectionOfString.isAssignableBy(listOfStrings));
        assertEquals(true, collectionOfString.isAssignableBy(linkedListOfString));

        assertEquals(false, listOfStrings.isAssignableBy(collectionOfString));
        assertEquals(true, listOfStrings.isAssignableBy(listOfStrings));
        assertEquals(true, listOfStrings.isAssignableBy(linkedListOfString));

        assertEquals(false, linkedListOfString.isAssignableBy(collectionOfString));
        assertEquals(false, linkedListOfString.isAssignableBy(listOfStrings));
        assertEquals(true, linkedListOfString.isAssignableBy(linkedListOfString));
    }

    @Test
    public void testGetAllAncestorsConsideringTypeParameters() {
        assertTrue(linkedListOfString.getAllAncestors().contains(object));
        assertTrue(linkedListOfString.getAllAncestors().contains(listOfStrings));
        assertTrue(linkedListOfString.getAllAncestors().contains(collectionOfString));
        assertFalse(linkedListOfString.getAllAncestors().contains(listOfA));
    }

    @Test
    public void testGetAllAncestorsConsideringGenericsCases() {
        ReferenceTypeUsage foo = new ReferenceTypeUsage(new ReflectionClassDeclaration(Foo.class, typeSolver), typeSolver);
        ReferenceTypeUsage bar = new ReferenceTypeUsage(new ReflectionClassDeclaration(Bar.class, typeSolver), typeSolver);
        ReferenceTypeUsage left, right;

        //YES MoreBazzing<Foo, Bar> e1 = new MoreBazzing<Foo, Bar>();
        assertEquals(true,
                new ReferenceTypeUsage(
                    new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                    ImmutableList.of(foo, bar), typeSolver)
                .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(foo, bar), typeSolver))
        );

        //YES MoreBazzing<? extends Foo, Bar> e2 = new MoreBazzing<Foo, Bar>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                        ImmutableList.of(WildcardUsage.extendsBound(foo), bar), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(foo, bar), typeSolver))
        );

        //YES MoreBazzing<Foo, ? extends Bar> e3 = new MoreBazzing<Foo, Bar>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                        ImmutableList.of(foo, WildcardUsage.extendsBound(bar)), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(foo, bar), typeSolver))
        );

        //YES MoreBazzing<? extends Foo, ? extends Foo> e4 = new MoreBazzing<Foo, Bar>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                        ImmutableList.of(WildcardUsage.extendsBound(foo), WildcardUsage.extendsBound(foo)), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(foo, bar), typeSolver))
        );

        //YES MoreBazzing<? extends Foo, ? extends Foo> e5 = new MoreBazzing<Bar, Bar>();
        left = new ReferenceTypeUsage(
                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                ImmutableList.of(WildcardUsage.extendsBound(foo), WildcardUsage.extendsBound(foo)), typeSolver);
        right = new ReferenceTypeUsage(
                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                ImmutableList.of(bar, bar), typeSolver);
        assertEquals(true, left.isAssignableBy(right));

        //YES Bazzer<Object, String, String> e6 = new MoreBazzing<String, Object>();
        left = new ReferenceTypeUsage(
                new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                ImmutableList.of(object, string, string), typeSolver);
        right = new ReferenceTypeUsage(
                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                ImmutableList.of(string, object), typeSolver);
        assertEquals(true, left.isAssignableBy(right));

        //YES Bazzer<String,String,String> e7 = new MoreBazzing<String, String>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(string, string, string), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(string, string), typeSolver))
        );

        //YES Bazzer<Bar,String,Foo> e8 = new MoreBazzing<Foo, Bar>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(bar, string, foo), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(foo, bar), typeSolver))
        );

        //YES Bazzer<Foo,String,Bar> e9 = new MoreBazzing<Bar, Foo>();
        assertEquals(true,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(foo, string, bar), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(bar, foo), typeSolver))
        );

        //NO Bazzer<Bar,String,Foo> n1 = new MoreBazzing<Bar, Foo>();
        assertEquals(false,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(bar,string,foo), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(bar, foo), typeSolver))
        );

        //NO Bazzer<Bar,String,Bar> n2 = new MoreBazzing<Bar, Foo>();
        assertEquals(false,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(bar, string, foo), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(bar, foo), typeSolver))
        );

        //NO Bazzer<Foo,Object,Bar> n3 = new MoreBazzing<Bar, Foo>();
        assertEquals(false,
                new ReferenceTypeUsage(
                        new ReflectionClassDeclaration(Bazzer.class, typeSolver),
                        ImmutableList.of(foo, object, bar), typeSolver)
                        .isAssignableBy(new ReferenceTypeUsage(
                                new ReflectionClassDeclaration(MoreBazzing.class, typeSolver),
                                ImmutableList.of(bar, foo), typeSolver))
        );
    }

    @Test
    public void charSequenceIsAssignableToObject() {
        TypeSolver typeSolver = new JreTypeSolver();
        ReferenceTypeUsage charSequence = new ReferenceTypeUsage(new ReflectionInterfaceDeclaration(CharSequence.class, typeSolver), typeSolver);
        ReferenceTypeUsage object = new ReferenceTypeUsage(new ReflectionClassDeclaration(Object.class, typeSolver), typeSolver);
        assertEquals(false, charSequence.isAssignableBy(object));
        assertEquals(true, object.isAssignableBy(charSequence));
    }

    @Test
    public void testGetFieldTypeExisting() {
        class Foo<A> {
            List<A> elements;
        }

        TypeSolver typeSolver = new JreTypeSolver();
        ReferenceTypeUsage ref = new ReferenceTypeUsage(new ReflectionClassDeclaration(Foo.class, typeSolver), typeSolver);

        assertEquals(true, ref.getFieldType("elements").isPresent());
        assertEquals(true, ref.getFieldType("elements").get().isReferenceType());
        assertEquals(List.class.getCanonicalName(), ref.getFieldType("elements").get().asReferenceType().getQualifiedName());
        assertEquals(1, ref.getFieldType("elements").get().asReferenceType().typeParametersValues().size());
        assertEquals(true, ref.getFieldType("elements").get().asReferenceType().typeParametersValues().get(0).isTypeParameter());
        assertEquals("A", ref.getFieldType("elements").get().asReferenceType().typeParametersValues().get(0).asTypeParameter().getName());

        ref = new ReferenceTypeUsage(new ReflectionClassDeclaration(Foo.class, typeSolver),
                ImmutableList.of(new ReferenceTypeUsage(new ReflectionClassDeclaration(String.class, typeSolver), typeSolver)),
                typeSolver);

        assertEquals(true, ref.getFieldType("elements").isPresent());
        assertEquals(true, ref.getFieldType("elements").get().isReferenceType());
        assertEquals(List.class.getCanonicalName(), ref.getFieldType("elements").get().asReferenceType().getQualifiedName());
        assertEquals(1, ref.getFieldType("elements").get().asReferenceType().typeParametersValues().size());
        assertEquals(true, ref.getFieldType("elements").get().asReferenceType().typeParametersValues().get(0).isReferenceType());
        assertEquals(String.class.getCanonicalName(), ref.getFieldType("elements").get().asReferenceType().typeParametersValues().get(0).asReferenceType().getQualifiedName());
    }

    @Test
    public void testGetFieldTypeUnexisting() {
        class Foo<A> {
            List<A> elements;
        }

        TypeSolver typeSolver = new JreTypeSolver();
        ReferenceTypeUsage ref = new ReferenceTypeUsage(new ReflectionClassDeclaration(Foo.class, typeSolver), typeSolver);

        assertEquals(false, ref.getFieldType("bar").isPresent());

        ref = new ReferenceTypeUsage(new ReflectionClassDeclaration(Foo.class, typeSolver),
                ImmutableList.of(new ReferenceTypeUsage(new ReflectionClassDeclaration(String.class, typeSolver), typeSolver)),
                typeSolver);

        assertEquals(false, ref.getFieldType("bar").isPresent());
    }*/

	/*
	 * The raw type is the supertype of all possible generic types, whether they
	 * contain a wildcard or not. For example, Collection is the supertype of
	 * Collection<Number>, Collection<Integer>, but also of Collection<?> and
	 * Collection<? extends Number>
	 */
    @Test
	void testIsRawTypeAssignableByGenerics() {
		ResolvedType rawCollectionType = type(Collection.class.getCanonicalName());

		ResolvedType collectionOfNumbers = genericType(Collection.class.getCanonicalName(),
				Number.class.getCanonicalName());
		ResolvedType collectionOfSomethingExtendingNumbers = genericType(Collection.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));
		ResolvedType collectionOfSomethingExtendingInteger = genericType(Collection.class.getCanonicalName(),
				extendsBound(Integer.class.getCanonicalName()));

		ResolvedType collectionOfAnything = genericType(Collection.class.getCanonicalName(),
				ResolvedWildcard.UNBOUNDED);

		assertTrue(rawCollectionType.isAssignableBy(collectionOfNumbers));
		assertTrue(rawCollectionType.isAssignableBy(collectionOfSomethingExtendingNumbers));
		assertTrue(rawCollectionType.isAssignableBy(collectionOfSomethingExtendingInteger));
		assertTrue(rawCollectionType.isAssignableBy(collectionOfAnything));

	}

	/*
	 * Inheritance relationships are the same for generic types as for raw types, as
	 * long as the generic type does not vary in the hierarchy. So
	 * Collection<Number> is the supertype of List<Integer>, Set<Integer>, and
	 * ArrayList<Integer>. Similarly, Collection<? extends Number> is the supertype
	 * of List<? extends Number>, Set<? extends Number>, and ArrayList<? extends
	 * Number>. On the other hand, no relationship exists between List<Number>, and
	 * List<? extends Number>, since the generic type is no longer the same.
	 */
    @Test
	void testIsGenericTypeAssignableByGenerics() {
		// Collection<Integer>
		ResolvedType collectionOfInteger = genericType(Collection.class.getCanonicalName(),
				Integer.class.getCanonicalName());

		// List<Integer>
		ResolvedType listOfInteger = genericType(List.class.getCanonicalName(), Integer.class.getCanonicalName());
		// Set<Integer>
		ResolvedType SetOfInteger = genericType(Set.class.getCanonicalName(), Integer.class.getCanonicalName());

		assertTrue(collectionOfInteger.isAssignableBy(listOfInteger));
		assertTrue(collectionOfInteger.isAssignableBy(SetOfInteger));

		// Collection<? extends Number>
		ResolvedType collectionOfSomethingExtendingNumbers = genericType(Collection.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));

		// list<? extends Number>
		ResolvedType listOfSomethingExtendingNumbers = genericType(List.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));
		// Set<? extends Number>
		ResolvedType setOfSomethingExtendingNumbers = genericType(Set.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));

		assertTrue(collectionOfSomethingExtendingNumbers.isAssignableBy(listOfSomethingExtendingNumbers));
		assertTrue(collectionOfSomethingExtendingNumbers.isAssignableBy(setOfSomethingExtendingNumbers));

		// List<Number>
		ResolvedType listOfNumber = genericType(List.class.getCanonicalName(),
				Number.class.getCanonicalName());

		assertFalse(listOfNumber.isAssignableBy(listOfSomethingExtendingNumbers));


		// Class<String> is not assignable by class<? extends String>

		ResolvedType classOfString = genericType(Class.class.getCanonicalName(), String.class.getCanonicalName());
		ResolvedType classOfSomethingExtendingString = genericType(Class.class.getCanonicalName(),
				extendsBound(String.class.getCanonicalName()));

		assertFalse(classOfString.isAssignableBy(classOfSomethingExtendingString));

	}

	/*
	 * The generic type built on the ? type is the supertype of all the generic
	 * types that can be built on this type, whether they contain a wildcard or not.
	 * For example, the type Collection<?> is the supertype of Collection<Number>
	 * and Collection<? extends Number>.
	 */
    @Test
	void testIsUnboundGenericTypeAssignableByGenerics() {

		// Collection<?>
		ResolvedType collectionOfAnything = genericType(Collection.class.getCanonicalName(),
				ResolvedWildcard.UNBOUNDED);

		// Collection<? extends Number>
		ResolvedType collectionOfSomethingExtendingNumbers = genericType(Collection.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));
		// Collection<Number>
		ResolvedType collectionOfNumbers = genericType(Collection.class.getCanonicalName(),
				Number.class.getCanonicalName());

		assertTrue(collectionOfAnything.isAssignableBy(collectionOfSomethingExtendingNumbers));
		assertTrue(collectionOfAnything.isAssignableBy(collectionOfNumbers));
	}

	/*
	 * A generic type built on a type ? extends X (where X is a given concrete type)
	 * is the supertype of the generic type built on X, of all the generic types
	 * built on the subtypes of X, and of all the generic types bounded by
	 * extensions of the subtypes of X. In other words, Collection<? extends Number>
	 * is the supertype of Collection<Float> and Collection<? extends Float> (which
	 * is a bit of a stretch, since Float is a final class).
	 */
    @Test
	void testIsExtendBoundedGenericTypeAssignableByGenerics() {
		// Collection<? extends Number>
		ResolvedType collectionOfSomethingExtendingNumbers = genericType(Collection.class.getCanonicalName(),
				extendsBound(Number.class.getCanonicalName()));

		// Collection<Float>
		ResolvedType collectionOfFloat = genericType(Collection.class.getCanonicalName(),
				Float.class.getCanonicalName());
		// Collection<? extends Float>
		ResolvedType collectionOfSomethingExtendingFloat = genericType(Collection.class.getCanonicalName(),
				extendsBound(Float.class.getCanonicalName()));

		assertTrue(collectionOfSomethingExtendingNumbers.isAssignableBy(collectionOfFloat));
		assertTrue(collectionOfSomethingExtendingNumbers.isAssignableBy(collectionOfSomethingExtendingFloat));

	}

	@Test
	void testIsSuperBoundedGenericTypeAssignableByGenerics() {
		// Collection<? super Number>
		ResolvedType collectionOfSomethingSuperNumbers = genericType(Collection.class.getCanonicalName(),
				superBound(Number.class.getCanonicalName()));

		// List<? super Serializable>
		ResolvedType collectionOfSomethingSuperSerializable = genericType(Collection.class.getCanonicalName(),
				superBound(Serializable.class.getCanonicalName()));
		// Collection<Number>
		ResolvedType collectionOfNumber = genericType(Collection.class.getCanonicalName(),
				Number.class.getCanonicalName());
		// Collection<Serializable>
		ResolvedType collectionOfSerializable = genericType(Collection.class.getCanonicalName(),
				Serializable.class.getCanonicalName());

		assertTrue(collectionOfSomethingSuperNumbers.isAssignableBy(collectionOfSomethingSuperSerializable));
		assertTrue(collectionOfSomethingSuperNumbers.isAssignableBy(collectionOfNumber));
		assertTrue(collectionOfSomethingSuperNumbers.isAssignableBy(collectionOfSerializable));

	}

    // Utility methods

	private void print(List<ResolvedReferenceType> ancestors) {
		for (ResolvedReferenceType ancestor : ancestors) {
			System.out.println(ancestor.describe());
		}
	}

	private List<ResolvedType> types(String... types) {
		return Arrays.stream(types).map(type -> type(type)).collect(Collectors.toList());
	}

	private ResolvedType type(String type) {
		return new ReferenceTypeImpl(typeSolver.solveType(type));
	}

	private ResolvedType genericType(String type, String... parameterTypes) {
		return new ReferenceTypeImpl(typeSolver.solveType(type), types(parameterTypes));
	}

	private ResolvedType genericType(String type, ResolvedType... parameterTypes) {
		return new ReferenceTypeImpl(typeSolver.solveType(type), Arrays.asList(parameterTypes));
	}

	private ResolvedType extendsBound(String type) {
		return ResolvedWildcard.extendsBound(type(type));
	}

	private ResolvedType superBound(String type) {
		return ResolvedWildcard.superBound(type(type));
	}



}
