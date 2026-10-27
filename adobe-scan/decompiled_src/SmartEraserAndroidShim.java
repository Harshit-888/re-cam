package com.adobe.magic_clean;

import kotlin.jvm.internal.DefaultConstructorMarker;

/* JADX INFO: compiled from: SmartEraserAndroidShim.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class SmartEraserAndroidShim {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);

    static {
        System.loadLibrary("MagicClean");
    }

    private final native CameraCleanUtils.CameraResult initializeSmartEraser(CameraCleanUtils.EraserInput eraserInput, CameraCleanUtils.EraserOutput eraserOutput);

    public final CameraCleanUtils.CameraResult Initialize(CameraCleanUtils.EraserInput eraserInput, CameraCleanUtils.EraserOutput eraserOutput) {
        eraserInput.getClass();
        eraserOutput.getClass();
        if (eraserInput.mInputBmp != null) {
            return initializeSmartEraser(eraserInput, eraserOutput);
        }
        CameraCleanUtils.CameraResult cameraResult = CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        eraserOutput.mErrorCode = cameraResult;
        cameraResult.getClass();
        return cameraResult;
    }
}
