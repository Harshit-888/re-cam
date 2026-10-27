package com.adobe.dcmscan.document;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import com.adobe.dcapilibrary.dcapi.client.assets.builder.DCAssetGetMetaDataFieldInitBuilder;
import com.adobe.dcapilibrary.dcapi.client.assets.builder.DCAssetRenditionInitBuilder;
import com.adobe.dcmscan.document.Page;
import com.adobe.magic_clean.DocClassificationUtils;
import com.adobe.t5.pdf.Document;
import defpackage.ap8;
import defpackage.awb;
import defpackage.b3l;
import defpackage.c4f;
import defpackage.cm9;
import defpackage.cub;
import defpackage.d6m;
import defpackage.deb;
import defpackage.e5a;
import defpackage.e8n;
import defpackage.ebf;
import defpackage.el8;
import defpackage.g5a;
import defpackage.hcf;
import defpackage.hh3;
import defpackage.hp4;
import defpackage.i7f;
import defpackage.j7f;
import defpackage.jdp;
import defpackage.jeb;
import defpackage.kj6;
import defpackage.km3;
import defpackage.ll3;
import defpackage.nck;
import defpackage.p08;
import defpackage.p0l;
import defpackage.pbh;
import defpackage.qa2;
import defpackage.qxc;
import defpackage.r67;
import defpackage.r75;
import defpackage.s56;
import defpackage.s75;
import defpackage.s86;
import defpackage.t56;
import defpackage.tld;
import defpackage.tnb;
import defpackage.tu8;
import defpackage.u36;
import defpackage.u5a;
import defpackage.u86;
import defpackage.ua7;
import defpackage.ub7;
import defpackage.v36;
import defpackage.va2;
import defpackage.wa2;
import defpackage.wok;
import defpackage.wt8;
import defpackage.x75;
import defpackage.zed;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.enums.EnumEntries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: compiled from: Page.kt */
/* JADX INFO: loaded from: classes2.dex */
public final class Page {
    public static final AtomicInteger D = new AtomicInteger();
    public static final String E = "file_dir";
    public static final String F = "path";
    public static final String G = "eraser_filename";
    public static final String H = "magic_eraser_filename";
    public static final String I = "markup_filename";
    public static final String J = "active_eraser_filename";
    public static final String K = "active_magic_eraser_filename";
    public static final String L = "magic_eraser_showing_original";
    public static final String M = "active_markup_filename";
    public static final String N = "digitize_session_id";
    public static final String O = DCAssetGetMetaDataFieldInitBuilder.FIELDS.URI;
    public static final String P = "path_type";
    public static final String Q = "capture_mode";
    public static final String R = "first_image_landscape";
    public static final String S = "page_type";
    public static final String T = "page_scale";
    public static final String U = "filter";
    public static final String V = "brightness";
    public static final String W = "contrast";
    public static final String X = "crop_points";
    public static final String Y = "rotation";
    public static final String Z = "dewarped";
    public static final String a0 = "manual_crop";
    public static final String b0 = "book_mode_position";
    public static final String c0 = "created_with_hss";
    public static final String d0 = "auto";
    public static final String e0 = "content_uri";
    public static final String f0 = "grayscale";
    public static final String g0 = "invalid";
    public static final String h0 = "original";
    public static final String i0 = "whiteboard";
    public static final String j0 = "lighttext";
    public static final String k0 = DCAssetRenditionInitBuilder.ASSET_TYPE.DOCUMENT;
    public static final String l0 = "business_card";
    public static final String m0 = "whiteboard";
    public static final String n0 = "id_card";
    public static final String o0 = "book";
    public static final String p0 = "left";
    public static final String q0 = "right";
    public static final String r0 = "\"filter\"";
    public static final String s0 = "\"auto\"";
    public static final String t0 = "\"grayscale\"";
    public static final String u0 = "\"invalid\"";
    public static final String v0 = "\"original\"";
    public static final String w0 = "\"whiteboard\"";
    public static final String x0 = "\"lighttext\"";
    public static final wt8 y0;
    public static final wt8 z0;
    public float A;
    public final hcf B;
    public final c C;
    public final int a;
    public final e b;
    public final hcf c;
    public boolean d;
    public com.adobe.dcmscan.document.b e;
    public boolean f;
    public Boolean g;
    public final b3l h;
    public final hcf i;
    public final ArrayList<qa2> j;
    public String k;
    public String l;
    public boolean m;
    public Page n;
    public String o;
    public CaptureMode p;
    public boolean q;
    public boolean r;
    public boolean s;
    public boolean t;
    public final boolean u;
    public int v;
    public final hcf w;
    public DocClassificationUtils.DocClassificationOutput x;
    public n1.b y;
    public final hcf z;

    /* JADX INFO: compiled from: Page.kt */
    /* JADX INFO: loaded from: classes.dex */
    public enum CaptureMode {
        WHITEBOARD,
        BOOK,
        DOCUMENT,
        ID_CARD,
        BUSINESS_CARD;

        private static final /* synthetic */ EnumEntries $ENTRIES = ap8.a(values());
        public static final a Companion = new a();

        /* JADX INFO: compiled from: Page.kt */
        public static final class a {

            /* JADX INFO: renamed from: com.adobe.dcmscan.document.Page$CaptureMode$a$a, reason: collision with other inner class name */
            /* JADX INFO: compiled from: Page.kt */
            /* JADX INFO: loaded from: classes2.dex */
            public static final /* synthetic */ class C0075a {
                public static final /* synthetic */ int[] a;

                static {
                    int[] iArr = new int[CaptureMode.values().length];
                    try {
                        iArr[CaptureMode.WHITEBOARD.ordinal()] = 1;
                    } catch (NoSuchFieldError unused) {
                    }
                    try {
                        iArr[CaptureMode.BUSINESS_CARD.ordinal()] = 2;
                    } catch (NoSuchFieldError unused2) {
                    }
                    try {
                        iArr[CaptureMode.BOOK.ordinal()] = 3;
                    } catch (NoSuchFieldError unused3) {
                    }
                    a = iArr;
                }
            }
        }

        public static EnumEntries<CaptureMode> getEntries() {
            return $ENTRIES;
        }

