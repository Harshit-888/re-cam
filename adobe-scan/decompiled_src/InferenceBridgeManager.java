package com.adobe.magic_clean.inference;

import android.util.Log;

/* JADX INFO: compiled from: InferenceBridgeManager.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes3.dex */
public final class InferenceBridgeManager {
    public static final InferenceBridgeManager INSTANCE = new InferenceBridgeManager();
    private static final String TAG = "MC_InferenceBridge";

    /* JADX INFO: compiled from: InferenceBridgeManager.kt */
    public static final class ModelIds {
        public static final ModelIds INSTANCE = new ModelIds();
        public static final int OBJECT_REMOVAL = 1;

        private ModelIds() {
        }
    }

    /* JADX INFO: compiled from: InferenceBridgeManager.kt */
    public static final class NativeErrorCode {
        public static final int INFERENCE_FAILED = 3;
        public static final NativeErrorCode INSTANCE = new NativeErrorCode();
        public static final int INVALID_INPUT = 4;
        public static final int MODEL_LOAD_FAILED = 1;
        public static final int MODEL_NOT_LOADED = 2;
        public static final int NONE = 0;
        public static final int OUTPUT_INVALID = 5;
        public static final int UNKNOWN = 6;

        private NativeErrorCode() {
        }
    }

    static {
        d6m aVar;
        try {
            System.loadLibrary("MagicClean");
            aVar = d6m.a;
        } catch (Throwable th) {
            aVar = new a(th);
        }
        Throwable thA = lbh.a(aVar);
        if (thA != null) {
            Log.w(TAG, "init: failed to load MagicClean library", thA);
        }
    }

    private InferenceBridgeManager() {
    }

    private final native String nativeBridgeGetAccelerator(int i);

    private final native int nativeBridgeGetLastError(int i);

    private final native float[] nativeBridgeInfer(int i, float[] fArr);

    private final native String nativeBridgeLoad(int i, int i2, String str);

    private final native void nativeBridgeUnload(int i);

    public final String getActiveAccelerator(int i) {
        try {
            String strNativeBridgeGetAccelerator = nativeBridgeGetAccelerator(i);
            return strNativeBridgeGetAccelerator == null ? "none" : strNativeBridgeGetAccelerator;
        } catch (Exception e) {
            Log.e(TAG, "getActiveAccelerator(" + i + ") failed", e);
            return "none";
        }
    }

    public final int getLastError(int i) {
        try {
            return nativeBridgeGetLastError(i);
        } catch (Exception e) {
            Log.e(TAG, "getLastError(" + i + ") failed", e);
            return 6;
        }
    }

    public final String loadModel(int i, int i2, String str) {
        str.getClass();
        try {
            return nativeBridgeLoad(i, i2, str);
        } catch (Exception e) {
            Log.e(TAG, "loadModel(" + i + ") failed", e);
            return null;
        }
    }

    public final float[] runInference(int i, float[] fArr) {
        fArr.getClass();
        try {
            return nativeBridgeInfer(i, fArr);
        } catch (Exception e) {
            Log.e(TAG, "runInference(" + i + ") failed", e);
            return null;
        }
    }

    public final void unloadModel(int i) {
        try {
            nativeBridgeUnload(i);
        } catch (Exception e) {
            Log.e(TAG, "unloadModel(" + i + ") failed", e);
        }
    }
}
