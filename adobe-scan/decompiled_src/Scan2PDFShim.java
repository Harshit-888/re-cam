package com.adobe.scan2pdf;

/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes3.dex */
public class Scan2PDFShim {
    private static final boolean isLibraryLoaded = Scan2PDFLibrary.isLoaded;

    public Scan2PDFUtils.Scan2PDFResult CreatePDF(Scan2PDFUtils.CreatePDFInput createPDFInput, Scan2PDFUtils.CreatePDFOutput createPDFOutput) {
        return !isLibraryLoaded ? Scan2PDFUtils.Scan2PDFResult.kScan2PDFResultUnknownFailure : CreatePDFNative(createPDFInput, createPDFOutput);
    }

    public native Scan2PDFUtils.Scan2PDFResult CreatePDFNative(Scan2PDFUtils.CreatePDFInput createPDFInput, Scan2PDFUtils.CreatePDFOutput createPDFOutput);

    public Scan2PDFUtils.Scan2PDFResult CreatePDFTextStream(Scan2PDFUtils.CreatePDFTextStreamInput createPDFTextStreamInput, Scan2PDFUtils.CreatePDFTextStreamOutput createPDFTextStreamOutput) {
        return !isLibraryLoaded ? Scan2PDFUtils.Scan2PDFResult.kScan2PDFResultUnknownFailure : CreatePDFTextStreamNative(createPDFTextStreamInput, createPDFTextStreamOutput);
    }

    public native Scan2PDFUtils.Scan2PDFResult CreatePDFTextStreamNative(Scan2PDFUtils.CreatePDFTextStreamInput createPDFTextStreamInput, Scan2PDFUtils.CreatePDFTextStreamOutput createPDFTextStreamOutput);
}
