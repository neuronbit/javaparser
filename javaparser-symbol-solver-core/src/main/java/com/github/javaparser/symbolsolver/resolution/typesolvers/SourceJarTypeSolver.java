/*
 * Copyright (C) 2015-2016 Federico Tomassetti
 * Copyright (C) 2017-2023 The JavaParser Team.
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

package com.github.javaparser.symbolsolver.resolution.typesolvers;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.cache.Cache;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.symbolsolver.cache.GuavaCache;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.BLEEDING_EDGE;
import static com.github.javaparser.Providers.provider;

/**
 * Defines a directory containing source code that should be used for solving symbols.
 * The directory must correspond to the root package of the files within.
 *
 * @author Federico Tomassetti
 */
public class SourceJarTypeSolver implements TypeSolver {
    private final SourceJarFile sourceJarFile;
    private final JavaParser javaParser;

    private TypeSolver parent;

    private final Cache<String, Optional<CompilationUnit>> parsedFiles;
    private final Cache<String, List<CompilationUnit>> parsedDirectories;
    private final Cache<String, SymbolReference<ResolvedReferenceTypeDeclaration>> foundTypes;
    private static final int CACHE_SIZE_UNSET = -1;


    public SourceJarTypeSolver(SourceJarFile sourceJarFile) {
        this(sourceJarFile, new ParserConfiguration().setLanguageLevel(BLEEDING_EDGE));
    }

    public SourceJarTypeSolver(String sourceJarPath) throws IOException {
        this(new SourceJarFile(sourceJarPath));
    }

    public SourceJarTypeSolver(String sourceJarPath, ParserConfiguration parserConfiguration) throws IOException {
        this(new SourceJarFile(sourceJarPath), parserConfiguration);
    }

    public SourceJarTypeSolver(SourceJarFile sourceJarFile, ParserConfiguration parserConfiguration) {
        this(sourceJarFile, parserConfiguration, CACHE_SIZE_UNSET);
    }