        public final boolean requiresPlus() {
            return this != DOCUMENT;
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public static final class a {
        public final Bitmap a;
        public final int b;

        public a(Bitmap bitmap, int i) {
            this.a = bitmap;
            this.b = i;
        }

        public static tnb a(a aVar) {
            int i = aVar.b;
            Bitmap bitmap = aVar.a;
            return new tnb(bitmap.getWidth(), bitmap.getHeight(), i);
        }

        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            return this.a.equals(aVar.a) && this.b == aVar.b;
        }

        public final int hashCode() {
            return Integer.hashCode(this.b) + (this.a.hashCode() * 31);
        }

        public final String toString() {
            return "BitmapInfo(bitmap=" + this.a + ", rotation=" + this.b + ")";
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public static final class b {

        /* JADX INFO: compiled from: Page.kt */
        public static final /* synthetic */ class a {
            public static final /* synthetic */ int[] a;

            static {
                int[] iArr = new int[n1.b.values().length];
                try {
                    iArr[n1.b.FIT_TO_PAPER.ordinal()] = 1;
                } catch (NoSuchFieldError unused) {
                }
                try {
                    iArr[n1.b.LETTER_PORTRAIT.ordinal()] = 2;
                } catch (NoSuchFieldError unused2) {
                }
                try {
                    iArr[n1.b.LETTER_LANDSCAPE.ordinal()] = 3;
                } catch (NoSuchFieldError unused3) {
                }
                try {
                    iArr[n1.b.LEGAL_PORTRAIT.ordinal()] = 4;
                } catch (NoSuchFieldError unused4) {
                }
                try {
                    iArr[n1.b.LEGAL_LANDSCAPE.ordinal()] = 5;
                } catch (NoSuchFieldError unused5) {
                }
                try {
                    iArr[n1.b.A3_PORTRAIT.ordinal()] = 6;
                } catch (NoSuchFieldError unused6) {
                }
                try {
                    iArr[n1.b.A3_LANDSCAPE.ordinal()] = 7;
                } catch (NoSuchFieldError unused7) {
                }
                try {
                    iArr[n1.b.A4_PORTRAIT.ordinal()] = 8;
                } catch (NoSuchFieldError unused8) {
                }
                try {
                    iArr[n1.b.A4_LANDSCAPE.ordinal()] = 9;
                } catch (NoSuchFieldError unused9) {
                }
                try {
                    iArr[n1.b.A5_PORTRAIT.ordinal()] = 10;
                } catch (NoSuchFieldError unused10) {
                }
                try {
                    iArr[n1.b.A5_LANDSCAPE.ordinal()] = 11;
                } catch (NoSuchFieldError unused11) {
                }
                try {
                    iArr[n1.b.BUSINESS_CARD.ordinal()] = 12;
                } catch (NoSuchFieldError unused12) {
                }
                a = iArr;
            }
        }

        public static String a(int i) {
            String str;
            if (i == -1) {
                str = Page.u0;
            } else if (i == 0) {
                str = Page.v0;
            } else if (i == 1) {
                str = Page.s0;
            } else if (i == 2) {
                str = Page.t0;
            } else if (i != 3) {
                str = i != 4 ? Page.u0 : Page.x0;
            } else {
                str = Page.w0;
            }
            return e8n.a(Page.r0, ":", str);
        }

        public static Page b(Uri uri, int i, boolean z, n1.b bVar, com.adobe.dcmscan.document.b bVar2, boolean z2) {
            deb.a.getClass();
            File fileB = deb.b(uri);
            if (fileB == null) {
                return null;
            }
            String path = fileB.getPath();
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            try {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    if (options.outWidth <= 100 || options.outHeight <= 100) {
                        return null;
                    }
                    if (jeb.a) {
                        try {
                            BitmapFactory.Options options2 = new BitmapFactory.Options();
                            BitmapRegionDecoder bitmapRegionDecoderNewInstance = BitmapRegionDecoder.newInstance(path, false);
                            bitmapRegionDecoderNewInstance.getClass();
                            if (bitmapRegionDecoderNewInstance.decodeRegion(new Rect(0, 0, 2, 2), options2) == null) {
                                return null;
                            }
                        } catch (Exception unused) {
                            BitmapFactory.Options options3 = new BitmapFactory.Options();
                            options3.inSampleSize = 8;
                            if (BitmapFactory.decodeFile(path, options3) == null) {
                                return null;
                            }
                        }
                    }
                    tu8.a.getClass();
                    int iD = tu8.d(path);
                    deb.a.getClass();
                    File fileD = deb.d(fileB, i, true, true);
                    if (fileD == null) {
                        return null;
                    }
                    Page page = new Page(fileD, iD, uri, bVar2, z2);
                    if (z) {
                        page.h(bVar, Float.valueOf(1.0f));
                    }
                    return page;
                } catch (Error e) {
                    String stackTraceString = Log.getStackTraceString(e);
                    if (stackTraceString == null) {
                        return null;
                    }
                    Log.e("Page", stackTraceString);
                    return null;
                }
            } catch (Exception e2) {
                String stackTraceString2 = Log.getStackTraceString(e2);
                if (stackTraceString2 == null) {
                    return null;
                }
                Log.e("Page", stackTraceString2);
                return null;
            }
        }

        public static /* synthetic */ Page c(Uri uri, int i, boolean z, n1.b bVar, com.adobe.dcmscan.document.b bVar2, int i2) {
            if ((i2 & 32) != 0) {
                bVar = null;
            }
            return b(uri, i, z, bVar, bVar2, false);
        }

        public static JSONObject d(String str, String str2) throws JSONException {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(Page.P, Page.E);
            jSONObject.put(Page.F, str);
            jSONObject.put(Page.Y, 0);
            jSONObject.put(Page.U, Page.h0);
            jSONObject.put(Page.X, awb.a(new kj6().a));
            if (str2 != null) {
                jSONObject.put(Page.Q, str2);
            }
            return jSONObject;
        }

        public static JSONObject e(int i, String str, boolean z) throws JSONException {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(Page.P, Page.E);
            jSONObject.put(Page.F, str);
            jSONObject.put(Page.Y, i);
            if (z) {
                jSONObject.put(Page.U, Page.d0);
                return jSONObject;
            }
            jSONObject.put(Page.U, Page.h0);
            jSONObject.put(Page.X, awb.a(new kj6().a));
            return jSONObject;
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public final class c {
        public final Uri a;

        public c(Uri uri) {
            uri.getClass();
            this.a = uri;
        }

        public final Uri a() {
            return this.a;
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public static final class d {
        public final RectF a;
        public final ArrayList b;

        public d(RectF rectF, ArrayList arrayList) {
            this.a = rectF;
            this.b = arrayList;
        }

        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof d)) {
                return false;
            }
            d dVar = (d) obj;
            return this.a.equals(dVar.a) && this.b.equals(dVar.b);
        }

        public final int hashCode() {
            return this.b.hashCode() + (this.a.hashCode() * 31);
        }

        public final String toString() {
            return "MarkDrawingPaths(bounds=" + this.a + ", marks=" + this.b + ")";
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public final class e {
        public String A;
        public final String a = hh3.b("\"", Page.V, "\"");
        public final String b = hh3.b("\"", Page.W, "\"");
        public final String c = hh3.b("\"", Page.Q, "\"");
        public final String d = hh3.b("\"", Page.Y, "\"");
        public final String e = hh3.b("\"", Page.S, "\"");
        public final String f = hh3.b("\"", Page.T, "\"");
        public final String g = hh3.b("\"", Page.R, "\"");
        public final String h = hh3.b("\"", Page.k0, "\"");
        public final String i = hh3.b("\"", Page.l0, "\"");
        public final String j = hh3.b("\"", Page.m0, "\"");
        public final String k = hh3.b("\"", Page.n0, "\"");
        public final String l = hh3.b("\"", Page.o0, "\"");
        public final String m = hh3.b("\"", Page.I, "\"");
        public final String n = hh3.b("\"", Page.M, "\"");
        public final String o = hh3.b("\"", Page.N, "\"");
        public final String p = e8n.a(Page.r0, ":", Page.u0);
        public String q;
        public String r;
        public String s;
        public String t;
        public String u;
        public String v;
        public String w;
        public String x;
        public String y;
        public String z;
    }

    /* JADX INFO: compiled from: Page.kt */
    public enum f {
        CONTENT_URI,
        FILE_DIR;

        private static final /* synthetic */ EnumEntries $ENTRIES = ap8.a(values());

        public static EnumEntries<f> getEntries() {
            return $ENTRIES;
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public static final class g {
        public static String a(JSONObject jSONObject) {
            return jSONObject.optString(Page.J);
        }

        public static String b(JSONObject jSONObject) {
            return jSONObject.optString(Page.K);
        }

        public static String c(JSONObject jSONObject) {
            return jSONObject.optString(Page.M);
        }

        public static n.d d(JSONObject jSONObject) {
            String strOptString = jSONObject.optString(Page.b0);
            if (cub.b(strOptString, Page.p0)) {
                return n.d.LEFT;
            }
            if (cub.b(strOptString, Page.q0)) {
                return n.d.RIGHT;
            }
            return null;
        }

        public static int e(JSONObject jSONObject) {
            return jSONObject.optInt(Page.V, 0);
        }

        public static CaptureMode f(JSONObject jSONObject) {
            String strOptString = jSONObject.optString(Page.Q);
            if (cub.b(strOptString, Page.l0) || cub.b(strOptString, "Business Card") || cub.b(strOptString, "BusinessCard")) {
                return CaptureMode.BUSINESS_CARD;
            }
            if (cub.b(strOptString, Page.m0) || cub.b(strOptString, "Whiteboard")) {
                return CaptureMode.WHITEBOARD;
            }
            if (cub.b(strOptString, Page.k0) || cub.b(strOptString, "Document")) {
                return CaptureMode.DOCUMENT;
            }
            if (cub.b(strOptString, Page.n0) || cub.b(strOptString, "ID Card") || cub.b(strOptString, "IdCard")) {
                return CaptureMode.ID_CARD;
            }
            if (cub.b(strOptString, Page.o0) || cub.b(strOptString, "Book")) {
                return CaptureMode.BOOK;
            }
            return null;
        }

        public static int g(JSONObject jSONObject) {
            return jSONObject.optInt(Page.W, 0);
        }

        public static boolean h(JSONObject jSONObject) {
            return jSONObject.optBoolean(Page.c0, false);
        }

        /* JADX WARN: Code duplicated, block: B:26:0x005c  */
        /* JADX WARN: Code duplicated, block: B:34:? A[RETURN, SYNTHETIC] */
        public static kj6 i(JSONObject jSONObject) {
            JSONArray jSONArrayOptJSONArray;
            JSONException e;
            String str;
            String stackTraceString;
            String str2 = Page.X;
            awb awbVar = awb.a;
            PointF[] pointFArr = new PointF[0];
            if (str2 != null) {
                try {
                    jSONArrayOptJSONArray = jSONObject.optJSONArray(str2);
                } catch (JSONException e2) {
                    e = e2;
                    str = awb.b;
                    stackTraceString = Log.getStackTraceString(e);
                    if (str != null) {
                        Log.e(str, stackTraceString);
                    }
                    if (pointFArr.length != 4) {
                        return null;
                    }
                    return new kj6(pointFArr);
                }
            } else {
                jSONArrayOptJSONArray = null;
            }
            if (jSONArrayOptJSONArray != null) {
                int length = jSONArrayOptJSONArray.length();
                PointF[] pointFArr2 = new PointF[length];
                for (int i = 0; i < length; i++) {
                    pointFArr2[i] = new PointF();
                }
                for (int i2 = 0; i2 < length; i2++) {
                    try {
                        JSONObject jSONObject2 = jSONArrayOptJSONArray.getJSONObject(i2);
                        pointFArr2[i2] = new PointF((float) jSONObject2.getDouble("x"), (float) jSONObject2.getDouble("y"));
                    } catch (JSONException e3) {
                        e = e3;
                        pointFArr = pointFArr2;
                        str = awb.b;
                        stackTraceString = Log.getStackTraceString(e);
                        if (str != null && stackTraceString != null) {
                            Log.e(str, stackTraceString);
                        }
                        if (pointFArr.length != 4) {
                            return null;
                        }
                        return new kj6(pointFArr);
                    }
                }
                pointFArr = pointFArr2;
            }
            if (pointFArr.length != 4) {
                return null;
            }
            return new kj6(pointFArr);
        }

        public static boolean j(JSONObject jSONObject) {
            return jSONObject.optBoolean(Page.Z, false);
        }

        public static String k(JSONObject jSONObject) {
            String strOptString = jSONObject.optString(Page.N);
            strOptString.getClass();
            if (strOptString.length() > 0) {
                return strOptString;
            }
            return null;
        }

        public static String l(JSONObject jSONObject) {
            return jSONObject.optString(Page.G);
        }

        public static int m(JSONObject jSONObject) {
            try {
                String string = jSONObject.getString(Page.U);
                if (cub.b(string, Page.h0)) {
                    return 0;
                }
                if (cub.b(string, Page.d0)) {
                    return 1;
                }
                if (cub.b(string, Page.f0)) {
                    return 2;
                }
                if (cub.b(string, Page.i0)) {
                    return 3;
                }
                if (cub.b(string, Page.j0)) {
                    return 4;
                }
                return cub.b(string, Page.g0) ? -1 : 1;
            } catch (Exception unused) {
                return -1;
            }
        }

        public static Boolean n(JSONObject jSONObject) {
            String str = Page.R;
            if (jSONObject.has(str)) {
                return Boolean.valueOf(jSONObject.getBoolean(str));
            }
            return null;
        }

        public static String o(JSONObject jSONObject) {
            return jSONObject.optString(Page.H);
        }

        public static boolean p(JSONObject jSONObject) {
            return jSONObject.optBoolean(Page.L, false);
        }

        public static boolean q(JSONObject jSONObject) {
            return jSONObject.optBoolean(Page.a0, false);
        }

        public static String r(JSONObject jSONObject) {
            return jSONObject.optString(Page.I);
        }

        public static String s(JSONObject jSONObject) {
            return jSONObject.optString(Page.F);
        }

        public static f t(JSONObject jSONObject) {
            try {
                String string = jSONObject.getString(Page.P);
                if (cub.b(string, Page.e0)) {
                    return f.CONTENT_URI;
                }
                return cub.b(string, Page.E) ? f.FILE_DIR : f.FILE_DIR;
            } catch (Exception unused) {
                return null;
            }
        }

        public static String u(JSONObject jSONObject) {
            return jSONObject.optString(Page.O);
        }

        public static float v(JSONObject jSONObject) {
            return jSONObject.optInt(Page.T, 100) / 100.0f;
        }

        /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
        public static n1.b w(JSONObject jSONObject) {
            String strOptString = jSONObject.optString(Page.S, "fit");
            strOptString.getClass();
            switch (strOptString.hashCode()) {
                case -1843217562:
                    if (strOptString.equals("A5_portrait")) {
                        return n1.b.A5_PORTRAIT;
                    }
                    break;
                case -1708030258:
                    if (strOptString.equals("A3_landscape")) {
                        return n1.b.A3_LANDSCAPE;
                    }
                    break;
                case -1646704057:
                    if (strOptString.equals("A4_portrait")) {
                        return n1.b.A4_PORTRAIT;
                    }
                    break;
                case -1450190552:
                    if (strOptString.equals("A3_portrait")) {
                        return n1.b.A3_PORTRAIT;
                    }
                    break;
                case -1290077003:
                    if (strOptString.equals("legal_landscape")) {
                        return n1.b.LEGAL_LANDSCAPE;
                    }
                    break;
                case -1006965680:
                    if (strOptString.equals("A5_landscape")) {
                        return n1.b.A5_LANDSCAPE;
                    }
                    break;
                case 629558319:
                    if (strOptString.equals("business_card")) {
                        return n1.b.BUSINESS_CARD;
                    }
                    break;
                case 641501793:
                    if (strOptString.equals("legal_portrait")) {
                        return n1.b.LEGAL_PORTRAIT;
                    }
                    break;
                case 789985679:
                    if (strOptString.equals("A4_landscape")) {
                        return n1.b.A4_LANDSCAPE;
                    }
                    break;
                case 1287328660:
                    if (strOptString.equals("letter_portrait")) {
                        return n1.b.LETTER_PORTRAIT;
                    }
                    break;
                case 1550686690:
                    if (strOptString.equals("letter_landscape")) {
                        return n1.b.LETTER_LANDSCAPE;
                    }
                    break;
            }
            return n1.b.FIT_TO_PAPER;
        }

        public static int x(JSONObject jSONObject) {
            return jSONObject.optInt(Page.Y, 0);
        }

        public static JSONArray y(JSONObject jSONObject) {
            return jSONObject.optJSONArray("sources");
        }

        public static boolean z(JSONObject jSONObject) {
            return cub.b(jSONObject.optString(Page.Q), "Form");
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    public static final /* synthetic */ class h {
        public static final /* synthetic */ int[] a;

        static {
            int[] iArr = new int[CaptureMode.values().length];
            try {
                iArr[CaptureMode.BUSINESS_CARD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[CaptureMode.WHITEBOARD.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[CaptureMode.DOCUMENT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[CaptureMode.ID_CARD.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                iArr[CaptureMode.BOOK.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            a = iArr;
        }
    }

    /* JADX INFO: compiled from: Page.kt */
    @r67(c = "com.adobe.dcmscan.document.Page$setMarkupData_BLOCKING$1", f = "Page.kt", l = {178}, m = "invokeSuspend", v = 2)
    public static final class i extends p0l implements u5a<s86, u36<? super d6m>, Object> {
        public int t;
        public final /* synthetic */ ArrayList<qa2> v;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public i(ArrayList<qa2> arrayList, u36<? super i> u36Var) {
            super(2, u36Var);
            this.v = arrayList;
        }

        @Override // defpackage.k73
        public final u36<d6m> create(Object obj, u36<?> u36Var) {
            return Page.this.new i(this.v, u36Var);
        }

        @Override // defpackage.u5a
        public final Object invoke(s86 s86Var, u36<? super d6m> u36Var) {
            return ((i) create(s86Var, u36Var)).invokeSuspend(d6m.a);
        }

        @Override // defpackage.k73
        public final Object invokeSuspend(Object obj) {
            u86 u86Var = u86.COROUTINE_SUSPENDED;
            int i = this.t;
            String strB = null;
            if (i == 0) {
                pbh.b(obj);
                Page page = Page.this;
                hcf hcfVar = page.i;
                this.t = 1;
                ArrayList<qa2> arrayList = page.j;
                arrayList.clear();
                ArrayList<qa2> arrayList2 = this.v;
                arrayList.addAll(arrayList2);
                Iterator<qa2> it = arrayList.iterator();
                while (it.hasNext()) {
                    it.next().g = null;
                }
                if (arrayList.isEmpty()) {
                    hcfVar.setValue(0L);
                } else {
                    hcfVar.setValue(Long.valueOf(SystemClock.elapsedRealtime()));
                    va2 va2Var = new va2();
                    va2Var.e(arrayList2);
                    String str = wa2.a;
                    strB = wa2.a.b(va2Var, null);
                }
                page.d0(strB);
                if (d6m.a == u86Var) {
                    return u86Var;
                }
            } else {
                if (i != 1) {
                    ll3.a("call to 'resume' before 'invoke' with coroutine");
                    return null;
                }
                pbh.b(obj);
            }
            return d6m.a;
        }
    }

    static {
        ExecutorService executorServiceNewSingleThreadExecutor = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor.getClass();
        y0 = new wt8(executorServiceNewSingleThreadExecutor);
        ExecutorService executorServiceNewSingleThreadExecutor2 = Executors.newSingleThreadExecutor();
        executorServiceNewSingleThreadExecutor2.getClass();
        z0 = new wt8(executorServiceNewSingleThreadExecutor2);
    }

    public Page(File file, int i2, boolean z, CaptureMode captureMode, com.adobe.dcmscan.document.b bVar, boolean z2, int i3) {
        CaptureMode captureMode2 = (i3 & 32) != 0 ? null : captureMode;
        boolean z3 = (i3 & 128) != 0 ? false : z2;
        boolean z4 = (i3 & Document.PERMITTED_OPERATION_FORM_ENTRY) == 0;
        file.getClass();
        this.a = D.incrementAndGet();
        this.b = new e();
        this.c = nck.g(el8.a);
        this.f = true;
        this.h = new b3l(new e5a() { // from class: a4f
            @Override // defpackage.e5a
            public final Object invoke() {
                return Page.a(this.a);
            }
        });
        this.i = nck.g(0L);
        this.j = new ArrayList<>();
        this.u = true;
        this.w = nck.g(Integer.valueOf(this.v));
        n1.b bVar2 = n1.b.FIT_TO_PAPER;
        this.y = bVar2;
        this.z = nck.g(bVar2);
        this.A = 1.0f;
        this.B = nck.g(Float.valueOf(1.0f));
        this.e = bVar;
        if (z4) {
            this.u = false;
        } else if (z) {
            t56.a.getClass();
            this.u = t56.g.getValue().booleanValue();
        }
        O(file, i2, z, false, null, captureMode2, null, z3);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static j7f a(Page page) throws Exception {
        Boolean boolValueOf;
        boolean z = false;
        if (page.g == null && page.f) {
            n nVar = (n) x75.O(page.v());
            if (nVar != null) {
                Size sizeK = nVar.K();
                ebf ebfVarA = jdp.a(Integer.valueOf(sizeK.getWidth()), Integer.valueOf(sizeK.getHeight()), nVar.G);
                boolValueOf = Boolean.valueOf(((Number) ebfVarA.a).intValue() >= ((Number) ebfVarA.b).intValue());
            } else {
                boolValueOf = null;
            }
            page.b0(boolValueOf);
            page.f = false;
        }
        Boolean bool = page.g;
        boolean zBooleanValue = bool != null ? bool.booleanValue() : true;
        if (page.B() && page.g != null) {
            z = true;
        }
        return new j7f(zBooleanValue, z);
    }

    public static i7f f(Page page, n1.b bVar, int i2, ArrayList arrayList, int i3) throws Exception {
        if ((i3 & 1) != 0) {
            bVar = page.y;
        }
        float f2 = (i3 & 2) != 0 ? page.A : 1.0f;
        if ((i3 & 4) != 0) {
            i2 = page.v;
        }
        if ((i3 & 8) != 0) {
            List<n> listV = page.v();
            ArrayList arrayList2 = new ArrayList(s75.o(listV, 10));
            for (n nVar : listV) {
                Size sizeK = nVar.K();
                arrayList2.add(new tnb(sizeK.getWidth(), sizeK.getHeight(), nVar.G));
            }
            arrayList = arrayList2;
        }
        return page.e(bVar, f2, i2, arrayList);
    }

    public final String A() {
        return this.l;
    }

    public final boolean B() {
        return this.p == CaptureMode.ID_CARD;
    }

    public final void C(StringBuilder sb, g5a<? super Page, Integer> g5aVar, g5a<? super File, String> g5aVar2, boolean z) {
        boolean z2;
        sb.append('{');
        Integer numInvoke = g5aVar.invoke(this);
        e eVar = this.b;
        String strA = numInvoke != null ? b.a(numInvoke.intValue()) : eVar.t;
        if (TextUtils.isEmpty(strA)) {
            sb.append(eVar.p);
        } else {
            sb.append(strA);
        }
        if (!TextUtils.isEmpty(eVar.u)) {
            sb.append(",");
            sb.append(eVar.u);
        }
        if (!TextUtils.isEmpty(eVar.v)) {
            sb.append(",");
            sb.append(eVar.v);
        }
        if (!TextUtils.isEmpty(eVar.q)) {
            sb.append(',');
            sb.append(eVar.q);
        }
        if (!TextUtils.isEmpty(eVar.r)) {
            sb.append(',');
            sb.append(eVar.r);
        }
        if (!TextUtils.isEmpty(eVar.s)) {
            sb.append(',');
            sb.append(eVar.s);
        }
        if (!TextUtils.isEmpty(eVar.w)) {
            sb.append(',');
            sb.append(eVar.w);
        }
        if (!TextUtils.isEmpty(eVar.x)) {
            sb.append(',');
            sb.append(eVar.x);
        }
        if (!TextUtils.isEmpty(eVar.y)) {
            sb.append(',');
            sb.append(eVar.y);
        }
        if (!TextUtils.isEmpty(eVar.z)) {
            sb.append(',');
            sb.append(eVar.z);
        }
        if (!TextUtils.isEmpty(eVar.A)) {
            sb.append(',');
            sb.append(eVar.A);
        }
        sb.append(',');
        sb.append("\"images\"");
        sb.append(":[");
        int size = v().size();
        int i2 = 0;
        while (true) {
            z2 = true;
            if (i2 >= size) {
                break;
            }
            if (i2 > 0) {
                sb.append(',');
            }
            n nVar = v().get(i2);
            n.r rVar = nVar.k;
            sb.append('{');
            String strInvoke = g5aVar2.invoke(nVar.m.c());
            if (strInvoke == null || strInvoke.length() == 0) {
                sb.append(rVar.r);
                if (!TextUtils.isEmpty(rVar.s)) {
                    sb.append(',');
                    sb.append(rVar.s);
                }
                if (!TextUtils.isEmpty(rVar.t)) {
                    sb.append(',');
                    sb.append(rVar.t);
                }
            } else {
                sb.append(rVar.c + ":" + rVar.q);
                sb.append(',');
                sb.append(rVar.b + ":" + JSONObject.quote(strInvoke));
            }
            if (!TextUtils.isEmpty(rVar.u)) {
                sb.append(',');
                sb.append(rVar.u);
            }
            if (!TextUtils.isEmpty(rVar.v)) {
                sb.append(',');
                sb.append(rVar.v);
            }
            if (!TextUtils.isEmpty(rVar.w)) {
                sb.append(',');
                sb.append(rVar.w);
            }
            if (!TextUtils.isEmpty(rVar.x)) {
                sb.append(',');
                sb.append(rVar.x);
            }
            if (!TextUtils.isEmpty(rVar.y)) {
                sb.append(',');
                sb.append(rVar.y);
            }
            if (!TextUtils.isEmpty(rVar.z)) {
                sb.append(',');
                sb.append(rVar.z);
            }
            if (!TextUtils.isEmpty(rVar.B)) {
                sb.append(',');
                sb.append(rVar.B);
            }
            if (!TextUtils.isEmpty(rVar.C)) {
                sb.append(',');
                sb.append(rVar.C);
            }
            if (!TextUtils.isEmpty(rVar.D)) {
                sb.append(',');
                sb.append(rVar.D);
            }
            if (!TextUtils.isEmpty(rVar.A)) {
                sb.append(',');
                sb.append(rVar.A);
            }
            hp4 hp4Var = nVar.c;
            if ((hp4Var != null && hp4Var.j) || nVar.l) {
                sb.append(',');
                sb.append(rVar.n + ": true");
            }
            sb.append('}');
            i2++;
        }
        sb.append("]");
        if (z && this.n != null) {
            sb.append(",\"sources\":[");
            Page page = this.n;
            while (page != null) {
                if (!z2) {
                    sb.append(',');
                }
                page.C(sb, g5aVar, g5aVar2, false);
                page = page.n;
                z2 = false;
            }
            sb.append("]");
        }
        sb.append("}");
    }

    public final n1.b E() {
        return this.y;
    }

    public final long F() {
        List<n> listV = v();
        ArrayList arrayList = new ArrayList(s75.o(listV, 10));
        Iterator<T> it = listV.iterator();
        while (it.hasNext()) {
            arrayList.add(Long.valueOf(((n) it.next()).K0));
        }
        Long l = (Long) x75.Y(arrayList);
        if (l != null) {
            return l.longValue();
        }
        return -1L;
    }

    public final int G() {
        return this.v;
    }

    public final com.adobe.dcmscan.y H() {
        com.adobe.dcmscan.y yVar;
        com.adobe.dcmscan.document.b bVar = this.e;
        if (bVar != null && (yVar = bVar.a) != null) {
            return yVar;
        }
        com.adobe.dcmscan.y yVar2 = com.adobe.dcmscan.z.a;
        return com.adobe.dcmscan.z.a;
    }

    public final boolean I() {
        return this.s;
    }

    public final boolean J() {
        return this.u;
    }

    public final Page K() {
        return this.n;
    }

    public final boolean L() {
        List<n> listV = v();
        if (listV != null && listV.isEmpty()) {
            return true;
        }
        for (n nVar : listV) {
            if (!n.s.i(nVar.w0)) {
                return false;
            }
            if (nVar.g0() && !n.s.i(nVar.f0)) {
                return false;
            }
        }
        return true;
    }

    public final boolean M() {
        return !this.j.isEmpty();
    }

    public final boolean N() {
        n1.b bVar = this.y;
        return ((bVar != n1.b.FIT_TO_PAPER && bVar != n1.b.BUSINESS_CARD) || B() || M()) ? false : true;
    }

    public final void O(File file, int i2, boolean z, boolean z2, hp4 hp4Var, CaptureMode captureMode, n.d dVar, boolean z3) {
        this.r = z;
        this.s = false;
        this.t = false;
        X(captureMode);
        this.q = z2;
        a0(-1);
        this.c.setValue(x75.g0(v(), new n(file, this, i2, hp4Var, H(), this.u, this.p, dVar, z3, Document.PERMITTED_OPERATION_ACCESSIBLE_CONTENT_EXTRACTION)));
    }

    public final boolean P() {
        DocClassificationUtils.DocClassificationOutput docClassificationOutput = this.x;
        return docClassificationOutput != null && docClassificationOutput.mDocClassification == DocClassificationUtils.DocClassification.kDocClassificationBusinessCard;
    }

    public final boolean Q() {
        if (this.m) {
            return true;
        }
        DocClassificationUtils.DocClassificationOutput docClassificationOutput = this.x;
        return docClassificationOutput != null && docClassificationOutput.mDocClassification == DocClassificationUtils.DocClassification.kDocClassificationForm;
    }

    public final boolean R() {
        return this.r && this.C != null;
    }

    public final boolean S() {
        List<n> listV = v();
        if (listV != null && listV.isEmpty()) {
            return false;
        }
        Iterator<T> it = listV.iterator();
        while (it.hasNext()) {
            if (((n) it.next()).L.m()) {
                return true;
            }
        }
        return false;
    }

    public final void T(int i2) {
        ArrayList arrayListV0 = x75.v0(v());
        arrayListV0.remove(i2);
        this.c.setValue(arrayListV0);
    }

    public final void U(int i2, n nVar) {
        ArrayList arrayListV0 = x75.v0(v());
        arrayListV0.set(i2, nVar);
        this.c.setValue(arrayListV0);
    }

    /* JADX WARN: Code duplicated, block: B:25:0x0060  */
    /* JADX WARN: Code duplicated, block: B:7:0x0013  */
    public final Object V(v36 v36Var) throws Throwable {
        c4f c4fVar;
        boolean z;
        boolean z2;
        if (v36Var instanceof c4f) {
            c4fVar = (c4f) v36Var;
            int i2 = c4fVar.w;
            if ((i2 & Integer.MIN_VALUE) != 0) {
                c4fVar.w = i2 - Integer.MIN_VALUE;
            } else {
                c4fVar = new c4f(this, v36Var);
            }
        } else {
            c4fVar = new c4f(this, v36Var);
        }
        Object objL0 = c4fVar.u;
        u86 u86Var = u86.COROUTINE_SUSPENDED;
        int i3 = c4fVar.w;
        if (i3 == 0) {
            pbh.b(objL0);
            Iterator<T> it = v().iterator();
            while (true) {
                if (!it.hasNext()) {
                    if (this.e != null) {
                        z = true;
                        break;
                    }
                } else {
                    com.adobe.dcmscan.document.h hVar = ((n) it.next()).m;
                    if (hVar.d()) {
                        z2 = false;
                    } else {
                        File fileC = hVar.c();
                        if (fileC != null ? fileC.exists() : false) {
                            z2 = true;
                        } else {
                            z2 = false;
                        }
                    }
                    if (!z2) {
                    }
                }
                z = false;
                break;
            }
            if (!z || this.x != null) {
                return d6m.a;
            }
            n nVar = v().get(0);
            c4fVar.t = this;
            c4fVar.w = 1;
            objL0 = nVar.l0(y0, c4fVar);
            if (objL0 == u86Var) {
                return u86Var;
            }
        } else {
            if (i3 != 1) {
                ll3.a("call to 'resume' before 'invoke' with coroutine");
                return null;
            }
            this = c4fVar.t;
            pbh.b(objL0);
        }
        this.x = (DocClassificationUtils.DocClassificationOutput) objL0;
        return d6m.a;
    }

    public final void W(int i2) {
        List<n> listV = v();
        if (listV == null || !listV.isEmpty()) {
            Iterator<T> it = listV.iterator();
            while (it.hasNext()) {
                if (((n) it.next()).w() != i2) {
                    g();
                    break;
                }
            }
        }
        for (n nVar : v()) {
            nVar.Y.setValue(nVar, n.L0[3], Integer.valueOf(i2));
        }
        e eVar = this.b;
        eVar.u = eVar.a + ":" + i2;
        com.adobe.dcmscan.document.b.a aVar = com.adobe.dcmscan.document.b.y;
        com.adobe.dcmscan.document.b.a.c(this.e, true, 4);
    }

    public final void X(CaptureMode captureMode) {
        String str;
        this.p = captureMode;
        if (captureMode == null) {
            return;
        }
        int i2 = captureMode == null ? -1 : h.a[captureMode.ordinal()];
        e eVar = this.b;
        if (i2 == 1) {
            str = eVar.i;
        } else if (i2 == 2) {
            str = eVar.j;
        } else if (i2 == 3) {
            str = eVar.h;
        } else if (i2 != 4) {
            str = i2 != 5 ? eVar.h : eVar.l;
        } else {
            str = eVar.k;
        }
        eVar.w = e8n.a(eVar.c, ":", str);
        if (this.y == n1.b.FIT_TO_PAPER && captureMode == CaptureMode.BUSINESS_CARD) {
            h0(n1.b.BUSINESS_CARD);
        }
    }

    public final void Y(int i2) {
        List<n> listV = v();
        if (listV == null || !listV.isEmpty()) {
            Iterator<T> it = listV.iterator();
            while (it.hasNext()) {
                if (((n) it.next()).B() != i2) {
                    g();
                    break;
                }
            }
        }
        for (n nVar : v()) {
            nVar.Z.setValue(nVar, n.L0[4], Integer.valueOf(i2));
        }
        e eVar = this.b;
        eVar.v = eVar.b + ":" + i2;
        com.adobe.dcmscan.document.b.a aVar = com.adobe.dcmscan.document.b.y;
        com.adobe.dcmscan.document.b.a.c(this.e, true, 4);
    }

    public final void Z(String str) {
        if (!cub.b(this.o, str)) {
            e eVar = this.b;
            String strA = null;
            if (str != null) {
                eVar.getClass();
                String str2 = !wok.H(str) ? str : null;
                if (str2 != null) {
                    strA = e8n.a(eVar.o, ":", JSONObject.quote(str2));
                }
            }
            eVar.A = strA;
        }
        this.o = str;
    }

    public final void a0(int i2) {
        List<n> listV = v();
        if (listV == null || !listV.isEmpty()) {
            Iterator<T> it = listV.iterator();
            while (it.hasNext()) {
                if (((n) it.next()).H() != i2) {
                    g();
                    break;
                }
            }
        }
        Iterator<n> it2 = v().iterator();
        while (it2.hasNext()) {
            it2.next().t0(i2);
        }
    }

    public final n b(File file, int i2, hp4 hp4Var) {
        file.getClass();
        n nVar = new n(file, this, i2, hp4Var, H(), this.u, this.p, (n.d) null, false, 640);
        this.c.setValue(x75.g0(v(), nVar));
        return nVar;
    }

    public final void b0(Boolean bool) {
        String strA;
        this.g = bool;
        if (B()) {
            boolean zB = cub.b(bool, Boolean.TRUE);
            e eVar = this.b;
            if (zB) {
                strA = s56.a(eVar.g, ": true");
            } else {
                strA = cub.b(bool, Boolean.FALSE) ? s56.a(eVar.g, ": false") : null;
            }
            eVar.x = strA;
            com.adobe.dcmscan.document.b.a aVar = com.adobe.dcmscan.document.b.y;
            com.adobe.dcmscan.document.b.a.c(this.e, false, 6);
        }
    }

    public final void c(n nVar) {
        this.c.setValue(x75.g0(v(), nVar));
    }

    public final void c0(boolean z) {
        this.t = z;
    }

    public final qxc d() {
        qxc qxcVarD = r75.d();
        qxcVarD.add(this);
        for (Page page = this.n; page != null; page = page.n) {
            qxcVarD.add(page);
        }
        return r75.c(qxcVarD);
    }

    public final void d0(String str) {
        if (!cub.b(this.k, str)) {
            e eVar = this.b;
            String strA = null;
            if (str != null) {
                eVar.getClass();
                String str2 = !wok.H(str) ? str : null;
                if (str2 != null) {
                    strA = e8n.a(eVar.m, ":", JSONObject.quote(str2));
                }
            }
            eVar.y = strA;
        }
        this.k = str;
    }

    public final i7f e(n1.b bVar, float f2, int i2, List<tnb> list) {
        bVar.getClass();
        list.getClass();
        boolean z = (bVar == n1.b.BUSINESS_CARD || bVar == n1.b.FIT_TO_PAPER) ? false : true;
        j7f j7fVar = (j7f) this.h.getValue();
        n1 n1VarA = n1.a.a(bVar);
        n1VarA.c = z;
        d6m d6mVar = d6m.a;
        return j7fVar.a(list, n1VarA, f2, i2);
    }

    public final void e0(ArrayList<qa2> arrayList) {
        arrayList.getClass();
        km3.f(new i(arrayList, null));
    }

    public final boolean equals(Object obj) {
        if (obj instanceof Page) {
            return this.a == ((Page) obj).a;
        }
        return false;
    }

    public final void f0(String str) {
        if (!cub.b(this.l, str)) {
            e eVar = this.b;
            String strA = null;
            if (str != null) {
                eVar.getClass();
                String str2 = !wok.H(str) ? str : null;
                if (str2 != null) {
                    strA = e8n.a(eVar.n, ":", JSONObject.quote(str2));
                }
            }
            eVar.z = strA;
        }
        this.l = str;
    }

    public final void g() {
        Iterator<T> it = v().iterator();
        while (it.hasNext()) {
            ((n) it.next()).getClass();
        }
    }

    public final void g0(com.adobe.dcmscan.document.b bVar) {
        this.e = bVar;
    }

    public final void h(n1.b bVar, Float f2) {
        float fFloatValue;
        if (bVar != null) {
            h0(bVar);
        }
        if (this.y == n1.b.FIT_TO_PAPER) {
            fFloatValue = 1.0f;
        } else {
            fFloatValue = f2 != null ? f2.floatValue() : this.A;
        }
        this.A = fFloatValue;
        this.B.setValue(Float.valueOf(fFloatValue));
        e eVar = this.b;
        eVar.s = eVar.f + ":" + tld.b(fFloatValue * 100.0f);
    }

    public final void h0(n1.b bVar) {
        String str;
        bVar.getClass();
        this.y = bVar;
        this.z.setValue(bVar);
        e eVar = this.b;
        String str2 = eVar.e;
        switch (b.a.a[bVar.ordinal()]) {
            case 1:
                str = "\"fit\"";
                break;
            case 2:
                str = "\"letter_portrait\"";
                break;
            case 3:
                str = "\"letter_landscape\"";
                break;
            case 4:
                str = "\"legal_portrait\"";
                break;
            case 5:
                str = "\"legal_landscape\"";
                break;
            case 6:
                str = "\"A3_portrait\"";
                break;
            case 7:
                str = "\"A3_landscape\"";
                break;
            case 8:
                str = "\"A4_portrait\"";
                break;
            case 9:
                str = "\"A4_landscape\"";
                break;
            case 10:
                str = "\"A5_portrait\"";
                break;
            case 11:
                str = "\"A5_landscape\"";
                break;
            case 12:
                str = "\"business_card\"";
                break;
            default:
                cm9.a();
                return;
        }
        eVar.r = e8n.a(str2, ":", str);
    }

    public final int hashCode() {
        return this.a;
    }

    public final boolean i() {
        List<n> listV = v();
        if (listV != null && listV.isEmpty()) {
            return false;
        }
        Iterator<T> it = listV.iterator();
        while (it.hasNext()) {
            if (((n) it.next()).g0()) {
                return true;
            }
        }
        return false;
    }

    public final void i0(boolean z) {
        this.m = z;
    }

    public final boolean j() {
        List<n> listV = v();
        if (listV != null && listV.isEmpty()) {
            return false;
        }
        Iterator<T> it = listV.iterator();
        while (it.hasNext()) {
            if (((n) it.next()).h0()) {
                return true;
            }
        }
        return false;
    }

    public final void j0(int i2) {
        String str;
        int i3 = this.v;
        if (i2 != 90 && i2 != 180 && i2 != 270) {
            i2 = 0;
        }
        this.v = i2;
        this.w.setValue(Integer.valueOf(i2));
        int i4 = this.v;
        e eVar = this.b;
        if (i4 != 0) {
            str = eVar.d + ":" + i4;
        } else {
            str = null;
        }
        eVar.q = str;
        if (i3 != -1) {
            com.adobe.dcmscan.document.b.a aVar = com.adobe.dcmscan.document.b.y;
            com.adobe.dcmscan.document.b.a.b(this.e, true, false);
            Iterator<T> it = v().iterator();
            while (it.hasNext()) {
                ((n) it.next()).k0();
            }
        }
    }

    public final int k() {
        n nVar = (n) x75.O(v());
        if (nVar != null) {
            return nVar.w();
        }
        return 0;
    }

    public final void k0(boolean z) {
        this.s = z;
    }

    public final CaptureMode l() {
        return this.p;
    }

    public final void l0() {
        this.f = false;
    }

    public final int m() {
        n nVar = (n) x75.O(v());
        if (nVar != null) {
            return nVar.B();
        }
        return 0;
    }

    public final void m0(Page page) {
        this.n = page;
    }

    public final boolean n() {
        List<n> listV = v();
        if (listV != null && listV.isEmpty()) {
            return false;
        }
        for (n nVar : listV) {
            hp4 hp4Var = nVar.c;
            if ((hp4Var != null && hp4Var.j) || nVar.l) {
                return true;
            }
        }
        return false;
    }

    public final void n0(String str) {
        zed.a.C0513a c0513aA;
        d0(str);
        String str2 = this.k;
        if (str2 == null || str2.length() <= 0) {
            return;
        }
        ArrayList<qa2> arrayList = this.j;
        arrayList.clear();
        String str3 = wa2.a;
        if (!TextUtils.isEmpty(str2) && wok.w(str2, ".ops", true)) {
            va2 va2Var = new va2();
            wa2.a.a(va2Var, str2);
            arrayList.addAll(va2Var.c);
        } else if (!TextUtils.isEmpty(str2) && wok.w(str2, ".marks", true) && (c0513aA = zed.a.a(str2)) != null) {
            arrayList.addAll(c0513aA.a);
        }
        Iterator<qa2> it = arrayList.iterator();
        while (it.hasNext()) {
            it.next().g = null;
        }
        this.i.setValue(Long.valueOf(SystemClock.elapsedRealtime()));
    }

    public final String o() {
        return this.o;
    }

    public final DocClassificationUtils.DocClassificationOutput p() {
        return this.x;
    }

    public final int q() {
        n nVar = (n) x75.O(v());
        if (nVar != null) {
            return nVar.H();
        }
        return -1;
    }

    public final boolean r() {
        return this.t;
    }

    public final boolean s() {
        return this.q;
    }

    public final int t() {
        return this.a;
    }

    public final float u() {
        return this.A;
    }

    public final List<n> v() {
        return (List) this.c.getValue();
    }

    public final c w() {
        return this.C;
    }

    public final Object x(boolean z, v36 v36Var) {
        ub7 ub7Var = p08.a;
        return km3.g(ua7.c, new k(this, z, null), v36Var);
    }

    public final n y() {
        return (n) x75.X(v());
    }

    public final ArrayList z() {
        ArrayList<qa2> arrayList = this.j;
        Iterator<qa2> it = arrayList.iterator();
        while (it.hasNext()) {
            it.next().g = null;
        }
        return arrayList;
    }

    public /* synthetic */ Page(File file, int i2, boolean z, hp4 hp4Var, CaptureMode captureMode, boolean z2, com.adobe.dcmscan.document.b bVar, boolean z3) {
        this(file, i2, z, hp4Var, captureMode, z2, null, bVar, z3);
    }

    public Page(File file, int i2, boolean z, hp4 hp4Var, CaptureMode captureMode, boolean z2, n.d dVar, com.adobe.dcmscan.document.b bVar, boolean z3) {
        file.getClass();
        captureMode.getClass();
        this.a = D.incrementAndGet();
        this.b = new e();
        this.c = nck.g(el8.a);
        this.f = true;
        this.h = new b3l(new e5a() { // from class: a4f
            @Override // defpackage.e5a
            public final Object invoke() {
                return Page.a(this.a);
            }
        });
        this.i = nck.g(0L);
        this.j = new ArrayList<>();
        this.u = true;
        this.w = nck.g(Integer.valueOf(this.v));
        n1.b bVar2 = n1.b.FIT_TO_PAPER;
        this.y = bVar2;
        this.z = nck.g(bVar2);
        this.A = 1.0f;
        this.B = nck.g(Float.valueOf(1.0f));
        this.e = bVar;
        this.u = z;
        O(file, i2, false, z2, hp4Var, captureMode, dVar, z3);
    }

    public Page(File file, int i2, Uri uri, com.adobe.dcmscan.document.b bVar, boolean z) {
        this(file, i2, true, null, bVar, z, 280);
        if (uri != null) {
            this.C = new c(uri);
            n nVar = v().get(0);
            nVar.getClass();
            f fVar = f.CONTENT_URI;
            n.r rVar = nVar.k;
            rVar.r = e8n.a(rVar.c, ":", rVar.q);
            String string = uri.toString();
            nVar.M = string;
            if (TextUtils.isEmpty(string)) {
                return;
            }
            rVar.t = e8n.a(rVar.b, ":", JSONObject.quote(nVar.M));
        }
    }
}
