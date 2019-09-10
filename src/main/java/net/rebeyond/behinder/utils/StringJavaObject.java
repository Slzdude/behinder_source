package net.rebeyond.behinder.utils;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class StringJavaObject extends SimpleJavaFileObject {
    private String content = "";

    public StringJavaObject(String _javaFileName, String _content) {
        super(_createStringJavaObjectUri(_javaFileName), Kind.SOURCE);
        this.content = _content;
    }

    private static URI _createStringJavaObjectUri(String name) {
        return URI.create("String:///" + name + Kind.SOURCE.extension);
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return this.content;
    }
}