    private <TKey, TValue> Cache<TKey, TValue> BuildCache(long cacheSizeLimit) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder().softValues();
        if (cacheSizeLimit != CACHE_SIZE_UNSET) {
            cacheBuilder.maximumSize(cacheSizeLimit);
        }
        return new GuavaCache<>(cacheBuilder.build());
    }

    /**
     * @param sourceJarPath       is the source code directory for the type solver.
     * @param parserConfiguration is the configuration the solver should use when inspecting source code files.
     * @param cacheSizeLimit      is an optional size limit to the internal caches used by this solver.
     *                            Be advised that setting the size too low might lead to noticeable performance degradation.
     *                            However, using a size limit is advised when solving symbols in large code sources. In such cases, internal caches might consume large amounts of heap space.
     */
    public SourceJarTypeSolver(SourceJarFile sourceJarPath, ParserConfiguration parserConfiguration, long cacheSizeLimit) {
        this.sourceJarFile = sourceJarPath;
        javaParser = new JavaParser(parserConfiguration);
        parsedFiles = BuildCache(cacheSizeLimit);
        parsedDirectories = BuildCache(cacheSizeLimit);
        foundTypes = BuildCache(cacheSizeLimit);
    }

    /**
     * Create a {@link SourceJarTypeSolver} with a custom cache system.
     *
     * @param sourceJarFile          The source code directory for the type solver.
     * @param javaParser             The {@link JavaParser} to be used when parsing .java files.
     * @param parsedFilesCache       The cache to be used to store {@link CompilationUnit} that is associated with
     *                               a file.
     * @param parsedDirectoriesCache The cache to store the list of {@link CompilationUnit} in a given directory.
     * @param foundTypesCache        The cache that associated a qualified name to its {@link SymbolReference}.
     */
    public SourceJarTypeSolver(SourceJarFile sourceJarFile,
                               JavaParser javaParser,
                               Cache<String, Optional<CompilationUnit>> parsedFilesCache,
                               Cache<String, List<CompilationUnit>> parsedDirectoriesCache,
                               Cache<String, SymbolReference<ResolvedReferenceTypeDeclaration>> foundTypesCache) {
        Objects.requireNonNull(sourceJarFile, "The srcDir can't be null.");
        Objects.requireNonNull(javaParser, "The javaParser can't be null.");
        Objects.requireNonNull(parsedFilesCache, "The parsedFilesCache can't be null.");
        Objects.requireNonNull(parsedDirectoriesCache, "The parsedDirectoriesCache can't be null.");
        Objects.requireNonNull(foundTypesCache, "The foundTypesCache can't be null.");

        this.sourceJarFile = sourceJarFile;
        this.javaParser = javaParser;
        this.parsedFiles = parsedFilesCache;
        this.parsedDirectories = parsedDirectoriesCache;
        this.foundTypes = foundTypesCache;
    }

    @Override
    public String toString() {
        return "JavaParserTypeSolver{" +
                "srcJar=" + sourceJarFile +
                ", parent=" + parent +
                '}';
    }

    @Override
    public TypeSolver getParent() {
        return parent;
    }

    @Override
    public void setParent(TypeSolver parent) {
        Objects.requireNonNull(parent);
        if (this.parent != null) {
            throw new IllegalStateException("This TypeSolver already has a parent.");
        }
        if (parent == this) {
            throw new IllegalStateException("The parent of this TypeSolver cannot be itself.");
        }
        this.parent = parent;
    }

    private Optional<CompilationUnit> parse(SourceJarEntry sourceFileEntry) {
        try {
            Optional<Optional<CompilationUnit>> cachedParsedFile = parsedFiles.get(sourceFileEntry.getPath());
            // If the value is already cached
            if (cachedParsedFile.isPresent()) {
                return cachedParsedFile.get();
            }

            if (sourceFileEntry.isDirectory() || !sourceFileEntry.getName().endsWith(".java")) {
                parsedFiles.put(sourceFileEntry.getPath(), Optional.empty());
                return Optional.empty();
            }

            // JavaParser only allow one parse at time.
            synchronized (javaParser) {
                Optional<CompilationUnit> compilationUnit = javaParser.parse(COMPILATION_UNIT, provider(sourceJarFile.getInputStream(sourceFileEntry),
                                javaParser.getParserConfiguration().getCharacterEncoding()))
                        .getResult();
//                        .map(cu -> cu.setStorage(srcFile));
                parsedFiles.put(sourceFileEntry.getPath(), compilationUnit);
                return compilationUnit;
            }
        } catch (IOException e) {
            throw new RuntimeException("Issue while parsing while type solving: " + sourceFileEntry.getName(), e);
        }
    }

    /**
     * Note that this parse only files directly contained in this directory.
     * It does not traverse recursively all children directory.
     */
    private List<CompilationUnit> parseDirectory(SourceJarEntry srcDirectory) {
        return parseDirectory(srcDirectory, false);
    }

    private List<CompilationUnit> parseDirectoryRecursively(SourceJarEntry srcDirectory) {
        return parseDirectory(srcDirectory, true);
    }

    private List<CompilationUnit> parseDirectory(SourceJarEntry srcDirectory, boolean recursively) {
        Optional<List<CompilationUnit>> cachedValue = parsedDirectories.get(srcDirectory.getPath());
        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        // If not cached, we need to load it
        List<CompilationUnit> units = new ArrayList<>();
        sourceJarFile.getChildEntries(srcDirectory).forEach(e -> {
            if (!e.isDirectory() && e.getName().endsWith(".java")) {
                parse(e).ifPresent(units::add);
            } else if (recursively && e.isDirectory()) {
                units.addAll(parseDirectoryRecursively(e));
            }
        });

        parsedDirectories.put(srcDirectory.getPath(), units);
        return units;
    }

    @Override
    public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String name) {
        Optional<SymbolReference<ResolvedReferenceTypeDeclaration>> cachedValue = foundTypes.get(name);
        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        // Otherwise load it
        SymbolReference<ResolvedReferenceTypeDeclaration> result = tryToSolveTypeUncached(name);
        foundTypes.put(name, result);
        return result;
    }

    private SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveTypeUncached(String name) {
        String[] nameElements = name.split("\\.");

        for (int i = nameElements.length; i > 0; i--) {
            StringBuilder filePath = new StringBuilder();
            for (int j = 0; j < i; j++) {
                filePath.append(nameElements[j]);
                if (j != i - 1) {
                    filePath.append("/");
                }
            }
            filePath.append(".java");

            StringBuilder typeName = new StringBuilder();
            for (int j = i - 1; j < nameElements.length; j++) {
                if (j != i - 1) {
                    typeName.append(".");
                }
                typeName.append(nameElements[j]);
            }

            SourceJarEntry dirToParse = null;
            // As an optimization we first try to look in the canonical position where we expect to find the file
            SourceJarEntry srcFile = sourceJarFile.getEntry(filePath.toString());
            if (Objects.nonNull(srcFile)) {
                Optional<CompilationUnit> compilationUnit = parse(srcFile);
                if (compilationUnit.isPresent()) {
                    Optional<com.github.javaparser.ast.body.TypeDeclaration<?>> astTypeDeclaration = Navigator
                            .findType(compilationUnit.get(), typeName.toString());
                    if (astTypeDeclaration.isPresent()) {
                        return SymbolReference
                                .solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
                    }
                }
                dirToParse = sourceJarFile.getParent(srcFile);
            } else {
                dirToParse = sourceJarFile.getParent(filePath.toString());
            }

            // If this is not possible we parse all files
            // We try just in the same package, for classes defined in a file not named as the class itself
            if (Objects.nonNull(dirToParse)) {
                List<CompilationUnit> compilationUnits = parseDirectory(dirToParse);
                for (CompilationUnit compilationUnit : compilationUnits) {
                    Optional<com.github.javaparser.ast.body.TypeDeclaration<?>> astTypeDeclaration = Navigator
                            .findType(compilationUnit, typeName.toString());
                    if (astTypeDeclaration.isPresent()) {
                        return SymbolReference
                                .solved(JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get()));
                    }
                }
            }
        }

        return SymbolReference.unsolved();
    }

    public static class SourceJarEntry extends ZipEntry {
        private final String sourceJarPath;

        public SourceJarEntry(String sourceJarPath, String name) {
            super(name);
            this.sourceJarPath = sourceJarPath;
        }

        public SourceJarEntry(String sourceJarPath, ZipEntry e) {
            super(e);
            this.sourceJarPath = sourceJarPath;
        }

        public String getPath() {
            return sourceJarPath + "_" + this.getName();
        }
    }

    public static class SourceJarFile {
        private final ZipFile sourceJarZip;
        private final String sourceJarPath;

        public SourceJarFile(String sourceJarPath) throws IOException {
            this.sourceJarPath = sourceJarPath;
            this.sourceJarZip = new ZipFile(sourceJarPath);
        }

        public SourceJarEntry getEntry(String name) {
            final ZipEntry entry = sourceJarZip.getEntry(name);
            if (null != entry) {
                return new SourceJarEntry(sourceJarPath, entry);
            } else {
                return null;
            }
        }

        public InputStream getInputStream(ZipEntry entry) throws IOException {
            return sourceJarZip.getInputStream(entry);
        }

        public List<SourceJarEntry> getChildEntries(SourceJarEntry sourceJarEntry) {
            final String name = sourceJarEntry.getName();
            return Collections.list(sourceJarZip.entries()).stream().filter(e -> e.getName().startsWith(name) && !name.equals(e.getName())
                            && e.getName().replace(name, "").split("/").length < 2).map(e -> new SourceJarEntry(sourceJarPath, e))
                    .collect(Collectors.toList());
        }

        public SourceJarEntry getParent(SourceJarEntry zipEntry) {
            final String name = zipEntry.getName();
            if (!name.contains("/") || name.split("/").length < 2) {
                return null;
            }

            final ZipEntry entry = sourceJarZip.getEntry(getParentPath(name));
            if (null != entry) {
                return new SourceJarEntry(sourceJarPath, entry);
            } else {
                return null;
            }
        }

        public SourceJarEntry getParent(String name) {
            final ZipEntry entry = sourceJarZip.getEntry(getParentPath(name));
            if (null != entry) {
                return new SourceJarEntry(sourceJarPath, entry);
            } else {
                return null;
            }
        }

        private String getParentPath(String name) {
            if (!name.contains("/") || name.split("/").length < 2) {
                return "";
            }

            String[] elements = name.split("/");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elements.length - 1; i++) {
                sb.append(elements[i]).append("/");
            }
            return sb.toString();
        }
    }
}
