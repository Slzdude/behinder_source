package org.eclipse.wb.swt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class SWTResourceManager {
    public static final int BOTTOM_LEFT = 3;
    public static final int BOTTOM_RIGHT = 4;
    protected static final int LAST_CORNER_KEY = 5;
    private static final int MISSING_IMAGE_SIZE = 10;
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    private static Map<RGB, Color> m_colorMap = new HashMap();
    private static Map<Image, Map<Image, Image>>[] m_decoratedImageMap = new Map[LAST_CORNER_KEY];
    private static Map<String, Font> m_fontMap = new HashMap();
    private static Map<Font, Font> m_fontToBoldFontMap = new HashMap();
    private static Map<Integer, Cursor> m_idToCursorMap = new HashMap();
    private static Map<String, Image> m_imageMap = new HashMap();

    public static Color getColor(int systemColorID) {
        return Display.getCurrent().getSystemColor(systemColorID);
    }

    public static Color getColor(int r, int g, int b) {
        return getColor(new RGB(r, g, b));
    }

    public static Color getColor(RGB rgb) {
        Color color = (Color) m_colorMap.get(rgb);
        if (color != null) {
            return color;
        }
        Color color2 = new Color(Display.getCurrent(), rgb);
        m_colorMap.put(rgb, color2);
        return color2;
    }

    public static void disposeColors() {
        for (Color color : m_colorMap.values()) {
            color.dispose();
        }
        m_colorMap.clear();
    }

    protected static Image getImage(InputStream stream) throws IOException {
        Image image;
        try {
            Display display = Display.getCurrent();
            ImageData data = new ImageData(stream);
            if (data.transparentPixel > 0) {
                image = new Image(display, data, data.getTransparencyMask());
            } else {
                image = new Image(display, data);
                stream.close();
            }
            return image;
        } finally {
            stream.close();
        }
    }

    public static Image getImage(String path) {
        Image image = (Image) m_imageMap.get(path);
        if (image != null) {
            return image;
        }
        try {
            Image image2 = getImage((InputStream) new FileInputStream(path));
            m_imageMap.put(path, image2);
            return image2;
        } catch (Exception e) {
            Image image3 = getMissingImage();
            m_imageMap.put(path, image3);
            return image3;
        }
    }

    public static Image getImage(Class<?> clazz, String path) {
        String key = clazz.getName() + '|' + path;
        Image image = (Image) m_imageMap.get(key);
        if (image != null) {
            return image;
        }
        try {
            Image image2 = getImage(clazz.getResourceAsStream(path));
            m_imageMap.put(key, image2);
            return image2;
        } catch (Exception e) {
            Image image3 = getMissingImage();
            m_imageMap.put(key, image3);
            return image3;
        }
    }

    private static Image getMissingImage() {
        Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
        GC gc = new GC(image);
        gc.setBackground(getColor(3));
        gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
        gc.dispose();
        return image;
    }

    public static Image decorateImage(Image baseImage, Image decorator) {
        return decorateImage(baseImage, decorator, 4);
    }

    public static Image decorateImage(Image baseImage, Image decorator, int corner) {
        if (corner <= 0 || corner >= LAST_CORNER_KEY) {
            throw new IllegalArgumentException("Wrong decorate corner");
        }
        Map<Image, Map<Image, Image>> cornerDecoratedImageMap = m_decoratedImageMap[corner];
        if (cornerDecoratedImageMap == null) {
            cornerDecoratedImageMap = new HashMap<>();
            m_decoratedImageMap[corner] = cornerDecoratedImageMap;
        }
        Map<Image, Image> decoratedMap = (Map) cornerDecoratedImageMap.get(baseImage);
        if (decoratedMap == null) {
            decoratedMap = new HashMap<>();
            cornerDecoratedImageMap.put(baseImage, decoratedMap);
        }
        Image result = (Image) decoratedMap.get(decorator);
        if (result == null) {
            Rectangle bib = baseImage.getBounds();
            Rectangle dib = decorator.getBounds();
            result = new Image(Display.getCurrent(), bib.width, bib.height);
            GC gc = new GC(result);
            gc.drawImage(baseImage, 0, 0);
            if (corner == 1) {
                gc.drawImage(decorator, 0, 0);
            } else if (corner == 2) {
                gc.drawImage(decorator, bib.width - dib.width, 0);
            } else if (corner == 3) {
                gc.drawImage(decorator, 0, bib.height - dib.height);
            } else if (corner == 4) {
                gc.drawImage(decorator, bib.width - dib.width, bib.height - dib.height);
            }
            gc.dispose();
            decoratedMap.put(decorator, result);
        }
        return result;
    }

    public static void disposeImages() {
        for (Image image : m_imageMap.values()) {
            image.dispose();
        }
        m_imageMap.clear();
        for (Map<Image, Map<Image, Image>> cornerDecoratedImageMap : m_decoratedImageMap) {
            if (cornerDecoratedImageMap != null) {
                for (Map<Image, Image> decoratedMap : cornerDecoratedImageMap.values()) {
                    for (Image image2 : decoratedMap.values()) {
                        image2.dispose();
                    }
                    decoratedMap.clear();
                }
                cornerDecoratedImageMap.clear();
            }
        }
    }

    public static Font getFont(String name, int height, int style) {
        return getFont(name, height, style, false, false);
    }

    public static Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
        String fontName = new StringBuilder(String.valueOf(name)).append('|').append(size).append('|').append(style).append('|').append(strikeout).append('|').append(underline).toString();
        Font font = (Font) m_fontMap.get(fontName);
        if (font != null) {
            return font;
        }
        FontData fontData = new FontData(name, size, style);
        if (strikeout || underline) {
            try {
                Class<?> logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT");
                Object logFont = FontData.class.getField("data").get(fontData);
                if (!(logFont == null || logFontClass == null)) {
                    if (strikeout) {
                        logFontClass.getField("lfStrikeOut").set(logFont, '\1');
                    }
                    if (underline) {
                        logFontClass.getField("lfUnderline").set(logFont, '\1');
                    }
                }
            } catch (Throwable e) {
                System.err.println("Unable to set underline or strikeout (probably on a non-Windows platform). " + e);
            }
        }
        Font font2 = new Font(Display.getCurrent(), fontData);
        m_fontMap.put(fontName, font2);
        return font2;
    }

    public static Font getBoldFont(Font baseFont) {
        Font font = (Font) m_fontToBoldFontMap.get(baseFont);
        if (font != null) {
            return font;
        }
        FontData data = baseFont.getFontData()[0];
        Font font2 = new Font(Display.getCurrent(), data.getName(), data.getHeight(), 1);
        m_fontToBoldFontMap.put(baseFont, font2);
        return font2;
    }

    public static void disposeFonts() {
        for (Font font : m_fontMap.values()) {
            font.dispose();
        }
        m_fontMap.clear();
        for (Font font2 : m_fontToBoldFontMap.values()) {
            font2.dispose();
        }
        m_fontToBoldFontMap.clear();
    }

    public static Cursor getCursor(int id) {
        Integer key = Integer.valueOf(id);
        Cursor cursor = (Cursor) m_idToCursorMap.get(key);
        if (cursor != null) {
            return cursor;
        }
        Cursor cursor2 = new Cursor(Display.getDefault(), id);
        m_idToCursorMap.put(key, cursor2);
        return cursor2;
    }

    public static void disposeCursors() {
        for (Cursor cursor : m_idToCursorMap.values()) {
            cursor.dispose();
        }
        m_idToCursorMap.clear();
    }

    public static void dispose() {
        disposeColors();
        disposeImages();
        disposeFonts();
        disposeCursors();
    }
}
