package com.adobe.magic_clean;

import android.graphics.Bitmap;
import android.graphics.PointF;
import defpackage.nu4;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/* JADX INFO: loaded from: classes.dex */
public class CameraCleanLiveEdgeDetectionAndroidShim {
    private static final boolean HARD_RESET = false;
    private static final String LOG_TAG;
    private static final boolean SOFT_RESET = true;
    private static final double kEpsilon = 1.0E-10d;
    private ArrayList<CCornersInfo> mAllCornersInfo;
    private int mBoundaryConfidence;
    private int mBoundaryConfidenceForCaptureThreshold;
    private int mBoundaryConfidenceForDisplayThreshold;
    private boolean mCaptureConditionsAnalyzed;
    private double mCornerToleranceLimit;
    private PointF[] mCornersTorchOff;
    private PointF[] mCornersTorchOn;
    private ArrayList<CCornersInfo> mDisplayedCornersInfo;
    private DocClassificationUtils.DocClassification mDocClassification;
    private int mDocClassificationConfidence;
    private boolean mEnableBetaFeatures;
    private int mGlareAnalysisTimeoutCounter;
    private boolean mGlareAnalyzed;
    private boolean mHardShadowsAnalyzed;
    private byte[] mImageBufferTorchOff;
    private byte[] mImageBufferTorchOn;
    private int mImageTorchOffHeight;
    private int mImageTorchOffWidth;
    private int mImageTorchOnHeight;
    private int mImageTorchOnWidth;
    private CameraCleanUtils.CropConfidence mInCropConfidence;
    private boolean mIsLowContrastBD;
    private CCornersInfo mLastStableCornerInfo;
    private int mLiveBoundaryCaptureEnableCounter;
    private long mLiveBoundaryCaptureEnableInterval;
    private boolean mLiveBoundaryCaptureEnabled;
    private boolean mLiveBoundaryHintUpdateEnabled;
    private long mLiveBoundaryHintUpdateInterval;
    private Timer mLiveBoundaryTimeKeeper;
    private int mLiveBoundaryTimeKeeperCounter;
    private long mLiveBoundaryTimeKeeperInterval;
    private boolean mLiveBoundaryTriggerCapture;
    private boolean mLiveBoundaryUpdateEnabled;
    private long mLiveBoundaryUpdateInterval;
    private int mLowBrightnessFrameCount;
    private boolean mLowLightAnalyzed;
    private int mNumInputFrames;
    private int mNumInputFramesBD;
    private int mNumInvalidations;
    private CCornersInfo mPrevCorners;
    private boolean mPrevDetectionResult;
    private LiveBoundaryHint mPrevLiveBoundaryHint;
    private LiveBoundaryTorchHint mPrevLiveBoundaryTorchHint;
    private int mPrevSensorOffset;
    private int mSoftResetCount;
    private int mStableBoundaryConfidence;
    private double mTimeTakenInGetLiveCorners;
    double mTimeThresholdToResetBoundaryConfidenceHigh;
    double mTimeThresholdToResetBoundaryConfidenceLow;
    private int mTorchOffFramesCount;
    private int mTorchOnFramesCount;

    /* JADX INFO: loaded from: classes3.dex */
    public static class CCameraCleanLiveBoundaryOutput {
        public CCornersInfo mCornersInfo = new CCornersInfo();
        public CCornersInfo mOriginalCornersInfo = new CCornersInfo();
        public boolean mIsLowContrast = false;
        public boolean mIsBoundaryUpdated = false;
        public LiveBoundaryHint mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintNone;
        public LiveBoundaryTorchHint mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOff;
        public int mOutCropConfidence = 1;
        public int mFrames = 0;
        public int mBoundaryConfidence = 0;
        public int mBoundaryDisplayThreshold = 0;
        public int mBoundaryCaptureThreshold = 0;
        public long mTimeTaken = 0;
    }

    public enum LiveBoundaryHint {
        kLiveBoundaryHintNone,
        kLiveBoundaryHintLookingForDocument,
        kLiveBoundaryHintHoldForCapture,
        kLiveBoundaryHintReadyForCapture
    }

    public enum LiveBoundaryTorchHint {
        kLiveBoundaryTorchHintNone,
        kLiveBoundaryTorchHintTorchOn,
        kLiveBoundaryTorchHintTorchOff
    }

    static {
        System.loadLibrary("MagicClean");
        LOG_TAG = CameraCleanLiveEdgeDetectionAndroidShim.class.getName();
    }

