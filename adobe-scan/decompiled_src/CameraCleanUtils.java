package com.adobe.magic_clean;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import java.lang.reflect.Array;

/* JADX INFO: loaded from: classes.dex */
public class CameraCleanUtils {

    /* JADX INFO: loaded from: classes3.dex */
    public static class BDWorkflow {
        public Workflow mWorkflow = Workflow.kWorkflowCombined;
        public CaptureType mCaptureType = CaptureType.kCaptureTypePost;

        public enum CaptureType {
            kCaptureTypeLive,
            kCaptureTypePost
        }

        public enum Workflow {
            kWorkflowNN,
            kWorkflowHeuristics,
            kWorkflowCombined
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class CMLAnalyticsInput {
        public boolean mCalculateMLAnalyticsforBD = false;
        public int mMaxSizeOfEachBuffer = 225;
        public int mNumberOfBuffersForBDAnalytics = 5;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public enum CameraResult {
        kCameraResultSuccess,
        kCameraResultPartialSuccess,
        kCameraResultOutOfMemory,
        kCameraResultUnknownFailure,
        kCameraResultInvalidParameter,
        kCameraResultDocBDIssues,
        kCameraResultDocIsStraight
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class CleaningLevel {
        public static int kDefaultAggressiveCleaningLevel = 4;
        public static int kDefaultCleaningLevel = 2;
        public static int kLightTextCleaningLevel = 8;
        public static int kMaxCleaningLevel = 6;
        public static int kMinCleaningLevel;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public enum CropAndCleanMode {
        kCropAndCleanModeDefault,
        kCropAndCleanModeOverride
    }

    public static class CropConfidence {
        public int mNumInvalidations = 2;
        public double mLastStableFrames = 50.0d;
        public double mTotalStableFrames = 75.0d;
        public double mCornersAreCloseThreshold = 10.0d;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public enum DeviceOrientation {
        kDeviceOrientationPortrait,
        kDeviceOrientationLandscape
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class DewarpCurveInput {
        public boolean mEnableBetaFeatures;
        public Bitmap mInputBmpBDSize;
        public byte[] mInputBmpNNMaskPage1;
        public byte[] mInputBmpNNMaskPage2;
        public Bitmap mInputBmpOriginalSize;
        public Bitmap mInputRotatedBmpForBD;
        public PointF[] mOriginalCornersInfo;
        public PointF[] mOriginalCornersInfoBeforeCrop;
        public boolean mIsImageRotated = false;
        public boolean mIsNNPageMaskAvailable = false;
        public DocSelectorType mDocSelectorType = DocSelectorType.kDocSelectorTypeBook;
        public boolean mWasFallbackCroppingPerformed = false;

        public DewarpCurveInput() {
            recycleData();
        }

        public void recycleData() {
            Bitmap bitmap = this.mInputBmpOriginalSize;
            if (bitmap != null) {
                bitmap.recycle();
                this.mInputBmpOriginalSize = null;
            }
            Bitmap bitmap2 = this.mInputBmpBDSize;
            if (bitmap2 != null) {
                bitmap2.recycle();
                this.mInputBmpBDSize = null;
            }
            Bitmap bitmap3 = this.mInputRotatedBmpForBD;
            if (bitmap3 != null) {
                bitmap3.recycle();
                this.mInputRotatedBmpForBD = null;
            }
            this.mOriginalCornersInfo = null;
            this.mInputBmpNNMaskPage1 = null;
            this.mInputBmpNNMaskPage2 = null;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class DewarpCurveOutput {
        public PointF[] mPointsVecBottom;
        public PointF[] mPointsVecLeft;
        public PointF[] mPointsVecRight;
        public PointF[] mPointsVecTop;
        public PointF[] mRevisedCornersInfo;
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
        public int mNumPointsInEachSide = 0;
        public double mStdevTop = 0.0d;
        public double mStdevBottom = 0.0d;
        public double mStdevLeft = 0.0d;
        public double mStdevRight = 0.0d;
        public double mPointDeviationThreshold = 0.0d;
        public byte[] mNNMaskForProcessing = null;

        public DewarpCurveOutput() {
            recycleData();
        }

        public void recycleData() {
            this.mRevisedCornersInfo = null;
            this.mPointsVecTop = null;
            this.mPointsVecBottom = null;
            this.mPointsVecLeft = null;
            this.mPointsVecRight = null;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class DewarpInput {
        public DewarpCurveInput mDewarpCurveInput = new DewarpCurveInput();
        public DewarpCurveOutput mDewarpCurveOutput = new DewarpCurveOutput();
        public int mCleaningLevel = 0;
        public boolean mAreCornersUpdatedManually = false;
        public int mPadSize = 0;
        public boolean mIsTextSpaceIncreaseReq = false;
        public boolean mEnableBetaFeatures = false;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class DewarpOutput {
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
        public boolean mIsDocDewarped = false;
        public Bitmap mDewarpedBmp = null;
        public float[] mDewarpingGrid = null;
        public int mDewarpingGridHeight = 0;
        public int mDewarpingGridWidth = 0;
        public PointF[] mOriginalCornerInfo = null;
        public PointF[] mWorkingCornerInfo = null;
        public int mOriginalImageWidth = 0;
        public int mOriginalImageHeight = 0;

        public DewarpOutput() {
            recycleData();
        }

        public void recycleData() {
            Bitmap bitmap = this.mDewarpedBmp;
            if (bitmap != null) {
                bitmap.recycle();
                this.mDewarpedBmp = null;
            }
            this.mDewarpingGrid = null;
            this.mOriginalCornerInfo = null;
            this.mWorkingCornerInfo = null;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public enum DocSelectorType {
        kDocSelectorTypeBusinessCard,
        kDocSelectorTypeForm,
        kDocSelectorTypeDocument,
        kDocSelectorTypeWhiteboard,
        kDocSelectorTypeReceipt,
        kDocSelectorTypeBook,
        kDocSelectorTypeEnd
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class EdgeDetectionInput {
        public CCornersInfo[] mCCornersInfoVec;
        public Bitmap mInputImage;
        public Bitmap mInputImageBD;
        public Bitmap mInputImageDC;
        public BDWorkflow mBDWorkflow = new BDWorkflow();
        public boolean mIsLowContrast = false;
        public DocClassificationUtils.DocClassification mDocClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
        public DocSelectorType mDocSelectorType = DocSelectorType.kDocSelectorTypeEnd;
        public boolean mEnableBetaFeatures = false;
        public CMLAnalyticsInput mMLAnalyticsInput = new CMLAnalyticsInput();
        public DeviceOrientation mDeviceOrientation = DeviceOrientation.kDeviceOrientationLandscape;

        public EdgeDetectionInput(Bitmap bitmap) {
            this.mInputImage = bitmap;
        }

        public void recycleBitmaps() {
            Bitmap bitmap = this.mInputImage;
            if (bitmap != null) {
                bitmap.recycle();
                this.mInputImage = null;
            }
            Bitmap bitmap2 = this.mInputImageBD;
            if (bitmap2 != null) {
                bitmap2.recycle();
                this.mInputImageBD = null;
            }
            Bitmap bitmap3 = this.mInputImageDC;
            if (bitmap3 != null) {
                bitmap3.recycle();
                this.mInputImageDC = null;
            }
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class EdgeDetectionOutput {
        public CCornersInfo[] mCCornersInfoVec;
        public Bitmap mInputBmpBDSize;
        public byte[] mInputBmpNNMaskPage1;
        public byte[] mInputBmpNNMaskPage2;
        public Bitmap mInputRotatedBmpForBD;
        public boolean mIsImageRotated;
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
        public DocClassificationUtils.DocClassification mDocClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
        public boolean mIsLowContrast = false;
        public CMLAnalyticsOutput mMLAnalyticsOutput = new CMLAnalyticsOutput();
        public boolean mIsNNPageMaskAvailable = false;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class EraserInput {
        public int mCleaningLevel = 2;
        public boolean mEnableBetaFeatures = false;
        public Bitmap mInputBmp;

        public EraserInput(Bitmap bitmap) {
            this.mInputBmp = bitmap;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class EraserOutput {
        public Bitmap mBackgroundBmp = null;
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class FilterThumbnailOutput {
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
        public Bitmap mOutputImage = null;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class ImageCleaningInput {
        public int mCleaningLevel;
        public PointF[] mCorners;
        public Bitmap mInputImage;
        public CropAndCleanMode mCropAndCleanMode = CropAndCleanMode.kCropAndCleanModeOverride;
        public boolean mEnableBetaFeatures = false;
        public CCleaningMasks mCleaningMasks = new CCleaningMasks();
        public boolean mUseDewarpingGrid = false;
        public float[] mDewarpingGrid = null;
        public int mDewarpingGridHeight = 0;
        public int mDewarpingGridWidth = 0;
        public int mOriginalImageWidth = 0;
        public int mOriginalImageHeight = 0;
        public PointF[] mOriginalCornerInfo = null;
        public PointF[] mWorkingCornerInfo = null;

        public ImageCleaningInput(Bitmap bitmap, PointF[] pointFArr, int i) {
            this.mInputImage = bitmap;
            this.mCorners = pointFArr;
            this.mCleaningLevel = i;
        }

        public boolean SetupDewarpingAwareCrop(DewarpOutput dewarpOutput) {
            float[] fArr;
            int i;
            int i2;
            if (dewarpOutput == null || (fArr = dewarpOutput.mDewarpingGrid) == null || (i = dewarpOutput.mDewarpingGridHeight) == 0 || (i2 = dewarpOutput.mDewarpingGridWidth) == 0) {
                return false;
            }
            this.mUseDewarpingGrid = true;
            this.mDewarpingGrid = fArr;
            this.mDewarpingGridHeight = i;
            this.mDewarpingGridWidth = i2;
            this.mOriginalImageWidth = dewarpOutput.mOriginalImageWidth;
            this.mOriginalImageHeight = dewarpOutput.mOriginalImageHeight;
            this.mOriginalCornerInfo = dewarpOutput.mOriginalCornerInfo;
            this.mWorkingCornerInfo = dewarpOutput.mWorkingCornerInfo;
            return true;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class ImageCleaningOutput {
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
        public Bitmap mBitmap = null;
        public boolean mIsTintRemoved = false;
        public int mCleaningLevel = 0;
        public int mDocumentColorType = 0;
        public LABColorPixel mTintPixel = new LABColorPixel();
        public CCleaningMasks mCleaningMasks = new CCleaningMasks();
        public boolean mDewarpMappingUsed = false;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class LABColorPixel {
        public int L = 0;
        public int A = 0;
        public int B = 0;
    }

    public enum ModelType {
        kModelTypeBD,
        kModelTypeDC,
        kModelTypeDD,
        kModelTypeSR,
        kModelTypeDW,
        kModelTypeEnd
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class WhitePadInput {
        public Bitmap mInputBmp;
        public PointF[] mOriginalCornersInfo;
        public int mWhitePadSize = 0;
        public boolean mEnableBetaFeatures = false;

        public WhitePadInput(Bitmap bitmap) {
            this.mInputBmp = bitmap;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class WhitePadOutput {
        public Bitmap mOutputBmp = null;
        public CameraResult mErrorCode = CameraResult.kCameraResultSuccess;
    }

    static {
        System.loadLibrary("MagicClean");
    }

    public static Bitmap RotateBitmap(Bitmap bitmap, float f) {
        Matrix matrix = new Matrix();
        matrix.postRotate(f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap getRGBABitmapFromYUVBuffer(byte[] bArr, int i, int i2) {
        return getRGBABitmapFromYUVNative(bArr, i, i2);
    }

    private static native Bitmap getRGBABitmapFromYUVNative(byte[] bArr, int i, int i2);

    private static native Bitmap getRGBABitmapFromYUV_420_888(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4, int i5, int i6);

    public static Bitmap getRGBABitmapFromYUV_420_888_Buffer(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4, int i5, int i6) {
        return getRGBABitmapFromYUV_420_888(bArr, i, bArr2, i2, bArr3, i3, i4, i5, i6);
    }

    public static void setBasePathToModels(String str) {
        setBasePathToModelsNative(str);
    }

    private static native void setBasePathToModelsNative(String str);

    /* JADX INFO: loaded from: classes3.dex */
    public static class CMLAnalyticsOutput {
        public boolean isValid;
        public byte[][] mBufferForKeys;

        public CMLAnalyticsOutput() {
            InitCMLAnalyticsOutput(false, null);
        }

        private void InitCMLAnalyticsOutput(boolean z, byte[][] bArr) {
            this.isValid = z;
            if (bArr == null) {
                this.mBufferForKeys = null;
                return;
            }
            this.mBufferForKeys = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, bArr.length, bArr[0].length);
            for (int i = 0; i < bArr.length; i++) {
                int i2 = 0;
                while (true) {
                    byte[] bArr2 = bArr[i];
                    if (i2 < bArr2.length) {
                        this.mBufferForKeys[i][i2] = bArr2[i2];
                        i2++;
                    }
                }
            }
        }

        public CMLAnalyticsOutput(boolean z, byte[][] bArr) {
            InitCMLAnalyticsOutput(z, bArr);
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class CCleaningMasks {
        public boolean mCleaningMasksAvailable;
        public byte[] mEbabMask;
        public Bitmap mNNOutputMask;

        public CCleaningMasks() {
            this.mNNOutputMask = null;
            this.mEbabMask = null;
            this.mCleaningMasksAvailable = false;
        }

        public CCleaningMasks(Bitmap bitmap, byte[] bArr, boolean z) {
            this.mNNOutputMask = bitmap;
            this.mEbabMask = bArr;
            this.mCleaningMasksAvailable = z;
        }
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class FilterThumbnailInput {
        public Bitmap mAutoCleanImage;
        public int mCleaningLevel;
        public int mDocumentColorType;
        public boolean mIsColorInfoAvailable;
        public boolean mIsTintRemoved;
        public Bitmap mOriginalImage;
        public LABColorPixel mTintPixel;

        public FilterThumbnailInput() {
            this.mIsTintRemoved = false;
            this.mDocumentColorType = 0;
            this.mIsColorInfoAvailable = false;
            this.mTintPixel = new LABColorPixel();
        }

        public FilterThumbnailInput(Bitmap bitmap, Bitmap bitmap2, int i, boolean z, int i2, boolean z2, LABColorPixel lABColorPixel) {
            this.mOriginalImage = bitmap;
            this.mAutoCleanImage = bitmap2;
            this.mCleaningLevel = i;
            this.mIsTintRemoved = z;
            this.mDocumentColorType = i2;
            this.mIsColorInfoAvailable = z2;
            this.mTintPixel = lABColorPixel;
        }
    }
}
