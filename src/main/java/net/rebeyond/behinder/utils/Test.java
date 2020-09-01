package net.rebeyond.behinder.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

public class Test {
    public Test() {
    }

    public static void main(String[] args) throws Exception {
        String clsName = "Cmd";
        String clsPath = String.format("res/net/rebeyond/behinder/payload/java/%s.class", clsName);
        ClassReader classReader = new ClassReader(Utils.getResourceData(clsPath));
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new CheckClassAdapter(cw) {
            @Override
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
        }, 0);
        byte[] result = cw.toByteArray();
    }
}
