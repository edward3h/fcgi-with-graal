package org.ethelred.cgi.servlet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ContentType {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private final String mimetype;
    private final Charset charset;
    private final @Nullable String boundary;

    public ContentType(String mimetype, @CheckForNull Charset charset, @Nullable String boundary) {
        this.mimetype = mimetype;
        this.charset = charset == null ? DEFAULT_CHARSET : charset;
        this.boundary = boundary;
    }

    @Nonnull
    public static Charset safeCharset(@CheckForNull ContentType contentType) {
        if (contentType == null || contentType.getCharset() == null)
        {
            return DEFAULT_CHARSET;
        }
        return contentType.getCharset();
    }

    public String getMimetype() {
        return mimetype;
    }

    public Charset getCharset() {
        return charset;
    }

    @Nullable
    public String getBoundary() {
        return boundary;
    }

    @Override
    public String toString() {
        var buf = new StringBuilder(mimetype);
        if (!DEFAULT_CHARSET.equals(charset)) {
            buf.append("; charset=").append(charset);
        }
        if (null != boundary) {
            buf.append("; boundary=").append(boundary);
        }
        return buf.toString();
    }

    public static ContentType parse(String headerValue) {
        var lexer = new ContentTypeLexer(CharStreams.fromString(headerValue));
        var parser = new ContentTypeParser(new CommonTokenStream(lexer));
        var tree = parser.content_type();
        var listener = new ParserListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return listener.build();
    }

    private static class ParserListener extends ContentTypeBaseListener {
        private String mimetype;
        private String charset;
        private @Nullable String boundary;

        public ContentType build() {
            if (mimetype == null) {
                throw new IllegalArgumentException("Invalid Content-Type");
            }
            return new ContentType(mimetype, charset == null ? null : Charset.forName(charset), boundary);
        }

        @Override
        public void exitMimetype(ContentTypeParser.MimetypeContext ctx) {
            if (ctx.exception != null) {
                throw new IllegalArgumentException(ctx.exception);
            }
            mimetype = ctx.getText();
        }

        @Override
        public void exitContent_type_parameter(ContentTypeParser.Content_type_parameterContext ctx) {
            var name = ctx.content_type_parameter_name().getText();
            var value = ctx.parameter_value().getText();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            switch (name) {
                case "charset" -> charset = value;
                case "boundary" -> boundary = value;
                default -> throw new IllegalArgumentException("Unknown parameter " + name);
            }
        }
    }
}
