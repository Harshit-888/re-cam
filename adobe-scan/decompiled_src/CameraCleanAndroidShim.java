package com.adobe.magic_clean;

import android.graphics.Bitmap;
import android.graphics.PointF;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/* JADX INFO: loaded from: classes.dex */
public class CameraCleanAndroidShim {
    static {
        System.loadLibrary("MagicClean");
    }

    private void MapPointsToOriginalRotation(CCornersInfo cCornersInfo) {
        PointF[] pointsRef = cCornersInfo.getPointsRef();
        PointF[] clonedPoints = cCornersInfo.getClonedPoints();
        PointF pointF = pointsRef[1];
        PointF pointF2 = clonedPoints[0];
        pointF.x = 1.0f - pointF2.y;
        pointF.y = pointF2.x;
        PointF pointF3 = pointsRef[2];
        PointF pointF4 = clonedPoints[1];
        pointF3.x = 1.0f - pointF4.y;
        pointF3.y = pointF4.x;
        PointF pointF5 = pointsRef[3];
        PointF pointF6 = clonedPoints[2];
        pointF5.x = 1.0f - pointF6.y;
        pointF5.y = pointF6.x;
        PointF pointF7 = pointsRef[0];
        PointF pointF8 = clonedPoints[3];
        pointF7.x = 1.0f - pointF8.y;
        pointF7.y = pointF8.x;
    }

    private void SetBDAnalyticsData(CameraCleanUtils.EdgeDetectionInput edgeDetectionInput) {
        CameraCleanUtils.CMLAnalyticsInput cMLAnalyticsInput = edgeDetectionInput.mMLAnalyticsInput;
        cMLAnalyticsInput.mCalculateMLAnalyticsforBD = cMLAnalyticsInput.mCalculateMLAnalyticsforBD && edgeDetectionInput.mBDWorkflow.mCaptureType == CameraCleanUtils.BDWorkflow.CaptureType.kCaptureTypePost;
    }

    private CameraCleanUtils.CameraResult ValidateEdgeDetectionInput(CameraCleanUtils.EdgeDetectionInput edgeDetectionInput) {
        return edgeDetectionInput.mInputImage == null ? CameraCleanUtils.CameraResult.kCameraResultInvalidParameter : CameraCleanUtils.CameraResult.kCameraResultSuccess;
    }

    private CameraCleanUtils.CameraResult ValidateFilterThumbnailInput(CameraCleanUtils.FilterThumbnailInput filterThumbnailInput) {
        int i;
        int i2 = filterThumbnailInput.mCleaningLevel;
        if (i2 != CameraCleanUtils.CleaningLevel.kDefaultAggressiveCleaningLevel && filterThumbnailInput.mOriginalImage == null) {
            return CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        }
        if (i2 > CameraCleanUtils.CleaningLevel.kDefaultCleaningLevel && filterThumbnailInput.mAutoCleanImage == null) {
            return CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        }
        if (i2 < CameraCleanUtils.CleaningLevel.kMinCleaningLevel || i2 > CameraCleanUtils.CleaningLevel.kLightTextCleaningLevel) {
            return CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        }
        return (!filterThumbnailInput.mIsColorInfoAvailable || ((i = filterThumbnailInput.mDocumentColorType) >= 0 && i <= 2)) ? CameraCleanUtils.CameraResult.kCameraResultSuccess : CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
    }

    private CameraCleanUtils.CameraResult ValidateImageCleaningInput(CameraCleanUtils.ImageCleaningInput imageCleaningInput) {
        return imageCleaningInput.mInputImage == null ? CameraCleanUtils.CameraResult.kCameraResultInvalidParameter : CameraCleanUtils.CameraResult.kCameraResultSuccess;
    }

