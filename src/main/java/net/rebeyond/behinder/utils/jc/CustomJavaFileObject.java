package net.rebeyond.behinder.utils.jc;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;

public class CustomJavaFileObject implements JavaFileObject {
    private final String binaryName;
    private final String name;
    private final URI uri;

    public CustomJavaFileObject(String binaryName2, URI uri2) {
        this.uri = uri2;
        this.binaryName = binaryName2;
        this.name = uri2.getPath() == null ? uri2.getSchemeSpecificPart() : uri2.getPath();
    }

    public URI toUri() {
        return this.uri;
    }

    public InputStream openInputStream() throws IOException {
        return this.uri.toURL().openStream();
    }

    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return this.name;
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getLastModified() {
        return 0;
    }

    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    public Kind getKind() {
        return Kind.CLASS;
    }

    public boolean isNameCompatible(String simpleName, Kind kind) {
        String baseName = simpleName + kind.extension;
        return kind.equals(getKind()) && (baseName.equals(getName()) || getName().endsWith("/" + baseName));
    }

    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException();
    }

    public Modifier getAccessLevel() {
        throw new UnsupportedOperationException();
    }

    public String binaryName() {
        return this.binaryName;
    }

    public String toString() {
        return "CustomJavaFileObject{uri=" + this.uri + '}';
    }
}
