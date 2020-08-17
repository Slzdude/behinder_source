package net.rebeyond.behinder.utils;

import org.objectweb.asm.Opcodes;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final int BUFFER_SIZE = 2048;
    private static final boolean KeepDirStructure = true;

    public static void main(String[] args) {
        try {
            unZipFiles("/Users/rebeyond/newScan.zip", "/Users/rebeyond/newScan");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0036 A[SYNTHETIC, Splitter:B:17:0x0036] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x003b A[Catch:{ Exception -> 0x0056 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void toZip(java.lang.String r10, java.lang.String r11, boolean r12) throws java.lang.Exception {
        /*
            long r4 = java.lang.System.currentTimeMillis()
            r1 = 0
            r6 = 0
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x005f }
            java.io.File r8 = new java.io.File     // Catch:{ Exception -> 0x005f }
            r8.<init>(r11)     // Catch:{ Exception -> 0x005f }
            r2.<init>(r8)     // Catch:{ Exception -> 0x005f }
            java.util.zip.ZipOutputStream r7 = new java.util.zip.ZipOutputStream     // Catch:{ Exception -> 0x0061, all -> 0x0058 }
            r7.<init>(r2)     // Catch:{ Exception -> 0x0061, all -> 0x0058 }
            java.io.File r3 = new java.io.File     // Catch:{ Exception -> 0x0028, all -> 0x005b }
            r3.<init>(r10)     // Catch:{ Exception -> 0x0028, all -> 0x005b }
            boolean r8 = r3.exists()     // Catch:{ Exception -> 0x0028, all -> 0x005b }
            if (r8 != 0) goto L_0x003f
            java.lang.Exception r8 = new java.lang.Exception     // Catch:{ Exception -> 0x0028, all -> 0x005b }
            java.lang.String r9 = "需压缩文件或者文件夹不存在"
            r8.<init>(r9)     // Catch:{ Exception -> 0x0028, all -> 0x005b }
            throw r8     // Catch:{ Exception -> 0x0028, all -> 0x005b }
        L_0x0028:
            r0 = move-exception
            r6 = r7
            r1 = r2
        L_0x002b:
            java.lang.Exception r8 = new java.lang.Exception     // Catch:{ all -> 0x0033 }
            java.lang.String r9 = "zip error from ZipUtils"
            r8.<init>(r9)     // Catch:{ all -> 0x0033 }
            throw r8     // Catch:{ all -> 0x0033 }
        L_0x0033:
            r8 = move-exception
        L_0x0034:
            if (r6 == 0) goto L_0x0039
            r6.close()     // Catch:{ Exception -> 0x0056 }
        L_0x0039:
            if (r1 == 0) goto L_0x003e
            r1.close()     // Catch:{ Exception -> 0x0056 }
        L_0x003e:
            throw r8
        L_0x003f:
            java.lang.String r8 = r3.getName()
            compress(r3, r7, r8)
            if (r12 == 0) goto L_0x004b
            delDir(r10)
        L_0x004b:
            if (r7 == 0) goto L_0x0050
            r7.close()     // Catch:{ Exception -> 0x0064 }
        L_0x0050:
            if (r2 == 0) goto L_0x0055
            r2.close()     // Catch:{ Exception -> 0x0064 }
        L_0x0055:
            return
        L_0x0056:
            r9 = move-exception
            goto L_0x003e
        L_0x0058:
            r8 = move-exception
            r1 = r2
            goto L_0x0034
        L_0x005b:
            r8 = move-exception
            r6 = r7
            r1 = r2
            goto L_0x0034
        L_0x005f:
            r0 = move-exception
            goto L_0x002b
        L_0x0061:
            r0 = move-exception
            r1 = r2
            goto L_0x002b
        L_0x0064:
            r8 = move-exception
            goto L_0x0055
        */
        throw new UnsupportedOperationException("Method not decompiled: net.rebeyond.behinder.utils.ZipUtil.toZip(java.lang.String, java.lang.String, boolean):void");
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name) throws Exception {
        byte[] buf = new byte[2048];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            FileInputStream in = new FileInputStream(sourceFile);
            while (true) {
                int len = in.read(buf);
                if (len != -1) {
                    zos.write(buf, 0, len);
                } else {
                    zos.closeEntry();
                    in.close();
                    return;
                }
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                zos.putNextEntry(new ZipEntry(name + "/"));
                zos.closeEntry();
                return;
            }
            for (File file : listFiles) {
                compress(file, zos, name + "/" + file.getName());
            }
        }
    }

    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        System.currentTimeMillis();
        try {
            File zipFile = new File(zipPath);
            if (!zipFile.exists()) {
                throw new IOException("需解压文件不存在.");
            }
            File pathFile = new File(descDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);
                String outPath = (descDir + File.separator + zipEntryName).replaceAll("\\*", "/");
                File file = new File(outPath.substring(0, outPath.lastIndexOf(47)));
                if (!file.exists()) {
                    file.mkdirs();
                }
                if (!new File(outPath).isDirectory()) {
                    OutputStream out = new FileOutputStream(outPath);
                    byte[] buf1 = new byte[Opcodes.ACC_ABSTRACT];
                    while (true) {
                        int len = in.read(buf1);
                        if (len <= 0) {
                            break;
                        }
                        out.write(buf1, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void delDir(String dirPath) throws IOException {
        System.currentTimeMillis();
        try {
            File dirFile = new File(dirPath);
            if (dirFile.exists()) {
                if (dirFile.isFile()) {
                    dirFile.delete();
                    return;
                }
                File[] files = dirFile.listFiles();
                if (files != null) {
                    for (File file : files) {
                        delDir(file.toString());
                    }
                    dirFile.delete();
                }
            }
        } catch (Exception e) {
            throw new IOException("删除文件异常.");
        }
    }
}