    private void WriteBDAnalyticsData(CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput) {
        String strConcat;
        if (edgeDetectionOutput.mMLAnalyticsOutput.isValid) {
            strConcat = "1";
            int i = 0;
            while (true) {
                byte[][] bArr = edgeDetectionOutput.mMLAnalyticsOutput.mBufferForKeys;
                if (i >= bArr.length) {
                    break;
                }
                if (bArr[i] != null) {
                    int i2 = 0;
                    while (true) {
                        byte[] bArr2 = edgeDetectionOutput.mMLAnalyticsOutput.mBufferForKeys[i];
                        if (i2 < bArr2.length) {
                            strConcat = strConcat.concat(String.format("%02X", Byte.valueOf(bArr2[i2])));
                            i2++;
                        }
                    }
                }
                i++;
            }
        } else {
            strConcat = "0";
        }
        try {
            FileWriter fileWriter = new FileWriter(new File("/sdcard/MCBDAnalytics.csv"), true);
            fileWriter.write(strConcat);
            fileWriter.write("\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private native CameraCleanUtils.CameraResult cropAndClean(CameraCleanUtils.ImageCleaningInput imageCleaningInput, CameraCleanUtils.ImageCleaningOutput imageCleaningOutput);

    private native CameraCleanUtils.CameraResult getCorners(CameraCleanUtils.EdgeDetectionInput edgeDetectionInput, CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput);

    private native CameraCleanUtils.CameraResult getFilterThumbnailImage(CameraCleanUtils.FilterThumbnailInput filterThumbnailInput, CameraCleanUtils.FilterThumbnailOutput filterThumbnailOutput);

    private native int[] getFinalWidthAndHeight(PointF[] pointFArr, int i, int i2);

    private native void preLoadMCModel(CameraCleanUtils.ModelType modelType, boolean z);

    public CameraCleanUtils.CameraResult CropAndClean(CameraCleanUtils.ImageCleaningInput imageCleaningInput, CameraCleanUtils.ImageCleaningOutput imageCleaningOutput) {
        CameraCleanUtils.CameraResult cameraResultValidateImageCleaningInput = ValidateImageCleaningInput(imageCleaningInput);
        if (cameraResultValidateImageCleaningInput == CameraCleanUtils.CameraResult.kCameraResultSuccess) {
            return cropAndClean(imageCleaningInput, imageCleaningOutput);
        }
        imageCleaningOutput.mErrorCode = cameraResultValidateImageCleaningInput;
        return cameraResultValidateImageCleaningInput;
    }

    /* JADX WARN: Code duplicated, block: B:11:0x0037 A[PHI: r7
  0x0037: PHI (r7v3 double) = (r7v1 double), (r7v2 double) binds: [B:10:0x0035, B:13:0x0040] A[DONT_GENERATE, DONT_INLINE]] */
    public CameraCleanUtils.CameraResult GetCorners(CameraCleanUtils.EdgeDetectionInput edgeDetectionInput, CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput) {
        boolean z;
        CameraCleanUtils.CameraResult cameraResultValidateEdgeDetectionInput = ValidateEdgeDetectionInput(edgeDetectionInput);
        if (cameraResultValidateEdgeDetectionInput != CameraCleanUtils.CameraResult.kCameraResultSuccess) {
            edgeDetectionOutput.mErrorCode = cameraResultValidateEdgeDetectionInput;
            return cameraResultValidateEdgeDetectionInput;
        }
        int height = edgeDetectionInput.mInputImage.getHeight();
        int width = edgeDetectionInput.mInputImage.getWidth();
        CameraCleanUtils.DocSelectorType docSelectorType = edgeDetectionInput.mDocSelectorType;
        CameraCleanUtils.DocSelectorType docSelectorType2 = CameraCleanUtils.DocSelectorType.kDocSelectorTypeBook;
        if (docSelectorType == docSelectorType2) {
            edgeDetectionInput.mInputImageBD = edgeDetectionInput.mInputImage;
        } else {
            double dFloor = Math.floor(Math.sqrt(((double) (width * height)) / 147456.0d));
            double d = 1.0d;
            if (dFloor < 1.0d) {
                dFloor = d;
            } else {
                d = 255.0d;
                if (dFloor > 255.0d) {
                    dFloor = d;
                }
            }
            int i = (int) dFloor;
            edgeDetectionInput.mInputImageBD = Bitmap.createScaledBitmap(edgeDetectionInput.mInputImage, width / i, height / i, false);
        }
        edgeDetectionOutput.mInputBmpBDSize = edgeDetectionInput.mInputImageBD;
        edgeDetectionInput.mInputImageDC = Bitmap.createScaledBitmap(edgeDetectionInput.mInputImage, 384, 384, false);
        SetBDAnalyticsData(edgeDetectionInput);
        if (edgeDetectionInput.mDocSelectorType != docSelectorType2 || width >= height) {
            z = false;
        } else {
            edgeDetectionInput.mInputImageBD = CameraCleanUtils.RotateBitmap(edgeDetectionInput.mInputImageBD, -90.0f);
            edgeDetectionInput.mInputImage = CameraCleanUtils.RotateBitmap(edgeDetectionInput.mInputImage, -90.0f);
            edgeDetectionOutput.mInputRotatedBmpForBD = edgeDetectionInput.mInputImageBD;
            edgeDetectionOutput.mIsImageRotated = true;
            z = true;
        }
        CameraCleanUtils.CameraResult corners = getCorners(edgeDetectionInput, edgeDetectionOutput);
        if (z) {
            MapPointsToOriginalRotation(edgeDetectionOutput.mCCornersInfoVec[0]);
            MapPointsToOriginalRotation(edgeDetectionOutput.mCCornersInfoVec[1]);
        }
        return corners;
    }

    public CameraCleanUtils.CameraResult GetFilterThumbnailImage(CameraCleanUtils.FilterThumbnailInput filterThumbnailInput, CameraCleanUtils.FilterThumbnailOutput filterThumbnailOutput) {
        CameraCleanUtils.CameraResult cameraResultValidateFilterThumbnailInput = ValidateFilterThumbnailInput(filterThumbnailInput);
        if (cameraResultValidateFilterThumbnailInput == CameraCleanUtils.CameraResult.kCameraResultSuccess) {
            return getFilterThumbnailImage(filterThumbnailInput, filterThumbnailOutput);
        }
        filterThumbnailOutput.mErrorCode = cameraResultValidateFilterThumbnailInput;
        return cameraResultValidateFilterThumbnailInput;
    }

    public Vector<Integer> GetFinalWidthAndHeight(PointF[] pointFArr, int i, int i2) {
        int[] finalWidthAndHeight = getFinalWidthAndHeight(pointFArr, i, i2);
        Vector<Integer> vector = new Vector<>(2);
        vector.add(0, Integer.valueOf(finalWidthAndHeight[0]));
        vector.add(1, Integer.valueOf(finalWidthAndHeight[1]));
        return vector;
    }

    public void PreloadMCModel(CameraCleanUtils.ModelType modelType, boolean z) {
        preLoadMCModel(modelType, z);
    }
}
