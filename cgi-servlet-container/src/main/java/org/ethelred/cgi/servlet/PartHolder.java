package org.ethelred.cgi.servlet;

import io.soabase.recordbuilder.core.RecordBuilder;

import javax.annotation.*;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@RecordBuilder
public record PartHolder(String name, ContentType contentType, byte[] data, String filename, StringStringList headers) implements Part {
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data());
    }

    @Override
    public String getContentType() {
        return contentType().toString();
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getSubmittedFileName() {
        return filename();
    }

    @Override
    public long getSize() {
        return data().length;
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedOperationException("PartHolder.write");
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException("PartHolder.delete");
    }

    @CheckForNull
    @Override
    public String getHeader(String name) {
        return headers().firstValue(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers().values(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers().names();
    }

    public String getText() {
        return new String(data(), ContentType.safeCharset(contentType()));
    }
}
