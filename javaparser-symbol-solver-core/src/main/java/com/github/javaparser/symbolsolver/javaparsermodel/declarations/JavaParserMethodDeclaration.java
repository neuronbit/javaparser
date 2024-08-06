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

package com.github.javaparser.symbolsolver.javaparsermodel.declarations;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.resolution.Context;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.core.resolution.TypeVariableResolutionCapability;
import com.github.javaparser.symbolsolver.declarations.common.MethodDeclarationCommonLogic;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.github.javaparser.resolution.Navigator.demandParentNode;

/**
 * @author Federico Tomassetti
 */
public class JavaParserMethodDeclaration implements ResolvedMethodDeclaration, TypeVariableResolutionCapability {

    private MethodDeclaration wrappedNode;
    private TypeSolver typeSolver;

    public JavaParserMethodDeclaration(MethodDeclaration wrappedNode, TypeSolver typeSolver) {
        this.wrappedNode = wrappedNode;
        this.typeSolver = typeSolver;
    }

    @Override
    public String toString() {
        return "JavaParserMethodDeclaration{" +
                "wrappedNode=" + wrappedNode +
                ", typeSolver=" + typeSolver +
                '}';
    }

    @Override
    public ResolvedReferenceTypeDeclaration declaringType() {
        if (demandParentNode(wrappedNode) instanceof ObjectCreationExpr) {
            ObjectCreationExpr parentNode = (ObjectCreationExpr) demandParentNode(wrappedNode);
            return new JavaParserAnonymousClassDeclaration(parentNode, typeSolver);
        }
        // TODO Fix: to use getSymbolResolver() we have to fix many unit tests 
        // that throw IllegalStateException("Symbol resolution not configured: to configure consider setting a SymbolResolver in the ParserConfiguration"
        // return wrappedNode.getSymbolResolver().toTypeDeclaration(wrappedNode);
        return symbolResolver(typeSolver).toTypeDeclaration(demandParentNode(wrappedNode));
    }

    private SymbolResolver symbolResolver(TypeSolver typeSolver) {
        return new JavaSymbolSolver(typeSolver);
    }

    @Override
    public ResolvedType getReturnType() {
        return JavaParserFacade.get(typeSolver).convert(wrappedNode.getType(), getContext());
    }

    @Override
    public int getNumberOfParams() {
        return wrappedNode.getParameters().size();
    }

    @Override
    public ResolvedParameterDeclaration getParam(int i) {
        if (i < 0 || i >= getNumberOfParams()) {
            throw new IllegalArgumentException(String.format("No param with index %d. Number of params: %d", i, getNumberOfParams()));
        }
        return new JavaParserParameterDeclaration(wrappedNode.getParameters().get(i), typeSolver);
    }

    public MethodUsage getUsage(Node node) {
        throw new UnsupportedOperationException();
    }

    public MethodUsage resolveTypeVariables(Context context, List<ResolvedType> parameterTypes) {
        return new MethodDeclarationCommonLogic(this, typeSolver).resolveTypeVariables(context, parameterTypes);
    }

    private Context getContext() {
        return JavaParserFactory.getContext(wrappedNode, typeSolver);
    }

    @Override
    public boolean isAbstract() {
        return !wrappedNode.getBody().isPresent();
    }

    @Override
    public String getName() {
        return wrappedNode.getName().getId();
    }

    @Override
    public List<ResolvedTypeParameterDeclaration> getTypeParameters() {
        return this.wrappedNode.getTypeParameters().stream().map((astTp) -> new JavaParserTypeParameter(astTp, typeSolver)).collect(Collectors.toList());
    }

    @Override
    public Optional<Node> getParentNode() {
        return this.wrappedNode.getParentNode();
    }

    @Override
    public boolean isDefaultMethod() {
        return wrappedNode.isDefault();
    }

    @Override
    public boolean isStatic() {
        return wrappedNode.isStatic();
    }

