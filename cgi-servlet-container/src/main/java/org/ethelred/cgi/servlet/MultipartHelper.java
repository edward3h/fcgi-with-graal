package org.ethelred.cgi.servlet;

import io.netty.handler.codec.http.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.ethelred.util.function.*;
import org.slf4j.*;

import javax.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

public class MultipartHelper extends MultipartBaseListener {
    private final Logger LOGGER = LoggerFactory.getLogger(MultipartHelper.class);
    @Nonnull
    private final ContentType contentType;
    private final PartHolderBuilder builder = PartHolderBuilder.builder();
    private ServletException servletException;
    private IOException ioException;
    private final Map<String, PartHolder> parts = new HashMap<>();

    public MultipartHelper(@Nonnull Supplier<ContentType> contentTypeSupplier, @Nonnull CheckedSupplier<InputStream, IOException> inputStreamSupplier) {
        this.contentType = contentTypeSupplier.get();
        if (contentType == null || contentType.getBoundary() == null || !HttpHeaderValues.MULTIPART_FORM_DATA.contentEquals(contentType.getMimetype())) {
            servletException = new ServletException("Invalid content type for multipart");
        } else {
            try (var input = inputStreamSupplier.get())
            {
                parse(input);
            } catch (IOException e) {
                ioException = e;
            }
        }
    }

    private void parse(InputStream inputStream) throws IOException {
        var lexer = new MultipartLexer(CharStreams.fromStream(inputStream, StandardCharsets.ISO_8859_1));
        //noinspection ConstantConditions
        lexer.setBoundary(contentType.getBoundary());
        var parser = new MultipartParser(new CommonTokenStream(lexer));
        parser.addParseListener(this);
        parser.multipart();
    }

    private void emit(PartHolder partHolder) {
        parts.put(partHolder.name(), partHolder);
        LOGGER.debug("emit({})", partHolder);
    }

    @Override
    public void enterPart(MultipartParser.PartContext ctx) {
        builder.headers(new StringStringList())
                .contentType(ContentType.parse("text/plain")); //default
    }

    @Override
    public void exitPart(MultipartParser.PartContext ctx) {
        emit(builder.build());
    }

    @Override
    public void exitHeader(MultipartParser.HeaderContext ctx) {
        if (ctx.header_name() == null) return;
        var name = ctx.header_name().getText();
        var value = ctx.header_value().getText();
        builder.headers().add(name, value);
        switch (name.toLowerCase()) {
            case "content-disposition" -> _contentDisposition(value);
            case "content-type" -> builder.contentType(ContentType.parse(value));
        }
    }

    private void _contentDisposition(String value) {
        Utils.splitEntries(value, "\\s*;\\s*", "=", (k, v) -> {
            switch (k) {
                case "name" -> builder.name(_noQuotes(v));
                case "filename" -> builder.filename(_noQuotes(v));
            }
        });
    }

    private String _noQuotes(String v) {
        if (v.startsWith("\"") && v.endsWith("\"")) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    @Override
    public void exitData(MultipartParser.DataContext ctx) {
        var text = ctx.getText();
        builder.data(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    public void addParameters(StringStringList parameterMap) {
        parts.values().forEach(partHolder -> parameterMap.add(partHolder.name(), partHolder.getText()));
    }

    public Collection<? extends Part> getParts() throws ServletException, IOException {
        _checkValid();
        return parts.values();
    }

    private void _checkValid() throws ServletException, IOException {
        if (servletException != null) throw servletException;
        if (ioException != null) throw ioException;
    }

    public Part getPart(String name) throws ServletException, IOException {
        _checkValid();
        return parts.get(name);
    }
}