    public CameraCleanLiveEdgeDetectionAndroidShim(double d) {
        this.mCornerToleranceLimit = d < 0.2d ? 0.2d : d;
        this.mLiveBoundaryTimeKeeperInterval = 250L;
        this.mLiveBoundaryUpdateInterval = 750L;
        this.mLiveBoundaryHintUpdateInterval = 1000L;
        this.mLiveBoundaryCaptureEnableInterval = 1500L;
        this.mDocClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
        this.mDocClassificationConfidence = 0;
        this.mNumInputFrames = 0;
        this.mAllCornersInfo = new ArrayList<>();
        this.mDisplayedCornersInfo = new ArrayList<>();
        this.mNumInvalidations = 0;
        this.mEnableBetaFeatures = false;
        this.mInCropConfidence = new CameraCleanUtils.CropConfidence();
        this.mTimeTakenInGetLiveCorners = 0.0d;
        this.mTimeThresholdToResetBoundaryConfidenceHigh = 0.7d;
        this.mTimeThresholdToResetBoundaryConfidenceLow = 0.3d;
        resetLiveBoundary(false);
        Timer timer = new Timer();
        this.mLiveBoundaryTimeKeeper = timer;
        timer.scheduleAtFixedRate(new TimerTask() { // from class: com.adobe.magic_clean.CameraCleanLiveEdgeDetectionAndroidShim.1
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                CameraCleanLiveEdgeDetectionAndroidShim.this.LiveBoundaryTimeKeeperFunction();
            }
        }, 0L, this.mLiveBoundaryTimeKeeperInterval);
    }

    private void AdjustBoundaryConfidenceThresholds() {
        double d = this.mTimeTakenInGetLiveCorners;
        if (d > this.mTimeThresholdToResetBoundaryConfidenceHigh) {
            MagicCleanLog.d(LOG_TAG, "getLiveCornersGray_AdjustBoundaryConfidenceThresholds: " + this.mTimeTakenInGetLiveCorners);
            if (this.mBoundaryConfidenceForDisplayThreshold > 1 || this.mBoundaryConfidenceForCaptureThreshold > 3) {
                this.mBoundaryConfidenceForDisplayThreshold = 1;
                this.mBoundaryConfidenceForCaptureThreshold = 3;
                return;
            }
            return;
        }
        if (d > this.mTimeThresholdToResetBoundaryConfidenceLow) {
            MagicCleanLog.d(LOG_TAG, "getLiveCornersGray_AdjustBoundaryConfidenceThresholds: " + this.mTimeTakenInGetLiveCorners);
            if (this.mBoundaryConfidenceForDisplayThreshold > 2 || this.mBoundaryConfidenceForCaptureThreshold > 4) {
                this.mBoundaryConfidenceForDisplayThreshold = 2;
                this.mBoundaryConfidenceForCaptureThreshold = 4;
            }
        }
    }

    private void AdjustCurrentBoundaryConfidence() {
        if (this.mLiveBoundaryUpdateEnabled && this.mTimeTakenInGetLiveCorners > this.mTimeThresholdToResetBoundaryConfidenceHigh) {
            this.mBoundaryConfidence = 0;
            return;
        }
        int i = this.mBoundaryConfidence;
        if (i > 5) {
            this.mBoundaryConfidence = i >> 1;
        } else if (i > 0) {
            this.mBoundaryConfidence = i - 1;
        }
    }

    private void AnalyzeCaptureExperienceParams(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        if (!this.mCaptureConditionsAnalyzed) {
            if (!cCameraCleanLiveBoundaryInput.mAnalyzeCaptureConditions) {
                this.mCaptureConditionsAnalyzed = true;
                return;
            }
            if (!cCameraCleanLiveBoundaryInput.mIsTorchActive) {
                this.mImageBufferTorchOff = (byte[]) cCameraCleanLiveBoundaryInput.mInputBufferGray.clone();
                this.mImageTorchOffWidth = cCameraCleanLiveBoundaryInput.mWidth;
                this.mImageTorchOffHeight = cCameraCleanLiveBoundaryInput.mHeight;
                this.mCornersTorchOff = cCameraCleanLiveBoundaryOutput.mCornersInfo.getClonedPoints();
                if (this.mTorchOffFramesCount == 0) {
                    this.mTorchOffFramesCount = this.mLiveBoundaryTimeKeeperCounter;
                }
                if (!this.mLowLightAnalyzed) {
                    AnalyzeLowLight(cCameraCleanLiveBoundaryInput, cCameraCleanLiveBoundaryOutput);
                }
                if (!this.mHardShadowsAnalyzed && !IsLowLight() && this.mLiveBoundaryTimeKeeperCounter - this.mTorchOffFramesCount > 3) {
                    AnalyzeHardShadows(cCameraCleanLiveBoundaryInput, cCameraCleanLiveBoundaryOutput);
                }
                if (this.mLowLightAnalyzed && this.mHardShadowsAnalyzed) {
                    if (cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint != LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOn) {
                        this.mGlareAnalyzed = true;
                    } else {
                        int i = this.mGlareAnalysisTimeoutCounter;
                        int i2 = this.mLiveBoundaryTimeKeeperCounter;
                        if (i == 0) {
                            this.mGlareAnalysisTimeoutCounter = i2;
                        } else if (i2 - i >= 8) {
                            this.mGlareAnalyzed = true;
                        }
                    }
                }
            } else if (!this.mGlareAnalyzed) {
                AnalyzeGlare(cCameraCleanLiveBoundaryInput, cCameraCleanLiveBoundaryOutput);
            }
            this.mCaptureConditionsAnalyzed = this.mLowLightAnalyzed && this.mHardShadowsAnalyzed && this.mGlareAnalyzed;
        }
        if (this.mCaptureConditionsAnalyzed) {
            this.mLiveBoundaryTriggerCapture = true;
        }
    }

    private void AnalyzeGlare(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        int i;
        int i2;
        this.mImageBufferTorchOn = (byte[]) cCameraCleanLiveBoundaryInput.mInputBufferGray.clone();
        this.mImageTorchOnWidth = cCameraCleanLiveBoundaryInput.mWidth;
        this.mImageTorchOnHeight = cCameraCleanLiveBoundaryInput.mHeight;
        PointF[] clonedPoints = cCameraCleanLiveBoundaryOutput.mCornersInfo.getClonedPoints();
        this.mCornersTorchOn = clonedPoints;
        if (this.mTorchOnFramesCount == 0) {
            this.mTorchOnFramesCount = this.mLiveBoundaryTimeKeeperCounter;
        }
        byte[] bArr = this.mImageBufferTorchOff;
        if (bArr == null || this.mCornersTorchOff == null || (i = this.mImageTorchOffWidth) == 0 || (i2 = this.mImageTorchOffHeight) == 0 || this.mImageBufferTorchOn == null || clonedPoints == null || this.mImageTorchOnWidth == 0 || this.mImageTorchOnHeight == 0 || this.mLiveBoundaryTimeKeeperCounter - this.mTorchOnFramesCount <= 3) {
            return;
        }
        Bitmap rGBABitmapFromYUVBuffer = CameraCleanUtils.getRGBABitmapFromYUVBuffer(bArr, i, i2);
        Bitmap scaledBitmap = getScaledBitmap(rGBABitmapFromYUVBuffer, rGBABitmapFromYUVBuffer.getWidth(), rGBABitmapFromYUVBuffer.getHeight());
        if (scaledBitmap != rGBABitmapFromYUVBuffer && scaledBitmap != null) {
            rGBABitmapFromYUVBuffer.recycle();
            rGBABitmapFromYUVBuffer = scaledBitmap;
        }
        Bitmap rGBABitmapFromYUVBuffer2 = CameraCleanUtils.getRGBABitmapFromYUVBuffer(this.mImageBufferTorchOn, this.mImageTorchOnWidth, this.mImageTorchOnHeight);
        Bitmap scaledBitmap2 = getScaledBitmap(rGBABitmapFromYUVBuffer2, rGBABitmapFromYUVBuffer2.getWidth(), rGBABitmapFromYUVBuffer2.getHeight());
        if (scaledBitmap2 != rGBABitmapFromYUVBuffer && scaledBitmap2 != null) {
            rGBABitmapFromYUVBuffer2.recycle();
            rGBABitmapFromYUVBuffer2 = scaledBitmap2;
        }
        boolean zImageHasGlareAfterTorch = imageHasGlareAfterTorch(rGBABitmapFromYUVBuffer, this.mCornersTorchOff, rGBABitmapFromYUVBuffer2, this.mCornersTorchOn);
        rGBABitmapFromYUVBuffer.recycle();
        rGBABitmapFromYUVBuffer2.recycle();
        if (zImageHasGlareAfterTorch) {
            cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOff;
        }
        this.mGlareAnalyzed = true;
        this.mLowLightAnalyzed = true;
        this.mHardShadowsAnalyzed = true;
    }

    private void AnalyzeHardShadows(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        int i;
        int i2;
        if (this.mLiveBoundaryCaptureEnabled) {
            this.mHardShadowsAnalyzed = true;
            this.mLowLightAnalyzed = true;
            return;
        }
        byte[] bArr = this.mImageBufferTorchOff;
        if (bArr == null || this.mCornersTorchOff == null || (i = this.mImageTorchOffWidth) == 0 || (i2 = this.mImageTorchOffHeight) == 0) {
            return;
        }
        Bitmap rGBABitmapFromYUVBuffer = CameraCleanUtils.getRGBABitmapFromYUVBuffer(bArr, i, i2);
        Bitmap scaledBitmap = getScaledBitmap(rGBABitmapFromYUVBuffer, rGBABitmapFromYUVBuffer.getWidth(), rGBABitmapFromYUVBuffer.getHeight());
        if (scaledBitmap != rGBABitmapFromYUVBuffer && scaledBitmap != null) {
            rGBABitmapFromYUVBuffer.recycle();
            rGBABitmapFromYUVBuffer = scaledBitmap;
        }
        boolean zImageHasDarkShadow = imageHasDarkShadow(rGBABitmapFromYUVBuffer, this.mCornersTorchOff);
        rGBABitmapFromYUVBuffer.recycle();
        if (zImageHasDarkShadow) {
            cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOn;
            this.mLowLightAnalyzed = true;
        }
        this.mHardShadowsAnalyzed = true;
    }

    private void AnalyzeLowLight(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        int i = IsAmbientLightDarkerThan(cCameraCleanLiveBoundaryInput, 5.0d) ? this.mLowBrightnessFrameCount + 1 : 0;
        this.mLowBrightnessFrameCount = i;
        if (this.mLiveBoundaryTimeKeeperCounter - this.mTorchOffFramesCount > 3) {
            boolean z = this.mLiveBoundaryCaptureEnabled;
            if (i >= (z ? 2 : 5)) {
                cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOn;
                this.mLowLightAnalyzed = true;
                this.mHardShadowsAnalyzed = true;
            } else if (z) {
                this.mLowLightAnalyzed = true;
                this.mHardShadowsAnalyzed = true;
            }
        }
    }

    private boolean AreCurrentCornersWithinTolerance(CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput, double d, boolean z) {
        PointF[] pointsRef = cCameraCleanLiveBoundaryOutput.mCornersInfo.getPointsRef();
        PointF[] pointsRef2 = this.mPrevCorners.getPointsRef();
        if (Math.abs(pointsRef[0].x - pointsRef2[0].x) <= d && Math.abs(pointsRef[0].y - pointsRef2[0].y) <= d && Math.abs(pointsRef[1].x - pointsRef2[1].x) <= d && Math.abs(pointsRef[1].y - pointsRef2[1].y) <= d && Math.abs(pointsRef[2].x - pointsRef2[2].x) <= d && Math.abs(pointsRef[2].y - pointsRef2[2].y) <= d && Math.abs(pointsRef[3].x - pointsRef2[3].x) <= d && Math.abs(pointsRef[3].y - pointsRef2[3].y) <= d) {
            return true;
        }
        if (!z) {
            return false;
        }
        cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mPrevCorners;
        return true;
    }

    private int CalculateCropConfidence(ArrayList<CCornersInfo> arrayList, int i, double d, double d2, double d3) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6 = 1;
        int i7 = this.mNumInvalidations >= i ? 1 : 0;
        if (i7 == 0 && CheckIfQuadHasDummyEdge((CCornersInfo) nu4.a(1, arrayList))) {
            i7 = 1;
        }
        int size = arrayList.size();
        int i8 = size % 2 != 0 ? size + 1 : size;
        if (i7 == 0) {
            int i9 = size - 1;
            int i10 = i9;
            int i11 = i8;
            int i12 = 0;
            i4 = 0;
            while (true) {
                if (i10 >= 0) {
                    if (i12 == i8 - i11) {
                        i2 = i6;
                        if (i12 == (d / 100.0d) * ((double) i8)) {
                            i3 = i12;
                            i7 = i2;
                            i5 = i7;
                            break;
                        }
                    } else {
                        i2 = i6;
                    }
                    if (WithinTolerance(arrayList.get(i9), arrayList.get(i10), d3 / 100.0d)) {
                        i12++;
                    } else {
                        i4++;
                    }
                    i11--;
                    i10--;
                    i6 = i2;
                } else {
                    i2 = i6;
                    i3 = i12;
                }
            }
            return (i7 == 0 || ((double) i3) / ((double) (i3 + i4)) < d2 / 100.0d) ? i5 : i2;
        }
        i2 = 1;
        i3 = 0;
        i4 = 0;
        i5 = 0;
        if (i7 == 0) {
        }
    }

    private boolean CanSetHoldForCaptureHint(CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        if (this.mBoundaryConfidence >= this.mBoundaryConfidenceForCaptureThreshold) {
            return this.mLiveBoundaryCaptureEnabled;
        }
        return false;
    }

    private boolean CanSetReadyForCaptureHint(CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        return this.mBoundaryConfidence >= this.mBoundaryConfidenceForCaptureThreshold && this.mLiveBoundaryCaptureEnabled && this.mCaptureConditionsAnalyzed && this.mLiveBoundaryTriggerCapture;
    }

    private boolean CheckIfQuadHasDummyEdge(CCornersInfo cCornersInfo) {
        return (((double) Math.abs(cCornersInfo.getPointsRef()[0].y - 0.0f)) <= kEpsilon && ((double) Math.abs(cCornersInfo.getPointsRef()[1].y - 0.0f)) <= kEpsilon) || (((double) Math.abs(cCornersInfo.getPointsRef()[1].x - 1.0f)) <= kEpsilon && ((double) Math.abs(cCornersInfo.getPointsRef()[2].x - 1.0f)) <= kEpsilon) || ((((double) Math.abs(cCornersInfo.getPointsRef()[2].y - 1.0f)) <= kEpsilon && ((double) Math.abs(cCornersInfo.getPointsRef()[3].y - 1.0f)) <= kEpsilon) || (((double) Math.abs(cCornersInfo.getPointsRef()[3].x - 0.0f)) <= kEpsilon && ((double) Math.abs(cCornersInfo.getPointsRef()[0].x - 0.0f)) <= kEpsilon));
    }

    private boolean GetBoundariesInLivePreview(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput, boolean z) {
        CCornersInfo cCornersInfo;
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (this.mPrevSensorOffset != cCameraCleanLiveBoundaryInput.mSenserOffset) {
            MagicCleanLog.d(LOG_TAG, "sensor offset changed from " + this.mPrevSensorOffset + " to " + cCameraCleanLiveBoundaryInput.mSenserOffset);
        }
        CameraCleanUtils.BDWorkflow bDWorkflow = new CameraCleanUtils.BDWorkflow();
        bDWorkflow.mCaptureType = CameraCleanUtils.BDWorkflow.CaptureType.kCaptureTypeLive;
        bDWorkflow.mWorkflow = CameraCleanUtils.BDWorkflow.Workflow.kWorkflowHeuristics;
        CameraCleanUtils.EdgeDetectionInput edgeDetectionInput = new CameraCleanUtils.EdgeDetectionInput(CameraCleanUtils.getRGBABitmapFromYUVBuffer(cCameraCleanLiveBoundaryInput.mInputBufferGray, cCameraCleanLiveBoundaryInput.mWidth, cCameraCleanLiveBoundaryInput.mHeight));
        edgeDetectionInput.mBDWorkflow = bDWorkflow;
        edgeDetectionInput.mIsLowContrast = this.mIsLowContrastBD;
        DocClassificationUtils.DocClassification docClassification = this.mDocClassification;
        DocClassificationUtils.DocClassification docClassification2 = DocClassificationUtils.DocClassification.kDocClassificationBusinessCard;
        int i = this.mNumInputFrames;
        if (docClassification == docClassification2) {
            if (i % 6 == 0) {
                docClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
            }
            edgeDetectionInput.mDocClassification = docClassification;
        } else {
            if (i % 3 == 0) {
                docClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
            }
            edgeDetectionInput.mDocClassification = docClassification;
        }
        edgeDetectionInput.mDocSelectorType = cCameraCleanLiveBoundaryInput.mDocSelectorType;
        CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput = new CameraCleanUtils.EdgeDetectionOutput();
        new CameraCleanAndroidShim().GetCorners(edgeDetectionInput, edgeDetectionOutput);
        edgeDetectionInput.recycleBitmaps();
        CCornersInfo[] cCornersInfoArr = edgeDetectionOutput.mCCornersInfoVec;
        if (cCornersInfoArr != null) {
            if (cCornersInfoArr.length > 0) {
                CCornersInfo cCornersInfo2 = cCornersInfoArr[0];
                cCornersInfo = new CCornersInfo(cCornersInfo2.mCornersType, cCornersInfo2.getPointsRef(), edgeDetectionOutput.mCCornersInfoVec[0].mIsGoodForDisplay);
            } else {
                cCornersInfo = new CCornersInfo();
            }
            if (cCameraCleanLiveBoundaryInput.mDocSelectorType == CameraCleanUtils.DocSelectorType.kDocSelectorTypeBook) {
                cCornersInfo.getPointsRef()[1] = edgeDetectionOutput.mCCornersInfoVec[1].getPointsRef()[1];
                cCornersInfo.getPointsRef()[2] = edgeDetectionOutput.mCCornersInfoVec[1].getPointsRef()[2];
            }
        } else {
            cCornersInfo = null;
        }
        boolean z2 = edgeDetectionOutput.mIsLowContrast;
        cCameraCleanLiveBoundaryOutput.mIsLowContrast = z2;
        this.mIsLowContrastBD = z2;
        DocClassificationUtils.DocClassification docClassification3 = this.mDocClassification;
        DocClassificationUtils.DocClassification docClassification4 = edgeDetectionOutput.mDocClassification;
        if (docClassification3 == docClassification4 && docClassification4 == docClassification2) {
            if (docClassification4 == docClassification2) {
                this.mDocClassificationConfidence++;
            }
        } else if (this.mDocClassificationConfidence > 0) {
            AdjustDocClassificationConfidence();
        }
        this.mDocClassification = edgeDetectionOutput.mDocClassification;
        MagicCleanLog.d(LOG_TAG, "getLiveCornersGray_GetBoundariesInLivePreview: " + (System.currentTimeMillis() - jCurrentTimeMillis));
        if (cCornersInfo != null) {
            cCameraCleanLiveBoundaryOutput.mCornersInfo = cCornersInfo;
            return true;
        }
        if (!z) {
            return false;
        }
        cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mPrevCorners;
        return true;
    }

    private boolean IgnoreBoundaryChangesDueToTorchOn(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput) {
        if (!cCameraCleanLiveBoundaryInput.mIsTorchActive) {
            return false;
        }
        int i = this.mTorchOnFramesCount;
        return i == 0 || this.mLiveBoundaryTimeKeeperCounter - i <= 3;
    }

    private boolean IsAmbientLightDarkerThan(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, double d) {
        return cCameraCleanLiveBoundaryInput.mBrightnessFromCamera <= d;
    }

    private boolean IsLowLight() {
        return this.mLowBrightnessFrameCount != 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void LiveBoundaryTimeKeeperFunction() {
        int i = this.mLiveBoundaryTimeKeeperCounter + 1;
        this.mLiveBoundaryTimeKeeperCounter = i;
        long j = this.mLiveBoundaryUpdateInterval;
        long j2 = this.mLiveBoundaryTimeKeeperInterval;
        if (((long) i) % (j / j2) == 0) {
            this.mLiveBoundaryUpdateEnabled = true;
        }
        if (((long) i) % (this.mLiveBoundaryHintUpdateInterval / j2) == 0) {
            this.mLiveBoundaryHintUpdateEnabled = true;
        }
        if (this.mBoundaryConfidence > this.mBoundaryConfidenceForDisplayThreshold) {
            int i2 = this.mLiveBoundaryCaptureEnableCounter + 1;
            this.mLiveBoundaryCaptureEnableCounter = i2;
            long j3 = this.mLiveBoundaryCaptureEnableInterval;
            if (((long) i2) % (j3 / j2) == 0) {
                this.mLiveBoundaryCaptureEnabled = true;
            }
            if (((long) i2) % ((j3 + 1000) / j2) == 0) {
                this.mLiveBoundaryTriggerCapture = true;
            }
        }
    }

    private void LivePreviewContainsDocument(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        cCameraCleanLiveBoundaryInput.mLivePreviewContainsDoc = false;
        CameraCleanDocDetectionAndroidShim cameraCleanDocDetectionAndroidShim = new CameraCleanDocDetectionAndroidShim();
        Bitmap rGBABitmapFromYUVBuffer = CameraCleanUtils.getRGBABitmapFromYUVBuffer(cCameraCleanLiveBoundaryInput.mInputBufferGray, cCameraCleanLiveBoundaryInput.mWidth, cCameraCleanLiveBoundaryInput.mHeight);
        DocDetectionUtils.CameraCleanDocDetectionInput cameraCleanDocDetectionInput = new DocDetectionUtils.CameraCleanDocDetectionInput(rGBABitmapFromYUVBuffer);
        cameraCleanDocDetectionInput.mIsLiveCaptureCase = true;
        DocDetectionUtils.CameraCleanDocDetectionOutput cameraCleanDocDetectionOutputDetectDocType = cameraCleanDocDetectionAndroidShim.DetectDocType(cameraCleanDocDetectionInput);
        rGBABitmapFromYUVBuffer.recycle();
        if (cameraCleanDocDetectionOutputDetectDocType != null && cameraCleanDocDetectionOutputDetectDocType.mDocType == DocDetectionUtils.DetectedDocType.kDetectedDocTypeDocument) {
            cCameraCleanLiveBoundaryInput.mLivePreviewContainsDoc = true;
        }
        MagicCleanLog.d(LOG_TAG, "getLiveCornersGray_LivePreviewContainsDocument: " + (System.currentTimeMillis() - jCurrentTimeMillis));
    }

    private void RestoreToDefaults() {
        this.mPrevCorners = new CCornersInfo();
        this.mPrevLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintLookingForDocument;
        this.mPrevLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOff;
        this.mPrevDetectionResult = false;
        this.mBoundaryConfidence = 0;
        this.mBoundaryConfidenceForDisplayThreshold = 1;
        this.mBoundaryConfidenceForCaptureThreshold = 3;
        this.mIsLowContrastBD = false;
        this.mNumInputFramesBD = 0;
        this.mLiveBoundaryTimeKeeperCounter = 0;
        this.mLiveBoundaryCaptureEnableCounter = 0;
        this.mLiveBoundaryUpdateEnabled = true;
        this.mLiveBoundaryHintUpdateEnabled = true;
        this.mLiveBoundaryCaptureEnabled = false;
        this.mLiveBoundaryTriggerCapture = false;
        int i = this.mSoftResetCount;
        this.mCaptureConditionsAnalyzed = i >= 2;
        this.mLowLightAnalyzed = i >= 2;
        this.mGlareAnalyzed = true;
        this.mHardShadowsAnalyzed = true;
        this.mImageBufferTorchOff = null;
        this.mImageTorchOffWidth = 0;
        this.mImageTorchOffHeight = 0;
        this.mCornersTorchOff = null;
        this.mImageBufferTorchOn = null;
        this.mImageTorchOnWidth = 0;
        this.mImageTorchOnHeight = 0;
        this.mCornersTorchOn = null;
        this.mTorchOnFramesCount = 0;
        this.mTorchOffFramesCount = 0;
        this.mGlareAnalysisTimeoutCounter = 0;
        this.mLowBrightnessFrameCount = 0;
        this.mStableBoundaryConfidence = 0;
        this.mLastStableCornerInfo = new CCornersInfo();
    }

    private void SetAppropriateHint(CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) {
        boolean z;
        boolean z2 = true;
        if (CanSetHoldForCaptureHint(cCameraCleanLiveBoundaryOutput)) {
            cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintHoldForCapture;
            z = true;
        } else {
            z = false;
        }
        if (z && CanSetReadyForCaptureHint(cCameraCleanLiveBoundaryOutput)) {
            cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintReadyForCapture;
        } else {
            z2 = z;
        }
        if (z2) {
            return;
        }
        cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintNone;
    }

    private boolean WithinTolerance(CCornersInfo cCornersInfo, CCornersInfo cCornersInfo2, double d) {
        return ((double) Math.abs(cCornersInfo.getPointsRef()[0].x - cCornersInfo2.getPointsRef()[0].x)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[0].y - cCornersInfo2.getPointsRef()[0].y)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[1].x - cCornersInfo2.getPointsRef()[1].x)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[1].y - cCornersInfo2.getPointsRef()[1].y)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[2].x - cCornersInfo2.getPointsRef()[2].x)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[2].y - cCornersInfo2.getPointsRef()[2].y)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[3].x - cCornersInfo2.getPointsRef()[3].x)) <= d && ((double) Math.abs(cCornersInfo.getPointsRef()[3].y - cCornersInfo2.getPointsRef()[3].y)) <= d;
    }

    /* JADX WARN: Code duplicated, block: B:4:0x0017 A[PHI: r2
  0x0017: PHI (r2v3 double) = (r2v1 double), (r2v2 double) binds: [B:3:0x0015, B:6:0x0020] A[DONT_GENERATE, DONT_INLINE]] */
    private Bitmap getScaledBitmap(Bitmap bitmap, int i, int i2) {
        double dFloor = Math.floor(Math.sqrt(((double) (i * i2)) / 76800.0d));
        double d = 1.0d;
        if (dFloor < 1.0d) {
            dFloor = d;
        } else {
            d = 255.0d;
            if (dFloor > 255.0d) {
                dFloor = d;
            }
        }
        int i3 = (int) dFloor;
        return Bitmap.createScaledBitmap(bitmap, i / i3, i2 / i3, false);
    }

    private native boolean imageHasDarkShadow(Bitmap bitmap, PointF[] pointFArr);

    private native boolean imageHasGlareAfterTorch(Bitmap bitmap, PointF[] pointFArr, Bitmap bitmap2, PointF[] pointFArr2);

    private void resetLiveBoundary(boolean z) {
        if (!z) {
            this.mSoftResetCount = 0;
        } else if (this.mImageBufferTorchOff != null || this.mImageBufferTorchOn != null) {
            this.mSoftResetCount++;
        }
        RestoreToDefaults();
    }

    public void AdjustDocClassificationConfidence() {
        int i = this.mDocClassificationConfidence;
        if (i > 7) {
            this.mDocClassificationConfidence = i >> 1;
        } else if (i > 0) {
            this.mDocClassificationConfidence = i - 1;
        }
    }

    public void cleanup() {
        this.mLiveBoundaryTimeKeeper.cancel();
    }

    public synchronized boolean getLiveCornersGray(CCameraCleanLiveBoundaryInput cCameraCleanLiveBoundaryInput, CCameraCleanLiveBoundaryOutput cCameraCleanLiveBoundaryOutput) throws Throwable {
        Throwable th;
        boolean z;
        CameraCleanLiveEdgeDetectionAndroidShim cameraCleanLiveEdgeDetectionAndroidShim;
        try {
            try {
                long jCurrentTimeMillis = System.currentTimeMillis();
                cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mPrevCorners;
                cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = this.mPrevLiveBoundaryHint;
                cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = this.mPrevLiveBoundaryTorchHint;
                cCameraCleanLiveBoundaryOutput.mIsLowContrast = this.mIsLowContrastBD;
                cCameraCleanLiveBoundaryOutput.mFrames = cCameraCleanLiveBoundaryInput.mFrameNumber;
                if (cCameraCleanLiveBoundaryInput.mDocSelectorType == CameraCleanUtils.DocSelectorType.kDocSelectorTypeBook) {
                    try {
                        this.mTimeThresholdToResetBoundaryConfidenceHigh = 0.5d;
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                if (cCameraCleanLiveBoundaryInput.mInputBufferGray != null) {
                    boolean zIgnoreBoundaryChangesDueToTorchOn = IgnoreBoundaryChangesDueToTorchOn(cCameraCleanLiveBoundaryInput);
                    if (GetBoundariesInLivePreview(cCameraCleanLiveBoundaryInput, cCameraCleanLiveBoundaryOutput, zIgnoreBoundaryChangesDueToTorchOn)) {
                        this.mBoundaryConfidence++;
                        if (AreCurrentCornersWithinTolerance(cCameraCleanLiveBoundaryOutput, this.mCornerToleranceLimit, zIgnoreBoundaryChangesDueToTorchOn) && this.mBoundaryConfidence >= this.mBoundaryConfidenceForDisplayThreshold) {
                            AnalyzeCaptureExperienceParams(cCameraCleanLiveBoundaryInput, cCameraCleanLiveBoundaryOutput);
                            if (this.mLiveBoundaryHintUpdateEnabled) {
                                SetAppropriateHint(cCameraCleanLiveBoundaryOutput);
                                this.mLiveBoundaryHintUpdateEnabled = false;
                            }
                            CCornersInfo cCornersInfo = cCameraCleanLiveBoundaryOutput.mCornersInfo;
                            cCameraCleanLiveBoundaryOutput.mOriginalCornersInfo = cCornersInfo;
                            if (this.mLiveBoundaryUpdateEnabled) {
                                this.mLiveBoundaryUpdateEnabled = false;
                                if (this.mTimeTakenInGetLiveCorners < this.mTimeThresholdToResetBoundaryConfidenceHigh) {
                                    if (this.mStableBoundaryConfidence > 0 && !WithinTolerance(this.mLastStableCornerInfo, cCornersInfo, 0.1d)) {
                                        cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mLastStableCornerInfo;
                                        this.mLiveBoundaryUpdateEnabled = true;
                                    }
                                    this.mStableBoundaryConfidence = 0;
                                }
                                cCameraCleanLiveBoundaryOutput.mIsBoundaryUpdated = true;
                            } else {
                                if (this.mTimeTakenInGetLiveCorners < this.mTimeThresholdToResetBoundaryConfidenceHigh) {
                                    boolean zWithinTolerance = WithinTolerance(this.mLastStableCornerInfo, cCornersInfo, 0.1d);
                                    int i = this.mStableBoundaryConfidence;
                                    if (zWithinTolerance) {
                                        this.mStableBoundaryConfidence = i + 1;
                                        this.mLastStableCornerInfo = cCameraCleanLiveBoundaryOutput.mCornersInfo;
                                    } else {
                                        int i2 = i - 1;
                                        this.mStableBoundaryConfidence = i2;
                                        if (i2 <= 0) {
                                            this.mStableBoundaryConfidence = 0;
                                            this.mLastStableCornerInfo = cCameraCleanLiveBoundaryOutput.mCornersInfo;
                                        }
                                    }
                                }
                                cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mPrevCorners;
                                cCameraCleanLiveBoundaryOutput.mIsBoundaryUpdated = false;
                            }
                        }
                    } else {
                        AdjustCurrentBoundaryConfidence();
                        if (this.mBoundaryConfidence == 0) {
                        }
                    }
                } else {
                    AdjustCurrentBoundaryConfidence();
                    z = this.mBoundaryConfidence == 0;
                }
                boolean z2 = this.mPrevDetectionResult;
                if (z) {
                    if (this.mLiveBoundaryUpdateEnabled) {
                        cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintNone;
                        cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOff;
                        resetLiveBoundary(true);
                    } else {
                        cCameraCleanLiveBoundaryOutput.mCornersInfo = this.mPrevCorners;
                        cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = this.mPrevLiveBoundaryHint;
                        cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = this.mPrevLiveBoundaryTorchHint;
                    }
                }
                boolean z3 = this.mBoundaryConfidence >= this.mBoundaryConfidenceForDisplayThreshold;
                if (z3) {
                    if (cCameraCleanLiveBoundaryOutput.mIsBoundaryUpdated) {
                        this.mDisplayedCornersInfo.add(cCameraCleanLiveBoundaryOutput.mCornersInfo);
                    }
                    this.mAllCornersInfo.add(cCameraCleanLiveBoundaryOutput.mCornersInfo);
                }
                if (z2 && !z3) {
                    this.mNumInvalidations++;
                }
                if (!z3) {
                    cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint = LiveBoundaryHint.kLiveBoundaryHintLookingForDocument;
                    cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint = LiveBoundaryTorchHint.kLiveBoundaryTorchHintTorchOff;
                }
                this.mPrevCorners = cCameraCleanLiveBoundaryOutput.mCornersInfo;
                this.mPrevSensorOffset = cCameraCleanLiveBoundaryInput.mSenserOffset;
                this.mPrevDetectionResult = z3;
                this.mPrevLiveBoundaryHint = cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint;
                this.mPrevLiveBoundaryTorchHint = cCameraCleanLiveBoundaryOutput.mLiveBoundaryTorchHint;
                int i3 = this.mNumInputFramesBD + 1;
                this.mNumInputFramesBD = i3;
                this.mNumInputFrames++;
                if (!z3 && i3 > this.mBoundaryConfidenceForCaptureThreshold) {
                    this.mIsLowContrastBD = false;
                    this.mNumInputFramesBD = 0;
                }
                if (this.mDisplayedCornersInfo.size() > 0 && z3 && cCameraCleanLiveBoundaryOutput.mLiveBoundaryHint == LiveBoundaryHint.kLiveBoundaryHintReadyForCapture) {
                    CameraCleanUtils.CropConfidence cropConfidence = this.mInCropConfidence;
                    if (cropConfidence == null) {
                        cropConfidence = new CameraCleanUtils.CropConfidence();
                    }
                    int i4 = cropConfidence.mNumInvalidations;
                    double d = cropConfidence.mLastStableFrames;
                    double d2 = cropConfidence.mCornersAreCloseThreshold;
                    cameraCleanLiveEdgeDetectionAndroidShim = this;
                    int iCalculateCropConfidence = cameraCleanLiveEdgeDetectionAndroidShim.CalculateCropConfidence(this.mDisplayedCornersInfo, i4, d, d, d2);
                    cCameraCleanLiveBoundaryOutput.mOutCropConfidence = iCalculateCropConfidence;
                    if (iCalculateCropConfidence == 0) {
                        cCameraCleanLiveBoundaryOutput.mOutCropConfidence = cameraCleanLiveEdgeDetectionAndroidShim.CalculateCropConfidence(cameraCleanLiveEdgeDetectionAndroidShim.mAllCornersInfo, i4, d, d, d2);
                    }
                } else {
                    cameraCleanLiveEdgeDetectionAndroidShim = this;
                }
                cameraCleanLiveEdgeDetectionAndroidShim.mTimeTakenInGetLiveCorners = (System.currentTimeMillis() - jCurrentTimeMillis) / 1000.0d;
                if (cameraCleanLiveEdgeDetectionAndroidShim.mNumInputFramesBD > 0) {
                    cameraCleanLiveEdgeDetectionAndroidShim.AdjustBoundaryConfidenceThresholds();
                }
                cCameraCleanLiveBoundaryOutput.mTimeTaken = System.currentTimeMillis() - jCurrentTimeMillis;
                cCameraCleanLiveBoundaryOutput.mBoundaryConfidence = cameraCleanLiveEdgeDetectionAndroidShim.mBoundaryConfidence;
                cCameraCleanLiveBoundaryOutput.mBoundaryDisplayThreshold = cameraCleanLiveEdgeDetectionAndroidShim.mBoundaryConfidenceForDisplayThreshold;
                cCameraCleanLiveBoundaryOutput.mBoundaryCaptureThreshold = cameraCleanLiveEdgeDetectionAndroidShim.mBoundaryConfidenceForCaptureThreshold;
                return z3;
            } catch (Throwable th3) {
                th = th3;
                th = th;
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            th = th;
            throw th;
        }
        throw th;
    }

    public void resetCropConfidenceParams() {
        this.mAllCornersInfo.clear();
        this.mDisplayedCornersInfo.clear();
        this.mNumInvalidations = 0;
    }

    public void setBoundaryConfidenceThreshold(int i) {
        this.mBoundaryConfidenceForDisplayThreshold = i;
    }

    public void setCornerToleranceLimit(double d) {
        this.mCornerToleranceLimit = d;
    }

    public void setCropConfidenceThresholds(CameraCleanUtils.CropConfidence cropConfidence) {
        this.mInCropConfidence = cropConfidence;
    }

    public void setDisplayedBoundaryUpdateInterval(long j) {
        this.mLiveBoundaryUpdateInterval = j;
    }

    public void setEnableBetaFeatures(boolean z) {
        this.mEnableBetaFeatures = z;
    }

    public void setLiveBoundaryCaptureEnableInterval(long j) {
        this.mLiveBoundaryCaptureEnableInterval = j;
    }

    public void setLiveBoundaryHintUpdateInterval(long j) {
        this.mLiveBoundaryHintUpdateInterval = j;
    }

    public void resetLiveBoundary() {
        resetLiveBoundary(false);
        this.mDocClassification = DocClassificationUtils.DocClassification.kDocClassificationEnd;
        this.mDocClassificationConfidence = 0;
        this.mNumInputFrames = 0;
    }

    /* JADX INFO: loaded from: classes3.dex */
    public static class CCameraCleanLiveBoundaryInput {
        private static final String FLASH_MODE_AUTO = "auto";
        private static final String FLASH_MODE_OFF = "off";
        private static final String FLASH_MODE_ON = "on";
        private static final String FLASH_MODE_TORCH = "torch";
        public String mActiveFlashMode;
        private boolean mAnalyzeCaptureConditions;
        public double mBrightnessFromCamera;
        public CameraCleanUtils.DocSelectorType mDocSelectorType;
        public int mFrameNumber;
        public int mHeight;
        public byte[] mInputBufferGray;
        public boolean mIsAutoCaptureEnabled;
        public boolean mIsTorchActive;
        private boolean mLivePreviewContainsDoc;
        public int mSenserOffset;
        public int mWidth;

        public CCameraCleanLiveBoundaryInput(byte[] bArr, int i, int i2, int i3, boolean z, boolean z2, String str, double d, CameraCleanUtils.DocSelectorType docSelectorType) {
            this.mInputBufferGray = bArr;
            this.mWidth = i;
            this.mHeight = i2;
            this.mSenserOffset = i3;
            this.mIsTorchActive = z;
            this.mIsAutoCaptureEnabled = z2;
            this.mActiveFlashMode = str;
            this.mBrightnessFromCamera = d;
            this.mDocSelectorType = docSelectorType;
            this.mFrameNumber = 0;
            this.mAnalyzeCaptureConditions = z2 && (str.equals(FLASH_MODE_AUTO) || this.mActiveFlashMode.equals(FLASH_MODE_TORCH));
            this.mLivePreviewContainsDoc = false;
        }

        public CCameraCleanLiveBoundaryInput(byte[] bArr, int i, int i2, int i3, boolean z, boolean z2, String str, double d, CameraCleanUtils.DocSelectorType docSelectorType, int i4) {
            this.mInputBufferGray = bArr;
            this.mWidth = i;
            this.mHeight = i2;
            this.mSenserOffset = i3;
            this.mIsTorchActive = z;
            this.mIsAutoCaptureEnabled = z2;
            this.mActiveFlashMode = str;
            this.mBrightnessFromCamera = d;
            this.mDocSelectorType = docSelectorType;
            this.mFrameNumber = i4;
            this.mAnalyzeCaptureConditions = z2 && (str.equals(FLASH_MODE_AUTO) || this.mActiveFlashMode.equals(FLASH_MODE_TORCH));
            this.mLivePreviewContainsDoc = false;
        }
    }
}