    /**
     * Returns the JavaParser node associated with this JavaParserMethodDeclaration.
     *
     * @return A visitable JavaParser node wrapped by this object.
     */
    public com.github.javaparser.ast.body.MethodDeclaration getWrappedNode() {
        return wrappedNode;
    }

    @Override
    public AccessSpecifier accessSpecifier() {
        return wrappedNode.getAccessSpecifier();
    }

    @Override
    public int getNumberOfSpecifiedExceptions() {
        return wrappedNode.getThrownExceptions().size();
    }

    @Override
    public ResolvedType getSpecifiedException(int index) {
        if (index < 0 || index >= getNumberOfSpecifiedExceptions()) {
            throw new IllegalArgumentException(String.format("No exception with index %d. Number of exceptions: %d",
                    index, getNumberOfSpecifiedExceptions()));
        }
        return JavaParserFacade.get(typeSolver).convert(wrappedNode.getThrownExceptions()
                .get(index), wrappedNode);
    }

    @Override
    public Optional<Node> toAst() {
        return Optional.of(wrappedNode);
    }

    @Override
    public String toDescriptor() {
        return wrappedNode.toDescriptor();
    }

    @Override
    public boolean hasAnnotation(String typeName) {
        if (typeName.contains(".")) {
            String simpleName = typeName.substring(typeName.lastIndexOf('.') + 1);
            return wrappedNode.getAnnotationByName(simpleName).isPresent() || wrappedNode.getAnnotationByName(typeName).isPresent();
        } else {
            return wrappedNode.getAnnotationByName(typeName).isPresent();
        }
    }

    @Override
    public Optional<List<ResolvedAnnotationExpr>> getAnnotation(String typeName) {
        final NodeList<AnnotationExpr> annotations = wrappedNode.getAnnotations();
        final List<ResolvedAnnotationExpr> result = new ArrayList<>(annotations.size());
        for (AnnotationExpr annotation : annotations) {
            if (ResolvedDeclaration.isMatch(typeName, annotation.getName().getIdentifier())) {
                result.add(new JavaParserResolvedAnnotationExpr(annotation, typeSolver));
            }
        }
        return Optional.of(result);
    }

    @Override
    public Optional<JavadocComment> getJavadocComment() {
        return wrappedNode.getJavadocComment();
    }

    @Override
    public Optional<Javadoc> getJavadoc() {
        return wrappedNode.getJavadoc();
    }

    @Override
    public int getStartLineNumber() {
        if (wrappedNode.getTokenRange().isPresent() && wrappedNode.getTokenRange().get().getBegin().getRange().isPresent()) {
            return wrappedNode.getTokenRange().get().getBegin().getRange().get().begin.line;
        } else {
            return -1;
        }
    }

    @Override
    public boolean isPublic() {
        return wrappedNode.isPublic();
    }

    public boolean setJavadoc(Javadoc javadoc) {
        final Optional<CompilationUnit.Storage> storage = getStorage(wrappedNode);
        if (!storage.isPresent()) {
            return false;
        }
        Range beginRange = null;
        Range endRange = null;

        // default is add the method
        final Optional<TokenRange> methodTokenRange = wrappedNode.getTokenRange();
        if (methodTokenRange.isPresent() && methodTokenRange.get().getBegin().getRange().isPresent()) {
            beginRange = methodTokenRange.get().getBegin().getRange().get();
        }

        final Optional<JavadocComment> javadocComment = wrappedNode.getJavadocComment();
        if (javadocComment.isPresent()) {
            final Optional<TokenRange> tokenRange = javadocComment.get().getTokenRange();
            if (tokenRange.isPresent()) {
                if (tokenRange.get().getBegin().getRange().isPresent()) {
                    beginRange = tokenRange.get().getBegin().getRange().get();
                }
                if (tokenRange.get().getEnd().getRange().isPresent()) {
                    endRange = tokenRange.get().getEnd().getRange().get();
                }
            } else {
                // javadoc is added at runtime, object tree is not updated
                final int beginLine = beginRange.begin.line;
                final int column = beginRange.begin.column;
                LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(javadocComment.get().getContent()));
                int lineLen = 0;
                try {
                    do {
                        final String line = lineNumberReader.readLine();
                        if (line == null) {
                            break;
                        } else {
                            lineLen = line.length();
                        }
                    } while (true);
                } catch (IOException e) {
                    //ignore
                }
                final int lineCount = lineNumberReader.getLineNumber();
                endRange = new Range(beginRange.begin, new Position(beginLine + lineCount, lineLen));
            }
        }

