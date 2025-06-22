package com.thebluecode.trxautophone.utils;

//import org.opencv.core.*;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
//
//    public static Rect detectImage(Bitmap screenshot, Bitmap targetImage) {
//        Mat screenMat = convertBitmapToMat(screenshot);
//        Mat targetMat = convertBitmapToMat(targetImage);
//
//        // Convert to grayscale
//        Mat screenGray = new Mat();
//        Mat targetGray = new Mat();
//        Imgproc.cvtColor(screenMat, screenGray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(targetMat, targetGray, Imgproc.COLOR_BGR2GRAY);
//
//        // Template matching
//        Mat result = new Mat();
//        Imgproc.matchTemplate(screenGray, targetGray, result, Imgproc.TM_CCOEFF_NORMED);
//
//        double[] minVal = new double[1];
//        double[] maxVal = new double[1];
//        Point minLoc = new Point();
//        Point maxLoc = new Point();
//
//        Core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc);
//
//        // Get the top-left corner of the best match
//        int x = (int) maxLoc.x;
//        int y = (int) maxLoc.y;
//
//        // Get the width and height of the target image
//        int width = targetImage.getWidth();
//        int height = targetImage.getHeight();
//
//        return new Rect(x, y, width, height);
//    }
//
//    private static Mat convertBitmapToMat(Bitmap bitmap) {
//        Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
//        Utils.bitmapToMat(bitmap, mat);
//        return mat;
//    }
}