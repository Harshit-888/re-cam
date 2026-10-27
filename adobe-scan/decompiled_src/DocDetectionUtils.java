package com.adobe.magic_clean;

import android.graphics.Bitmap;

/* JADX INFO: loaded from: classes3.dex */
public class DocDetectionUtils {
    public static int kImageDimensionsForDocDetectionNN = 128;
    public static int kMinInputImageSizeForDocDetectionNN = 307200;

    public static class CameraCleanDocDetectionInput {
        public Bitmap mInputBitmap;
        public boolean mIsLiveCaptureCase = false;

        public CameraCleanDocDetectionInput(Bitmap bitmap) {
            this.mInputBitmap = bitmap;
        }
    }

    public enum DetectedDocType {
        kDetectedDocTypeNone,
        kDetectedDocTypeDocument,
        kDetectedDocTypeNotDocument,
        kDetectedDocTypeFuzzy
    }

    public enum DocDetectionErrCode {
        kDocDetectionErrCodeSuccess,
        kDocDetectionErrCodeFailure
    }

    public static class CameraCleanDocDetectionOutput {
        public DetectedDocType mDocType;
        public DocDetectionErrCode mError;

        public CameraCleanDocDetectionOutput(DetectedDocType detectedDocType, DocDetectionErrCode docDetectionErrCode) {
            this.mDocType = detectedDocType == null ? DetectedDocType.kDetectedDocTypeNone : detectedDocType;
            this.mError = docDetectionErrCode == null ? DocDetectionErrCode.kDocDetectionErrCodeSuccess : docDetectionErrCode;
        }

        public CameraCleanDocDetectionOutput() {
            this.mDocType = DetectedDocType.kDetectedDocTypeNone;
            this.mError = DocDetectionErrCode.kDocDetectionErrCodeSuccess;
        }
    }
}
