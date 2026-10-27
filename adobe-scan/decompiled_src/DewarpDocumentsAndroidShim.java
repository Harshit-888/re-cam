package com.adobe.magic_clean;

/* JADX INFO: loaded from: classes.dex */
public class DewarpDocumentsAndroidShim {
    static {
        System.loadLibrary("MagicClean");
    }

    private native CameraCleanUtils.CameraResult addWhitePaddingToImage(CameraCleanUtils.WhitePadInput whitePadInput, CameraCleanUtils.WhitePadOutput whitePadOutput);

    private native CameraCleanUtils.CameraResult computeDocumentCurveForDewarping(CameraCleanUtils.DewarpCurveInput dewarpCurveInput, CameraCleanUtils.DewarpCurveOutput dewarpCurveOutput);

    private native CameraCleanUtils.CameraResult runDocumentDewarping(CameraCleanUtils.DewarpInput dewarpInput, CameraCleanUtils.DewarpOutput dewarpOutput, boolean z);

    public CameraCleanUtils.CameraResult AddWhitePaddingToImage(CameraCleanUtils.WhitePadInput whitePadInput, CameraCleanUtils.WhitePadOutput whitePadOutput) {
        if (whitePadInput.mInputBmp != null) {
            return addWhitePaddingToImage(whitePadInput, whitePadOutput);
        }
        CameraCleanUtils.CameraResult cameraResult = CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        whitePadOutput.mErrorCode = cameraResult;
        return cameraResult;
    }

    public CameraCleanUtils.CameraResult ComputeDocumentCurveForDewarping(CameraCleanUtils.DewarpCurveInput dewarpCurveInput, CameraCleanUtils.DewarpCurveOutput dewarpCurveOutput) {
        if (dewarpCurveInput.mInputBmpOriginalSize == null) {
            CameraCleanUtils.CameraResult cameraResult = CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
            dewarpCurveOutput.mErrorCode = cameraResult;
            return cameraResult;
        }
        CameraCleanUtils.DocSelectorType docSelectorType = dewarpCurveInput.mDocSelectorType;
        if (docSelectorType == CameraCleanUtils.DocSelectorType.kDocSelectorTypeDocument || docSelectorType == CameraCleanUtils.DocSelectorType.kDocSelectorTypeBook) {
            return computeDocumentCurveForDewarping(dewarpCurveInput, dewarpCurveOutput);
        }
        CameraCleanUtils.CameraResult cameraResult2 = CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        dewarpCurveOutput.mErrorCode = cameraResult2;
        return cameraResult2;
    }

    public CameraCleanUtils.CameraResult RunDocumentDewarping(CameraCleanUtils.DewarpInput dewarpInput, CameraCleanUtils.DewarpOutput dewarpOutput, boolean z) {
        if (dewarpInput.mDewarpCurveInput.mInputBmpOriginalSize != null) {
            return runDocumentDewarping(dewarpInput, dewarpOutput, z);
        }
        CameraCleanUtils.CameraResult cameraResult = CameraCleanUtils.CameraResult.kCameraResultInvalidParameter;
        dewarpOutput.mErrorCode = cameraResult;
        return cameraResult;
    }

    public CameraCleanUtils.CameraResult RunDocumentDewarping(CameraCleanUtils.DewarpInput dewarpInput, CameraCleanUtils.DewarpOutput dewarpOutput) {
        return RunDocumentDewarping(dewarpInput, dewarpOutput, true);
    }
}
