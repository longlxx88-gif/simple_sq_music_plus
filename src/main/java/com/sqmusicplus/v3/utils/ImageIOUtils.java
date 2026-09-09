package com.sqmusicplus.v3.utils;


import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: SQ
 * Date: 2022/8/9
 * Time: 19:53
 * Description:
 */
public class ImageIOUtils {



//    public static boolean webpToJpg(File webpFile, File jpgFile){
////        String src  = "a.webp";
////        String dest = "a.png";
//        try {
//            WebpIO.create().toNormalImage(webpFile, jpgFile);
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
////
////        WebpIO.create().toWEBP("hello.png", "hello.webp");
//    }


    public static BufferedImage read(File input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }
        if (!input.canRead()) {
            throw new IIOException("Can't read input file!");
        }

        ImageInputStream stream = ImageIO.createImageInputStream(input);
        if (stream == null) {
            throw new IIOException("Can't create an ImageInputStream!");
        }
        BufferedImage bi;
        try {
            bi = read(stream);
        } catch (Exception e) {
            bi = null;
        }
        if (bi == null) {
            stream.close();
            // The previous WebP ImageIO provider bundled an x86-64 native library.
            // N1 uses the ARM64 ffmpeg package already present in the runtime image.
            return readWithFfmpeg(input);
        }
        return bi;
    }

    private static BufferedImage readWithFfmpeg(File input) {
        File converted = null;
        try {
            converted = Files.createTempFile("sqmusic-cover-", ".png").toFile();
            Process process = new ProcessBuilder(
                    "ffmpeg", "-v", "error", "-y", "-i", input.getAbsolutePath(),
                    "-frames:v", "1", converted.getAbsolutePath())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            return process.exitValue() == 0 ? ImageIO.read(converted) : null;
        } catch (Exception e) {
            return null;
        } finally {
            if (converted != null) {
                try {
                    Files.deleteIfExists(converted.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static BufferedImage read(ImageInputStream stream)
            throws Exception {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }

        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext()) {
            return null;
        }

        BufferedImage bi;
        final ImageReader reader = iter.next();

        try {
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(stream, true, true);
            bi = reader.read(0, param);
        } catch ( Exception e) {
            Raster raster = reader.readRaster(0, null);
            bi = createJPEG4(raster);
        } catch (Error e) {
            // handle Errors here, including NoSuchFieldError
            System.out.println("Caught an error: " + e.getMessage());
            // usually you would not continue execution after catching an Error
            return null;
        } finally {
            reader.dispose();
            stream.close();
        }
        return bi;
    }

    private static BufferedImage createJPEG4(Raster raster) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];
        // 彩色空间转换
        float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
        float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
        float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
        float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

        for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
            float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];

            double val = y + 1.402 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y + 1.772 * (cb - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);
        }

        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb,
                rgb.length), w, h, w * 3, 3, new int[] { 0, 1, 2 }, null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, true,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }
    /**
     * 无损压缩
     */
    public static File convertWebpToJpeg(File webpFile, File jpgFile) {
        try {
            Process process = new ProcessBuilder(
                    "ffmpeg", "-v", "error", "-y", "-i", webpFile.getAbsolutePath(),
                    "-frames:v", "1", "-q:v", "2", jpgFile.getAbsolutePath())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return webpFile;
            }
            return process.exitValue() == 0 && jpgFile.isFile() && jpgFile.length() > 0
                    ? jpgFile
                    : webpFile;
        } catch (Exception e) {
            return webpFile;
        }
    }
}
