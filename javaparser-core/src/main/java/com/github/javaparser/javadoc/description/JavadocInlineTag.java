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
package com.github.javaparser.javadoc.description;

import java.util.Objects;
import java.util.Optional;

import static com.github.javaparser.utils.Utils.nextWord;
import static com.github.javaparser.utils.Utils.screamingToCamelCase;

/**
 * An inline tag contained in a Javadoc description.
 * <p>
 * For example <code>{@link String}</code>
 */
public class JavadocInlineTag implements JavadocDescriptionElement {

    public static JavadocDescriptionElement fromText(String text) {
        if (!text.startsWith("{@")) {
            throw new IllegalArgumentException(String.format("Expected to start with '{@'. Text '%s'", text));
        }
        if (!text.endsWith("}")) {
            throw new IllegalArgumentException(String.format("Expected to end with '}'. Text '%s'", text));
        }
        text = text.substring(2, text.length() - 1);
        String tagName = nextWord(text);
        Type type = Type.fromName(tagName);
        Optional<String> content = Optional.empty();
        Optional<String> label = Optional.empty();
        if (type.hasContent()) {
            text = text.substring(tagName.length()).trim();
            content = Optional.of(nextWord(text));
            if (type.hasLabel()) {
                label = Optional.of(text.substring(content.get().length()).trim());
            }
        }

        return new JavadocInlineTag(tagName, type, content, label);
    }

    /**
     * The type of tag: it could either correspond to a known tag (code, docRoot, etc.) or represent
     * an unknown tag.
     */
    public enum Type {

        CODE,
        DOC_ROOT,
        INHERIT_DOC,
        LINK,
        LINKPLAIN,
        LITERAL,
        VALUE,
        SYSTEM_PROPERTY,
        UNKNOWN;

        Type() {
            this.keyword = screamingToCamelCase(name());
        }

        private String keyword;

        static JavadocInlineTag.Type fromName(String tagName) {
            for (JavadocInlineTag.Type t : JavadocInlineTag.Type.values()) {
                if (t.keyword.equals(tagName)) {
                    return t;
                }
            }
            return UNKNOWN;
        }

        boolean hasContent() {
            return this == CODE || this == LINK || this == LINKPLAIN || this == LITERAL || this == VALUE;
        }

        boolean hasLabel() {
            return this == LINK || this == LINKPLAIN;
        }
    }

    private String tagName;

    private Type type;

    private Optional<String> content;

    private Optional<String> label;

    public JavadocInlineTag(String tagName, Type type, Optional<String> content, Optional<String> label) {
        this.tagName = tagName;
        this.type = type;
        this.content = content;
        this.label = label;
    }

    public Type getType() {
        return type;
    }

    public Optional<String> getContent() {
        return content;
    }

    public String getName() {
        return tagName;
    }

    public Optional<String> getLabel() {
        return label;
    }

    @Override
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append("{@");
        sb.append(tagName);
        this.content.ifPresent(s -> sb.append(" ").append(s));
        this.label.ifPresent(s -> sb.append(" ").append(s));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JavadocInlineTag that = (JavadocInlineTag) o;
        if (!Objects.equals(tagName, that.tagName))
            return false;
        if (type != that.type)
            return false;
        if (!content.equals(that.content)) {
            return false;
        }
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        int result = tagName != null ? tagName.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (content.hashCode());
        result = 31 * result + (label.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "JavadocInlineTag{" + "tagName='" + tagName + '\''
                + ", type=" + type
                + ", content='" + content + '\''
                + ", label='" + label + '\''
                + '}';
    }
}
