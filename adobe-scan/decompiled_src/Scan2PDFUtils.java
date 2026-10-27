package com.adobe.scan2pdf;

import com.adobe.mobileocrandroidhelper.OCRResult;
import java.util.ArrayList;

/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes3.dex */
public class Scan2PDFUtils {

    public static class CreatePDFInput {
        byte[] mImageBuffer;
        int mImageHeight;
        int mImageWidth;
        ArrayList<OCRResult.OCRRegionResults> mOCRRegionResults;
        String mPDFFilePath;
        int mPageHeight;
        int mPageWidth;

        public CreatePDFInput(byte[] bArr, int i, int i2, int i3, int i4, ArrayList<OCRResult.OCRRegionResults> arrayList, String str) {
            this.mImageBuffer = bArr;
            this.mImageHeight = i;
            this.mImageWidth = i2;
            this.mPageHeight = i3;
            this.mPageWidth = i4;
            this.mOCRRegionResults = arrayList;
            this.mPDFFilePath = str;
        }
    }

    public static class CreatePDFOutput {
        public String mPDFTextStream = null;
    }

    public static class CreatePDFTextStreamInput {
        int mImageHeight;
        int mImageWidth;
        ArrayList<OCRResult.OCRRegionResults> mOCRRegionResults;
        int mPageHeight;
        int mPageWidth;

        public CreatePDFTextStreamInput(int i, int i2, int i3, int i4, ArrayList<OCRResult.OCRRegionResults> arrayList) {
            this.mImageHeight = i;
            this.mImageWidth = i2;
            this.mPageHeight = i3;
            this.mPageWidth = i4;
            this.mOCRRegionResults = arrayList;
        }
    }

    public static class CreatePDFTextStreamOutput {
        public String mPDFTextStream = null;
    }

    public enum Scan2PDFResult {
        kScan2PDFResultSuccess,
        kScan2PDFResultUnknownFailure,
        kScan2PDFResultInvalidParameter,
        kScan2PDFResultMemoryFailure
    }
}
