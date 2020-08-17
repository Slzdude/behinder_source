package net.rebeyond.behinder.utils.jc;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class MyJavaFileObject extends SimpleJavaFileObject {
    private ByteArrayOutputStream outPutStream;
    private final String source;

    public MyJavaFileObject(String name, String source2) {
        super(URI.create("String:///" + name + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
        this.source = source2;
    }

    public MyJavaFileObject(String name, JavaFileObject.Kind kind) {
        super(URI.create("String:///" + name + kind.extension), kind);
        this.source = null;
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        if (this.source != null) {
            return this.source;
        }
        throw new IllegalArgumentException("source == null");
    }

    public OutputStream openOutputStream() throws IOException {
        this.outPutStream = new ByteArrayOutputStream();
        return this.outPutStream;
    }

    public byte[] getCompiledBytes() {
        return this.outPutStream.toByteArray();
    }
}
