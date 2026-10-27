package defpackage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.Base64;
import android.util.Log;
import com.adobe.dcmscan.y;
import com.adobe.magic_clean.CCornersInfo;
import com.adobe.magic_clean.CameraCleanAndroidShim;
import com.adobe.magic_clean.CameraCleanDocDetectionAndroidShim;
import com.adobe.magic_clean.CameraCleanUtils;
import com.adobe.magic_clean.DocDetectionUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: compiled from: MagicCleanEdgeDetection.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class m8d implements nab {
    public static final wt8 c;
    public static final wt8 d;
    public static final wt8 e;
    public static final wt8 f;
    public static final List<String> g;
    public static final int h;
    public static final int i;
    public static final int j;
    public static final int k;
    public final b3l a = new b3l(new x7d());
    public final b3l b = new b3l(new y7d());

    /* JADX INFO: compiled from: MagicCleanEdgeDetection.kt */
    @r67(c = "com.adobe.dcmscan.algorithms.MagicCleanEdgeDetection$setupSenseiModels$1", f = "MagicCleanEdgeDetection.kt", l = {}, m = "invokeSuspend", v = 2)
    public static final class a extends p0l implements u5a<s86, u36<? super d6m>, Object> {
        public a(u36<? super a> u36Var) {
            super(2, u36Var);
        }

        @Override // defpackage.k73
        public final u36<d6m> create(Object obj, u36<?> u36Var) {
            return m8d.this.new a(u36Var);
        }

        @Override // defpackage.u5a
        public final Object invoke(s86 s86Var, u36<? super d6m> u36Var) {
            return ((a) create(s86Var, u36Var)).invokeSuspend(d6m.a);
        }

        @Override // defpackage.k73
        public final Object invokeSuspend(Object obj) {
            u86 u86Var = u86.COROUTINE_SUSPENDED;
            pbh.b(obj);
            wt8 wt8Var = m8d.c;
            String str = "setupSenseiModels() called on " + Thread.currentThread();
            if (zvi.a) {
                Log.d("m8d", str);
            }
            for (String str2 : m8d.g) {
                String str3 = "makeNNModelsDir: created " + str2 + ": " + new File(ihi.a().getCacheDir(), str2).mkdir();
                if (zvi.a) {
                    Log.d("m8d", str3);
                }
            }
            try {
                File cacheDir = ihi.a().getCacheDir();
                CameraCleanUtils.setBasePathToModels(cacheDir != null ? cacheDir.getPath() : null);
            } catch (Error e) {
                n8d.b(e);
            }
            return d6m.a;
        }
    }

    static {
        ExecutorService executorServiceNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor.getClass();
        c = new wt8(executorServiceNewSingleThreadExecutor);
        ExecutorService executorServiceNewSingleThreadExecutor2 = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor2.getClass();
        d = new wt8(executorServiceNewSingleThreadExecutor2);
        ExecutorService executorServiceNewSingleThreadExecutor3 = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor3.getClass();
        e = new wt8(executorServiceNewSingleThreadExecutor3);
        ExecutorService executorServiceNewSingleThreadExecutor4 = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor4.getClass();
        f = new wt8(executorServiceNewSingleThreadExecutor4);
        g = r75.h("bd", "sr", "dd", "dc", "bbd", "dw");
        h = 6;
        i = 2;
        j = 4;
        k = 8;
    }

    public static String k(CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput, CCornersInfo[] cCornersInfoArr) {
        CameraCleanUtils.CMLAnalyticsOutput cMLAnalyticsOutput = edgeDetectionOutput.mMLAnalyticsOutput;
        if (!cMLAnalyticsOutput.isValid) {
            return null;
        }
        byte[][] bArr = cMLAnalyticsOutput.mBufferForKeys;
        bArr.getClass();
        if (bArr.length == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr2 = edgeDetectionOutput.mMLAnalyticsOutput.mBufferForKeys[0];
        int iN = (n(4, bArr2) + n(0, bArr2) + 1) * 2;
        byte[][] bArr3 = edgeDetectionOutput.mMLAnalyticsOutput.mBufferForKeys;
        bArr3.getClass();
        for (byte[] bArr4 : bArr3) {
            int length = bArr4.length;
            if (iN <= length) {
                length = iN;
            }
            byteArrayOutputStream.write(bArr4, 0, length);
            iN -= length;
        }
        int[] iArr = new int[8];
        if (cCornersInfoArr.length != 0) {
            PointF[] pointsRef = cCornersInfoArr[0].getPointsRef();
            int length2 = pointsRef.length;
            for (int i2 = 0; i2 < length2; i2++) {
                int i3 = i2 * 2;
                PointF pointF = pointsRef[i2];
                iArr[i3] = (int) (pointF.x * 256.0f);
                iArr[i3 + 1] = (int) (pointF.y * 256.0f);
            }
        }
        for (int i4 = 0; i4 < 8; i4++) {
            int i5 = iArr[i4];
            byteArrayOutputStream.write(i5 >> 8);
            byteArrayOutputStream.write(i5);
        }
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), 2);
    }

    public static int m(int i2) {
        if (i2 == 0) {
            return 0;
        }
        if (i2 == 1) {
            return i;
        }
        if (i2 == 2) {
            return j;
        }
        if (i2 == 3) {
            return h;
        }
        if (i2 != 4) {
            return 0;
        }
        return k;
    }

    public static int n(int i2, byte[] bArr) {
        byte b = 0;
        if (bArr == null) {
            return 0;
        }
        int i3 = (((i2 < 0 || i2 >= bArr.length) ? (byte) 0 : bArr[i2]) & 255) << 8;
        int i4 = i2 + 1;
        if (i4 >= 0 && i4 < bArr.length) {
            b = bArr[i4];
        }
        return i3 + (b & 255);
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0015  */
    @Override // defpackage.nab
    public final Object a(Bitmap bitmap, Bitmap bitmap2, int i2, nab.c cVar, v36 v36Var) throws Throwable {
        g8d g8dVar;
        boolean zB;
        Exception exc;
        x1k x1kVar;
        Error error;
        CameraCleanUtils.FilterThumbnailOutput filterThumbnailOutput;
        if (v36Var instanceof g8d) {
            g8dVar = (g8d) v36Var;
            int i3 = g8dVar.x;
            if ((i3 & Integer.MIN_VALUE) != 0) {
                g8dVar.x = i3 - Integer.MIN_VALUE;
            } else {
                g8dVar = new g8d(this, v36Var);
            }
        } else {
            g8dVar = new g8d(this, v36Var);
        }
        Object obj = g8dVar.v;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i4 = g8dVar.x;
        Bitmap bitmap3 = null;
        if (i4 == 0) {
            pbh.b(obj);
            x1k x1kVar2 = new x1k();
            boolean z = cVar != null;
            if (cVar != null) {
                try {
                    zB = cVar.b();
                } catch (Error e2) {
                    error = e2;
                    x1kVar = x1kVar2;
                    n8d.b(error);
                } catch (Exception e3) {
                    exc = e3;
                    x1kVar = x1kVar2;
                    h7f.a(exc);
                    Log.e("MagicCleanEdgeDetection", "Exception has been caught", exc);
                }
            } else {
                zB = false;
            }
            int iC = cVar != null ? cVar.c() : 0;
            Object objA = cVar != null ? cVar.a() : null;
            CameraCleanUtils.LABColorPixel lABColorPixel = objA instanceof CameraCleanUtils.LABColorPixel ? (CameraCleanUtils.LABColorPixel) objA : null;
            if (lABColorPixel == null) {
                lABColorPixel = new CameraCleanUtils.LABColorPixel();
            }
            CameraCleanUtils.FilterThumbnailInput filterThumbnailInput = new CameraCleanUtils.FilterThumbnailInput(bitmap, bitmap2, m(i2), zB, iC, z, lABColorPixel);
            CameraCleanUtils.FilterThumbnailOutput filterThumbnailOutput2 = new CameraCleanUtils.FilterThumbnailOutput();
            wt8 wt8Var = f;
            h8d h8dVar = new h8d(this, filterThumbnailInput, filterThumbnailOutput2, (u36) null);
            g8dVar.t = x1kVar2;
            g8dVar.u = filterThumbnailOutput2;
            g8dVar.x = 1;
            Object objG = km3.g(wt8Var, h8dVar, g8dVar);
            if (objG == u86Var) {
                return u86Var;
            }
            obj = objG;
            filterThumbnailOutput = filterThumbnailOutput2;
            x1kVar = x1kVar2;
        } else {
            if (i4 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            filterThumbnailOutput = g8dVar.u;
            x1kVar = g8dVar.t;
            try {
                pbh.b(obj);
            } catch (Error e4) {
                error = e4;
                n8d.b(error);
            } catch (Exception e5) {
                exc = e5;
                h7f.a(exc);
                Log.e("MagicCleanEdgeDetection", "Exception has been caught", exc);
            }
        }
        bitmap3 = filterThumbnailOutput.mOutputImage;
        return new nab.h(bitmap3, x1kVar.a());
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    @Override // defpackage.nab
    public final Object b(Bitmap bitmap, PointF[] pointFArr, CameraCleanUtils.DocSelectorType docSelectorType, byte[] bArr, byte[] bArr2, v36 v36Var) throws Throwable {
        z7d z7dVar;
        x1k x1kVar;
        CameraCleanUtils.DewarpCurveOutput dewarpCurveOutput;
        CameraCleanUtils.DewarpCurveInput dewarpCurveInput;
        if (v36Var instanceof z7d) {
            z7dVar = (z7d) v36Var;
            int i2 = z7dVar.y;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                z7dVar.y = i2 - Integer.MIN_VALUE;
            } else {
                z7dVar = new z7d(this, v36Var);
            }
        } else {
            z7dVar = new z7d(this, v36Var);
        }
        Object obj = z7dVar.w;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i3 = z7dVar.y;
        if (i3 == 0) {
            pbh.b(obj);
            x1k x1kVar2 = new x1k();
            CameraCleanUtils.DewarpCurveInput dewarpCurveInput2 = new CameraCleanUtils.DewarpCurveInput();
            dewarpCurveInput2.mInputBmpOriginalSize = bitmap;
            dewarpCurveInput2.mOriginalCornersInfo = pointFArr;
            dewarpCurveInput2.mDocSelectorType = docSelectorType;
            dewarpCurveInput2.mIsImageRotated = false;
            dewarpCurveInput2.mInputRotatedBmpForBD = null;
            dewarpCurveInput2.mIsNNPageMaskAvailable = bArr != null;
            dewarpCurveInput2.mInputBmpNNMaskPage1 = bArr;
            dewarpCurveInput2.mInputBmpNNMaskPage2 = bArr2;
            CameraCleanUtils.DewarpCurveOutput dewarpCurveOutput2 = new CameraCleanUtils.DewarpCurveOutput();
            a8d a8dVar = new a8d(this, dewarpCurveInput2, dewarpCurveOutput2, (u36) null);
            z7dVar.t = x1kVar2;
            z7dVar.u = dewarpCurveInput2;
            z7dVar.v = dewarpCurveOutput2;
            z7dVar.y = 1;
            Object objG = km3.g(e, a8dVar, z7dVar);
            if (objG == u86Var) {
                return u86Var;
            }
            x1kVar = x1kVar2;
            obj = objG;
            dewarpCurveOutput = dewarpCurveOutput2;
            dewarpCurveInput = dewarpCurveInput2;
        } else {
            if (i3 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            dewarpCurveOutput = z7dVar.v;
            dewarpCurveInput = z7dVar.u;
            x1kVar = z7dVar.t;
            pbh.b(obj);
        }
        CameraCleanUtils.CameraResult cameraResult = (CameraCleanUtils.CameraResult) obj;
        if (cameraResult == CameraCleanUtils.CameraResult.kCameraResultSuccess) {
            return new nab.d.b(dewarpCurveInput, dewarpCurveOutput, x1kVar.a());
        }
        Log.e("m8d", "computeDocumentCurve: curveResult = " + cameraResult);
        cameraResult.getClass();
        return new nab.d.a(cameraResult, x1kVar.a());
    }

    /* JADX WARN: Code duplicated, block: B:7:0x001f  */
    @Override // defpackage.nab
    public final Object c(Bitmap bitmap, PointF[] pointFArr, int i2, y yVar, boolean z, Object obj, v36 v36Var) throws Throwable {
        b8d b8dVar;
        x1k x1kVar;
        boolean z2;
        Object obj2;
        Object obj3;
        CameraCleanUtils.ImageCleaningOutput imageCleaningOutput;
        x1k x1kVar2;
        Exception e2;
        int i3;
        Bitmap bitmapCopy;
        Error e3;
        boolean z3;
        Bitmap bitmap2 = bitmap;
        if (v36Var instanceof b8d) {
            b8dVar = (b8d) v36Var;
            int i4 = b8dVar.z;
            if ((i4 & Integer.MIN_VALUE) != 0) {
                b8dVar.z = i4 - Integer.MIN_VALUE;
            } else {
                b8dVar = new b8d(this, v36Var);
            }
        } else {
            b8dVar = new b8d(this, v36Var);
        }
        Object obj4 = b8dVar.x;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i5 = b8dVar.z;
        boolean z4 = false;
        i = 0;
        int i6 = 0;
        z4 = false;
        z4 = false;
        z4 = false;
        CameraCleanUtils.LABColorPixel lABColorPixel = null;
        if (i5 == 0) {
            pbh.b(obj4);
            x1kVar = new x1k();
            if (yVar != null) {
                try {
                    z2 = yVar.k;
                } catch (Error e4) {
                    e3 = e4;
                    x1kVar2 = x1kVar;
                    i3 = 0;
                    obj2 = null;
                    bitmapCopy = null;
                    n8d.b(e3);
                    x1kVar = x1kVar2;
                    z3 = z4;
                    i6 = i3;
                    return new nab.a(bitmapCopy, new nab.c(z3, i6, lABColorPixel), obj2, x1kVar.a());
                } catch (Exception e5) {
                    e2 = e5;
                    x1kVar2 = x1kVar;
                    i3 = 0;
                    obj2 = null;
                    bitmapCopy = null;
                    h7f.a(e2);
                    Log.e("MagicCleanEdgeDetection", "Exception has been caught", e2);
                    x1kVar = x1kVar2;
                    z3 = z4;
                    i6 = i3;
                    return new nab.a(bitmapCopy, new nab.c(z3, i6, lABColorPixel), obj2, x1kVar.a());
                }
            } else {
                z2 = false;
            }
            CameraCleanUtils.ImageCleaningInput imageCleaningInput = new CameraCleanUtils.ImageCleaningInput(bitmap2, pointFArr, m(i2));
            if (obj instanceof CameraCleanUtils.CCleaningMasks) {
                imageCleaningInput.mCleaningMasks = (CameraCleanUtils.CCleaningMasks) obj;
            }
            CameraCleanUtils.ImageCleaningOutput imageCleaningOutput2 = new CameraCleanUtils.ImageCleaningOutput();
            if (z) {
                imageCleaningInput.mCropAndCleanMode = CameraCleanUtils.CropAndCleanMode.kCropAndCleanModeDefault;
            } else {
                imageCleaningInput.mCropAndCleanMode = CameraCleanUtils.CropAndCleanMode.kCropAndCleanModeOverride;
            }
            imageCleaningInput.mEnableBetaFeatures = z2;
            wt8 wt8Var = c;
            c8d c8dVar = new c8d(this, imageCleaningInput, imageCleaningOutput2, (u36) null);
            b8dVar.t = bitmap2;
            b8dVar.u = obj;
            b8dVar.v = x1kVar;
            b8dVar.w = imageCleaningOutput2;
            b8dVar.z = 1;
            Object objG = km3.g(wt8Var, c8dVar, b8dVar);
            if (objG == u86Var) {
                return u86Var;
            }
            obj2 = obj;
            obj3 = objG;
            imageCleaningOutput = imageCleaningOutput2;
        } else {
            if (i5 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            imageCleaningOutput = b8dVar.w;
            x1kVar2 = b8dVar.v;
            obj2 = b8dVar.u;
            Bitmap bitmap3 = b8dVar.t;
            try {
                pbh.b(obj4);
                x1kVar = x1kVar2;
                bitmap2 = bitmap3;
                obj3 = obj4;
            } catch (Error e6) {
                e3 = e6;
                i3 = 0;
                obj2 = null;
                bitmapCopy = null;
                n8d.b(e3);
                x1kVar = x1kVar2;
                z3 = z4;
                i6 = i3;
                return new nab.a(bitmapCopy, new nab.c(z3, i6, lABColorPixel), obj2, x1kVar.a());
            } catch (Exception e7) {
                e2 = e7;
                i3 = 0;
                obj2 = null;
                bitmapCopy = null;
                h7f.a(e2);
                Log.e("MagicCleanEdgeDetection", "Exception has been caught", e2);
                x1kVar = x1kVar2;
                z3 = z4;
                i6 = i3;
                return new nab.a(bitmapCopy, new nab.c(z3, i6, lABColorPixel), obj2, x1kVar.a());
            }
        }
        CameraCleanUtils.CameraResult cameraResult = (CameraCleanUtils.CameraResult) obj3;
        bitmapCopy = imageCleaningOutput.mBitmap;
        try {
            if (bitmapCopy == null) {
                Log.e("MagicCleanEdgeDetection", "CameraCleanAndroidShim.CropAndClean failed: " + cameraResult);
                Bitmap.Config config = bitmap2.getConfig();
                if (config == null) {
                    config = Bitmap.Config.ARGB_8888;
                }
                bitmapCopy = bitmap2.copy(config, true);
            } else if ((bitmapCopy.getPixel(0, 0) & (-16777216)) != -16777216) {
                new Canvas(bitmapCopy).drawColor(-16777216, PorterDuff.Mode.ADD);
            }
            z3 = imageCleaningOutput.mIsTintRemoved;
            try {
                i6 = imageCleaningOutput.mDocumentColorType;
                if (obj2 == null) {
                    obj2 = imageCleaningOutput.mCleaningMasks;
                }
                try {
                    lABColorPixel = imageCleaningOutput.mTintPixel;
                } catch (Error e8) {
                    e3 = e8;
                    i3 = i6;
                    z4 = z3;
                    x1kVar2 = x1kVar;
                    n8d.b(e3);
                    x1kVar = x1kVar2;
                    z3 = z4;
                    i6 = i3;
                } catch (Exception e9) {
                    e2 = e9;
                    i3 = i6;
                    z4 = z3;
                    x1kVar2 = x1kVar;
                    h7f.a(e2);
                    Log.e("MagicCleanEdgeDetection", "Exception has been caught", e2);
                    x1kVar = x1kVar2;
                    z3 = z4;
                    i6 = i3;
                }
            } catch (Error e10) {
                e3 = e10;
                i3 = i6;
                obj2 = null;
            } catch (Exception e11) {
                e2 = e11;
                i3 = i6;
                obj2 = null;
            }
        } catch (Error e12) {
            e3 = e12;
            x1kVar2 = x1kVar;
            i3 = 0;
            obj2 = null;
        } catch (Exception e13) {
            e2 = e13;
            x1kVar2 = x1kVar;
            i3 = 0;
            obj2 = null;
        }
        return new nab.a(bitmapCopy, new nab.c(z3, i6, lABColorPixel), obj2, x1kVar.a());
    }

    @Override // defpackage.nab
    public final void d(boolean z) {
        try {
            CameraCleanAndroidShim cameraCleanAndroidShimL = l();
            String str = "preloadBdModel() called with: runOnGpu = " + z;
            if (zvi.a) {
                Log.d("m8d", str);
            }
            foa foaVar = foa.a;
            wt8 wt8Var = d;
            km3.d(foaVar, wt8Var, null, new i8d(cameraCleanAndroidShimL, z, null), 2);
            CameraCleanAndroidShim cameraCleanAndroidShimL2 = l();
            if (zvi.a) {
                Log.d("m8d", "preloadDwModel() called with: runOnGpu = true");
            }
            km3.d(foaVar, wt8Var, null, new j8d(cameraCleanAndroidShimL2, null), 2);
        } catch (Error e2) {
            n8d.b(e2);
        }
    }

    @Override // defpackage.nab
    public final Vector<Integer> e(PointF[] pointFArr, int i2, int i3) {
        pointFArr.getClass();
        try {
            Vector<Integer> vectorGetFinalWidthAndHeight = l().GetFinalWidthAndHeight(pointFArr, i2, i3);
            vectorGetFinalWidthAndHeight.getClass();
            return vectorGetFinalWidthAndHeight;
        } catch (Error e2) {
            n8d.b(e2);
            return new Vector<>(2);
        }
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    @Override // defpackage.nab
    public final Object f(Bitmap bitmap, v36 v36Var) throws Throwable {
        f8d f8dVar;
        if (v36Var instanceof f8d) {
            f8dVar = (f8d) v36Var;
            int i2 = f8dVar.v;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                f8dVar.v = i2 - Integer.MIN_VALUE;
            } else {
                f8dVar = new f8d(this, v36Var);
            }
        } else {
            f8dVar = new f8d(this, v36Var);
        }
        Object objG = f8dVar.t;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i3 = f8dVar.v;
        if (i3 == 0) {
            pbh.b(objG);
            f8dVar.v = 1;
            objG = g(bitmap, null, null, f8dVar);
            if (objG == u86Var) {
                return u86Var;
            }
        } else {
            if (i3 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            pbh.b(objG);
        }
        nab.g gVar = (nab.g) objG;
        return new nab.f(gVar.c()[0], gVar.e(), gVar.d(), gVar.a(), gVar.b());
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    @Override // defpackage.nab
    public final Object g(Bitmap bitmap, hp4 hp4Var, y yVar, v36 v36Var) throws Throwable {
        d8d d8dVar;
        CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput;
        x1k x1kVar;
        CCornersInfo[] cCornersInfoArr;
        if (v36Var instanceof d8d) {
            d8dVar = (d8d) v36Var;
            int i2 = d8dVar.x;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                d8dVar.x = i2 - Integer.MIN_VALUE;
            } else {
                d8dVar = new d8d(this, v36Var);
            }
        } else {
            d8dVar = new d8d(this, v36Var);
        }
        Object obj = d8dVar.v;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i3 = d8dVar.x;
        try {
            if (i3 == 0) {
                pbh.b(obj);
                x1k x1kVar2 = new x1k();
                boolean z = yVar != null ? yVar.k : false;
                CameraCleanUtils.EdgeDetectionInput edgeDetectionInput = new CameraCleanUtils.EdgeDetectionInput(bitmap);
                CameraCleanUtils.EdgeDetectionOutput edgeDetectionOutput2 = new CameraCleanUtils.EdgeDetectionOutput();
                if (hp4Var != null) {
                    edgeDetectionInput.mDocSelectorType = hp4Var.d();
                    if (hp4Var.e() != null && !hp4Var.e().d()) {
                        t56 t56Var = t56.a;
                        PointF[] pointFArrB = hp4Var.e().b();
                        t56Var.getClass();
                        edgeDetectionInput.mCCornersInfoVec = new CCornersInfo[]{new CCornersInfo(CCornersInfo.CCornersInfoType.kCCornersInfoTypeLive, t56.b(pointFArrB))};
                    }
                }
                CameraCleanUtils.CMLAnalyticsInput cMLAnalyticsInput = edgeDetectionInput.mMLAnalyticsInput;
                cMLAnalyticsInput.mCalculateMLAnalyticsforBD = true;
                cMLAnalyticsInput.mMaxSizeOfEachBuffer = 225;
                cMLAnalyticsInput.mNumberOfBuffersForBDAnalytics = 5;
                edgeDetectionInput.mEnableBetaFeatures = z;
                wt8 wt8Var = d;
                e8d e8dVar = new e8d(this, edgeDetectionInput, edgeDetectionOutput2, (u36) null);
                d8dVar.t = x1kVar2;
                d8dVar.u = edgeDetectionOutput2;
                d8dVar.x = 1;
                Object objG = km3.g(wt8Var, e8dVar, d8dVar);
                if (objG == u86Var) {
                    return u86Var;
                }
                obj = objG;
                edgeDetectionOutput = edgeDetectionOutput2;
                x1kVar = x1kVar2;
            } else {
                if (i3 != 1) {
                    ll3.a("call to 'resume' before 'invoke' with coroutine");
                    return null;
                }
                edgeDetectionOutput = d8dVar.u;
                x1kVar = d8dVar.t;
                pbh.b(obj);
            }
            if (((CameraCleanUtils.CameraResult) obj) != CameraCleanUtils.CameraResult.kCameraResultSuccess) {
                cCornersInfoArr = new CCornersInfo[]{new CCornersInfo()};
            } else {
                cCornersInfoArr = edgeDetectionOutput.mCCornersInfoVec;
                if (cCornersInfoArr == null || cCornersInfoArr.length == 0) {
                    cCornersInfoArr = new CCornersInfo[]{new CCornersInfo()};
                } else {
                    cCornersInfoArr.getClass();
                }
            }
            CCornersInfo[] cCornersInfoArr2 = cCornersInfoArr;
            return new nab.g(cCornersInfoArr2, k(edgeDetectionOutput, cCornersInfoArr2), x1kVar.a(), edgeDetectionOutput.mInputBmpNNMaskPage1, edgeDetectionOutput.mInputBmpNNMaskPage2);
        } catch (Error e2) {
            n8d.b(e2);
            return new nab.g(new CCornersInfo[]{new CCornersInfo()});
        }
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    @Override // defpackage.nab
    public final Object h(nab.d.b bVar, v36 v36Var) throws Throwable {
        k8d k8dVar;
        CameraCleanUtils.DewarpOutput dewarpOutput;
        if (v36Var instanceof k8d) {
            k8dVar = (k8d) v36Var;
            int i2 = k8dVar.w;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                k8dVar.w = i2 - Integer.MIN_VALUE;
            } else {
                k8dVar = new k8d(this, v36Var);
            }
        } else {
            k8dVar = new k8d(this, v36Var);
        }
        Object objG = k8dVar.u;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i3 = k8dVar.w;
        if (i3 == 0) {
            pbh.b(objG);
            CameraCleanUtils.DewarpInput dewarpInput = new CameraCleanUtils.DewarpInput();
            dewarpInput.mDewarpCurveInput = new CameraCleanUtils.DewarpCurveInput();
            dewarpInput.mDewarpCurveInput = bVar.a();
            dewarpInput.mDewarpCurveOutput = new CameraCleanUtils.DewarpCurveOutput();
            dewarpInput.mDewarpCurveOutput = bVar.b();
            CameraCleanUtils.DewarpOutput dewarpOutput2 = new CameraCleanUtils.DewarpOutput();
            l8d l8dVar = new l8d(this, dewarpInput, dewarpOutput2, (u36) null);
            k8dVar.t = dewarpOutput2;
            k8dVar.w = 1;
            objG = km3.g(e, l8dVar, k8dVar);
            if (objG == u86Var) {
                return u86Var;
            }
            dewarpOutput = dewarpOutput2;
        } else {
            if (i3 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            dewarpOutput = k8dVar.t;
            pbh.b(objG);
        }
        if (((CameraCleanUtils.CameraResult) objG) == CameraCleanUtils.CameraResult.kCameraResultSuccess) {
            return new nab.e.b(dewarpOutput);
        }
        Log.e("m8d", "runDocumentDeWarping: errorCode = " + dewarpOutput.mErrorCode);
        return nab.e.a.a;
    }

    @Override // defpackage.nab
    public final DocDetectionUtils.DetectedDocType i(Bitmap bitmap) {
        try {
            DocDetectionUtils.CameraCleanDocDetectionOutput cameraCleanDocDetectionOutputDetectDocType = new CameraCleanDocDetectionAndroidShim().DetectDocType(new DocDetectionUtils.CameraCleanDocDetectionInput(bitmap));
            if (cameraCleanDocDetectionOutputDetectDocType == null) {
                return DocDetectionUtils.DetectedDocType.kDetectedDocTypeNone;
            }
            DocDetectionUtils.DetectedDocType detectedDocType = cameraCleanDocDetectionOutputDetectDocType.mDocType;
            detectedDocType.getClass();
            return detectedDocType;
        } catch (Error e2) {
            n8d.b(e2);
            return DocDetectionUtils.DetectedDocType.kDetectedDocTypeNone;
        }
    }

    @Override // defpackage.nab
    public final void j() {
        km3.d(foa.a, d, null, new a(null), 2);
    }

    public final CameraCleanAndroidShim l() {
        return (CameraCleanAndroidShim) this.a.getValue();
    }
}
