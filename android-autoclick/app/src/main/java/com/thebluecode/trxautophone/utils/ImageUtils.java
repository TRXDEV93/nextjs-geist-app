package com.thebluecode.trxautophone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced image utility class with improved image processing and matching
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 1920;
    private static final float MATCH_THRESHOLD = 0.85f;
    private static final int SAMPLE_POINTS = 100;

    private ImageUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Load bitmap from Uri
     */
    @Nullable
    public static Bitmap loadBitmap(@NonNull Context context, @NonNull Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            // First decode bounds
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
            options.inJustDecodeBounds = false;

            // Decode bitmap with sample size
            try (InputStream is2 = context.getContentResolver().openInputStream(uri)) {
                return BitmapFactory.decodeStream(is2, null, options);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap: " + uri, e);
            return null;
        }
    }

    /**
     * Save bitmap to file
     */
    public static boolean saveBitmap(@NonNull Bitmap bitmap, @NonNull File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
            return false;
        }
    }

    /**
     * Convert Image to Bitmap
     */
    @Nullable
    public static Bitmap imageToBitmap(@NonNull Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(
            image.getWidth() + rowPadding / pixelStride,
            image.getHeight(),
            Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    /**
     * Find image in screen
     */
    @Nullable
    public static Point findImageInScreen(@NonNull Bitmap screen, @NonNull Bitmap template) {
        // Resize template if needed
        if (template.getWidth() > screen.getWidth() || template.getHeight() > screen.getHeight()) {
            template = resizeBitmap(template, screen.getWidth(), screen.getHeight());
        }

        int maxX = screen.getWidth() - template.getWidth();
        int maxY = screen.getHeight() - template.getHeight();

        double bestMatch = 0;
        Point bestLocation = null;

        // Sample points for faster matching
        List<Point> samplePoints = generateSamplePoints(template.getWidth(), template.getHeight());

        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                double match = calculateMatch(screen, template, x, y, samplePoints);
                if (match > MATCH_THRESHOLD && match > bestMatch) {
                    bestMatch = match;
                    bestLocation = new Point(x, y);
                }
            }
        }

        return bestLocation;
    }

    /**
     * Calculate match score
     */
    private static double calculateMatch(Bitmap screen, Bitmap template, 
                                       int offsetX, int offsetY, List<Point> samplePoints) {
        int matches = 0;
        for (Point p : samplePoints) {
            int x = offsetX + p.x;
            int y = offsetY + p.y;
            if (x < screen.getWidth() && y < screen.getHeight() &&
                colorsMatch(screen.getPixel(x, y), template.getPixel(p.x, p.y))) {
                matches++;
            }
        }
        return (double) matches / samplePoints.size();
    }

    /**
     * Compare colors with tolerance
     */
    private static boolean colorsMatch(int color1, int color2) {
        int tolerance = 25;
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        return Math.abs(r1 - r2) <= tolerance &&
               Math.abs(g1 - g2) <= tolerance &&
               Math.abs(b1 - b2) <= tolerance;
    }

    /**
     * Generate sample points for matching
     */
    private static List<Point> generateSamplePoints(int width, int height) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < SAMPLE_POINTS; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            points.add(new Point(x, y));
        }
        return points;
    }

    /**
     * Calculate sample size for bitmap loading
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, 
                                           int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight &&
                   (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Resize bitmap
     */
    @NonNull
    public static Bitmap resizeBitmap(@NonNull Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min(
            (float) maxWidth / width,
            (float) maxHeight / height
        );

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(
            bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * Draw rectangle on bitmap
     */
    public static void drawRect(@NonNull Bitmap bitmap, @NonNull Rect rect, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(rect, paint);
    }

    /**
     * Convert bitmap to byte array
     */
    public static byte[] bitmapToByteArray(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Convert byte array to bitmap
     */
    @Nullable
    public static Bitmap byteArrayToBitmap(byte[] data) {
        if (data == null) return null;
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Take screenshot of view
     */
    @NonNull
    public static Bitmap takeScreenshot(@NonNull android.view.View view) {
        Bitmap bitmap = Bitmap.createBitmap(
            view.getWidth(),
            view.getHeight(),
            Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