        if (beginRange == null) {
            return false;
        } else if (endRange != null) {
            final boolean replaced = replace(javadoc, storage.get(), beginRange, endRange);
            if (!replaced) {
                return false;
            }
        } else {
            final boolean inserted = insert(javadoc, storage.get(), beginRange);
            if (!inserted) {
                return false;
            }
        }

        wrappedNode.setJavadocComment(javadoc);
        return true;
    }

    private boolean insert(Javadoc javadoc, CompilationUnit.Storage storage, Range beginRange) {
        final File file = storage.getPath().toFile();
        final File newFile = new File(file.getParentFile(), file.getName() + ".new");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                int lineNum = reader.getLineNumber();
                if (lineNum != beginRange.begin.line) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
                if (lineNum == beginRange.begin.line) {
                    final int start = Math.max(beginRange.begin.column - 1, 0);
                    if (!line.isEmpty()) {
                        writer.write(line.substring(0, start));
                    }
                    writer.write(prettyComment(javadoc.toComment().asString(), start));
                    for (int i = 0; i < start; i++) {
                        writer.write(" ");
                    }
                    if (!line.isEmpty()) {
                        writer.write(line.substring(start));
                    }
                    writer.write(System.lineSeparator());
                }
            }
            reader.close();
            writer.close();
            //TODO spearwang 2024/6/20: 文件更新了，但是已经被parse的并没有更新
            return file.delete() && newFile.renameTo(file);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean replace(Javadoc javadoc, CompilationUnit.Storage storage, Range beginRange, Range endRange) {
        final File file = storage.getPath().toFile();
        final File newFile = new File(file.getParentFile(), file.getName() + ".new");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                int lineNum = reader.getLineNumber();
                if (lineNum < beginRange.begin.line || lineNum > endRange.end.line) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
                if (lineNum == beginRange.begin.line) {
                    final int start = Math.max(beginRange.begin.column - 1, 0);
                    if (!line.isEmpty()) {
                        writer.write(line.substring(0, start));
                    }
                    writer.write(prettyComment(javadoc.toComment().asString(), start));
                }
                if (lineNum == endRange.end.line) {
                    final int column = Math.max(endRange.end.column, 0);
                    if (!line.isEmpty()) {
                        writer.write(line.substring(column));
                    }
                }
            }
            reader.close();
            writer.close();
            return file.delete() && newFile.renameTo(file);
        } catch (IOException e) {
            return false;
        }
    }

    private String prettyComment(String comment, int indent) {
        if (comment == null) {
            return comment;
        }
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(comment);
        //skip the first line
        if (scanner.hasNextLine()) {
            sb.append(scanner.next());
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (int i = 0; i < indent; i++) {
                sb.append(" ");
            }
            sb.append(line);
            sb.append(System.lineSeparator());
        }
        scanner.close();
        return sb.toString();
    }

    Optional<CompilationUnit.Storage> getStorage(MethodDeclaration wrappedNode) {
        Optional<Node> parentNode = wrappedNode.getParentNode();
        while (parentNode.isPresent()) {
            if (parentNode.get() instanceof CompilationUnit) {
                return ((CompilationUnit) parentNode.get()).getStorage();
            } else {
                parentNode = parentNode.get().getParentNode();
            }
        }
        return Optional.empty();
    }
}
