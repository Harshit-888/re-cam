package com.indymobile.app.model.editor;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import androidx.annotation.Nullable;
import com.google.ads.mediation.inmobi.InMobiNetworkValues;
import com.indymobile.app.activity.editor.WorldData;
import com.indymobile.app.model.PSPage;
import com.mbridge.msdk.playercommon.exoplayer2.extractor.ogg.DefaultOggSeeker;
import com.mbridge.msdk.playercommon.exoplayer2.text.ttml.TtmlNode;
import com.mbridge.msdk.playercommon.exoplayer2.util.MimeTypes;
import com.nimbusds.jose.jwk.JWKParameterNames;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import p000.fu4;
import p000.fy3;
import p000.gy3;
import p000.ix3;
import p000.pu3;
import p000.s46;
import p000.sx3;
import p000.ud2;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSSticker implements Parcelable {
    public static final Parcelable.Creator<PSSticker> CREATOR = new C66751();
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_PAD = 0;
    public static final int TYPE_PAGE = 4;
    public static final int TYPE_SIGNATURE = 1;
    public static final int TYPE_TEXT = 2;

    /* JADX INFO: renamed from: a */
    @fu4("type")
    private int f31406a;

    /* JADX INFO: renamed from: b */
    @fu4("x")
    private float f31407b;

    /* JADX INFO: renamed from: c */
    @fu4(JWKParameterNames.ELLIPTIC_CURVE_Y_COORDINATE)
    private float f31408c;

    /* JADX INFO: renamed from: d */
    @fu4(InMobiNetworkValues.WIDTH)
    private int f31409d;

    /* JADX INFO: renamed from: e */
    @fu4(InMobiNetworkValues.HEIGHT)
    private int f31410e;

    /* JADX INFO: renamed from: f */
    @fu4("scale")
    private float f31411f;

    /* JADX INFO: renamed from: g */
    @fu4("scaleX")
    private float f31412g;

    /* JADX INFO: renamed from: h */
    @fu4("scaleY")
    private float f31413h;

    /* JADX INFO: renamed from: i */
    @fu4("rotate")
    private float f31414i;

    /* JADX INFO: renamed from: j */
    @fu4("rounded_corners")
    private int f31415j;

    /* JADX INFO: renamed from: k */
    @fu4(TtmlNode.ATTR_TTS_COLOR)
    private int f31416k;

    /* JADX INFO: renamed from: l */
    @fu4("opacity100")
    private int f31417l;

    /* JADX INFO: renamed from: m */
    @fu4("bright")
    private int f31418m;

    /* JADX INFO: renamed from: n */
    @fu4("contrast")
    private float f31419n;

    /* JADX INFO: renamed from: o */
    @fu4("saturation")
    private float f31420o;

    /* JADX INFO: renamed from: p */
    @Nullable
    @fu4("image_id")
    private String f31421p;

    /* JADX INFO: renamed from: q */
    @Nullable
    @fu4(MimeTypes.BASE_TYPE_TEXT)
    private String f31422q;

    /* JADX INFO: renamed from: r */
    @Nullable
    @fu4("font_name")
    private String f31423r;

    /* JADX INFO: renamed from: s */
    @Nullable
    @fu4("text_align")
    private TextAlign f31424s;

    /* JADX INFO: renamed from: t */
    @Nullable
    @fu4("page_id")
    private Integer f31425t;

    /* JADX INFO: renamed from: com.indymobile.app.model.editor.PSSticker$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66751 implements Parcelable.Creator<PSSticker> {
        @Override // android.os.Parcelable.Creator
        public final PSSticker createFromParcel(Parcel parcel) {
            return new PSSticker(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public final PSSticker[] newArray(int i) {
            return new PSSticker[i];
        }
    }

    /* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
    /* JADX WARN: Unknown enum class pattern. Please report as an issue! */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public static final class TextAlign {
        private static final /* synthetic */ TextAlign[] $VALUES;

        @fu4("2")
        public static final TextAlign TEXT_ALIGN_CENTER;

        @fu4("0")
        public static final TextAlign TEXT_ALIGN_LEFT;

        @fu4("1")
        public static final TextAlign TEXT_ALIGN_RIGHT;

        static {
            TextAlign textAlign = new TextAlign("TEXT_ALIGN_LEFT", 0);
            TEXT_ALIGN_LEFT = textAlign;
            TextAlign textAlign2 = new TextAlign("TEXT_ALIGN_RIGHT", 1);
            TEXT_ALIGN_RIGHT = textAlign2;
            TextAlign textAlign3 = new TextAlign("TEXT_ALIGN_CENTER", 2);
            TEXT_ALIGN_CENTER = textAlign3;
            $VALUES = new TextAlign[]{textAlign, textAlign2, textAlign3};
        }

        public static TextAlign valueOf(String str) {
            return (TextAlign) Enum.valueOf(TextAlign.class, str);
        }

        public static TextAlign[] values() {
            return (TextAlign[]) $VALUES.clone();
        }
    }

    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public PSSticker(Parcel parcel) {
        this.f31406a = parcel.readInt();
        this.f31407b = parcel.readFloat();
        this.f31408c = parcel.readFloat();
        this.f31409d = parcel.readInt();
        this.f31410e = parcel.readInt();
        this.f31411f = parcel.readFloat();
        this.f31412g = parcel.readFloat();
        this.f31413h = parcel.readFloat();
        this.f31417l = parcel.readInt();
        this.f31414i = parcel.readFloat();
        this.f31415j = parcel.readInt();
        this.f31416k = parcel.readInt();
        this.f31418m = parcel.readInt();
        this.f31419n = parcel.readFloat();
        this.f31420o = parcel.readFloat();
        this.f31421p = parcel.readString();
        this.f31422q = parcel.readString();
        int i = parcel.readInt();
        this.f31424s = i == -1 ? null : TextAlign.values()[i];
        int i2 = parcel.readInt();
        this.f31425t = i2 != -1 ? Integer.valueOf(i2) : null;
    }

    /* JADX INFO: renamed from: A */
    public final void m15903A(ix3 ix3Var) {
        Integer num = this.f31425t;
        if (num == null) {
            return;
        }
        sx3 sx3VarM31196n = ix3Var.m31196n(num.intValue());
        int i = sx3VarM31196n.f60751c - 1;
        sx3VarM31196n.f60751c = i;
        if (i <= 0) {
            Bitmap bitmap = sx3VarM31196n.f60750b;
            if (bitmap != null) {
                bitmap.recycle();
                sx3VarM31196n.f60750b = null;
            }
            sx3VarM31196n.f60751c = -1;
        }
    }

    /* JADX INFO: renamed from: B */
    public final void m15904B(gy3 gy3Var) {
        PSStickerImage pSStickerImageM30275u;
        String str = this.f31421p;
        if (str == null || (pSStickerImageM30275u = gy3Var.m30275u(str)) == null) {
            return;
        }
        fy3 fy3VarM30265k = gy3Var.m30265k(pSStickerImageM30275u);
        int i = fy3VarM30265k.f48772b - 1;
        fy3VarM30265k.f48772b = i;
        if (i <= 0) {
            Bitmap bitmap = fy3VarM30265k.f48771a;
            if (bitmap != null) {
                bitmap.recycle();
                fy3VarM30265k.f48771a = null;
            }
            fy3VarM30265k.f48772b = -1;
        }
    }

    /* JADX INFO: renamed from: C */
    public final Bitmap m15905C(ix3 ix3Var) {
        Integer num = this.f31425t;
        Bitmap bitmapM35288l = null;
        if (num == null) {
            return null;
        }
        sx3 sx3VarM31196n = ix3Var.m31196n(num.intValue());
        if (sx3VarM31196n.f60751c == -1 && sx3VarM31196n.f60750b == null) {
            PSPage pSPage = sx3VarM31196n.f60749a;
            if (pSPage != null) {
                try {
                    bitmapM35288l = s46.m35288l(ix3Var.f51528b ? 500 : DefaultOggSeeker.MATCH_BYTE_RANGE, pSPage.m15844d().getAbsolutePath());
                } catch (Exception unused) {
                }
            }
            sx3VarM31196n.f60750b = bitmapM35288l;
            sx3VarM31196n.f60751c = 0;
        }
        sx3VarM31196n.f60751c++;
        return sx3VarM31196n.f60750b;
    }

    /* JADX INFO: renamed from: D */
    public final Bitmap m15906D(gy3 gy3Var) {
        PSStickerImage pSStickerImageM30275u;
        String str = this.f31421p;
        if (str == null || (pSStickerImageM30275u = gy3Var.m30275u(str)) == null) {
            return null;
        }
        fy3 fy3VarM30265k = gy3Var.m30265k(pSStickerImageM30275u);
        if (fy3VarM30265k.f48772b == -1 && fy3VarM30265k.f48771a == null) {
            fy3VarM30265k.f48771a = gy3Var.m30264i(pSStickerImageM30275u);
            fy3VarM30265k.f48772b = 0;
        }
        fy3VarM30265k.f48772b++;
        return fy3VarM30265k.f48771a;
    }

    /* JADX INFO: renamed from: E */
    public final void m15907E(int i) {
        this.f31418m = i;
    }

    /* JADX INFO: renamed from: F */
    public final void m15908F(int i) {
        this.f31416k = i;
    }

    /* JADX INFO: renamed from: G */
    public final void m15909G(float f) {
        this.f31419n = f;
    }

    /* JADX INFO: renamed from: H */
    public final void m15910H(String str) {
        this.f31423r = str;
    }

    /* JADX INFO: renamed from: I */
    public final void m15911I(int i) {
        this.f31410e = i;
    }

    /* JADX INFO: renamed from: J */
    public final void m15912J(int i) {
        this.f31417l = i;
    }

    /* JADX INFO: renamed from: K */
    public final void m15913K(float f, float f2) {
        this.f31407b = f;
        this.f31408c = f2;
    }

    /* JADX INFO: renamed from: L */
    public final void m15914L(float f) {
        this.f31414i = f % 360.0f;
    }

    /* JADX INFO: renamed from: M */
    public final void m15915M(int i) {
        this.f31415j = i;
    }

    /* JADX INFO: renamed from: N */
    public final void m15916N(float f) {
        this.f31420o = f;
    }

    /* JADX INFO: renamed from: O */
    public final void m15917O(float f) {
        this.f31411f = f;
    }

    /* JADX INFO: renamed from: Q */
    public final void m15918Q(float f) {
        this.f31412g = f;
    }

    /* JADX INFO: renamed from: R */
    public final void m15919R(float f) {
        this.f31413h = f;
    }

    /* JADX INFO: renamed from: S */
    public final void m15920S(String str) {
        this.f31422q = str;
    }

    /* JADX INFO: renamed from: T */
    public final void m15921T(TextAlign textAlign) {
        this.f31424s = textAlign;
    }

    /* JADX INFO: renamed from: U */
    public final void m15922U(int i) {
        this.f31409d = i;
    }

    /* JADX INFO: renamed from: a */
    public final boolean m15923a(TextPaint textPaint) {
        float f;
        float textSize = textPaint.getTextSize();
        if (this.f31409d > 0) {
            String strM15942u = m15942u();
            int iRound = Math.round(Layout.getDesiredWidth(strM15942u, textPaint));
            float f2 = textSize;
            float f3 = 5.0f;
            int i = iRound;
            while (true) {
                int i2 = this.f31409d;
                if (i2 == iRound) {
                    break;
                }
                if (i2 <= iRound) {
                    f = f2 - f3;
                    if (f < 6.0f) {
                        textPaint.setTextSize(6.0f);
                        break;
                    }
                } else {
                    f = f2 + f3;
                }
                textPaint.setTextSize(f);
                int iRound2 = Math.round(Layout.getDesiredWidth(strM15942u, textPaint));
                int i3 = this.f31409d;
                if ((i3 <= iRound2 || f >= f2) && (i3 >= iRound2 || f <= f2)) {
                    f2 = f;
                    iRound = iRound2;
                } else {
                    if (i == iRound2) {
                        break;
                    }
                    f3 *= 0.5f;
                    i = iRound2;
                }
            }
        }
        return textSize != textPaint.getTextSize();
    }

    /* JADX INFO: renamed from: b */
    public final StaticLayout m15924b(TextPaint textPaint) {
        Layout.Alignment alignment;
        String strM15942u = m15942u();
        int iCeil = (int) Math.ceil(Layout.getDesiredWidth(strM15942u, textPaint));
        TextAlign textAlign = this.f31424s;
        if (textAlign == null) {
            alignment = Layout.Alignment.ALIGN_NORMAL;
        } else {
            int iOrdinal = textAlign.ordinal();
            if (iOrdinal != 1) {
                alignment = iOrdinal != 2 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER;
            } else {
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
            }
        }
        return StaticLayout.Builder.obtain(strM15942u, 0, strM15942u.length(), textPaint, iCeil).setAlignment(alignment).setLineSpacing(0.0f, 1.0f).setIncludePad(true).build();
    }

    /* JADX INFO: renamed from: c */
    public final int m15925c() {
        return this.f31418m;
    }

    /* JADX INFO: renamed from: d */
    public final int m15926d() {
        return this.f31416k;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    /* JADX INFO: renamed from: e */
    public final float m15927e() {
        return this.f31419n;
    }

    /* JADX INFO: renamed from: f */
    public final Paint m15928f() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int i = this.f31417l;
        if (i < 100) {
            paint.setAlpha((i * 255) / 100);
        }
        int i2 = this.f31406a;
        if (i2 == 0 || i2 == 1) {
            if (this.f31416k != -16777216) {
                paint.setColorFilter(new PorterDuffColorFilter(this.f31416k, PorterDuff.Mode.SRC_IN));
            }
        } else if ((i2 == 3 || i2 == 4) && (this.f31418m != 0 || this.f31419n != 1.0f || this.f31420o != 1.0f)) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(this.f31420o);
            ColorMatrix colorMatrix2 = new ColorMatrix();
            float f = this.f31419n;
            float f2 = this.f31418m;
            colorMatrix2.set(new float[]{f, 0.0f, 0.0f, 0.0f, f2, 0.0f, f, 0.0f, 0.0f, f2, 0.0f, 0.0f, f, 0.0f, f2, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
            colorMatrix.postConcat(colorMatrix2);
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            return paint;
        }
        return paint;
    }

    /* JADX INFO: renamed from: g */
    public final String m15929g() {
        return ud2.m36446u(this.f31423r) ? "Sans Serif" : this.f31423r;
    }

    /* JADX INFO: renamed from: h */
    public final int m15930h() {
        return this.f31410e;
    }

    /* JADX INFO: renamed from: i */
    public final String m15931i() {
        return this.f31421p;
    }

    /* JADX INFO: renamed from: j */
    public final Matrix m15932j(WorldData worldData, float f, float f2, float f3, float f4) {
        Matrix matrix = new Matrix();
        float f5 = worldData.f31147b;
        float f6 = this.f31407b * worldData.f31149d;
        float f7 = worldData.f31151f;
        float f8 = (f6 * f7) + f5;
        float f9 = (this.f31408c * worldData.f31150e * f7) + worldData.f31148c;
        matrix.preScale(f3, f4, f8, f9);
        matrix.preRotate(this.f31414i, f8, f9);
        matrix.preScale(this.f31412g, this.f31413h, f8, f9);
        float f10 = this.f31411f;
        matrix.preScale(f10, f10, f8, f9);
        matrix.preTranslate(f8 - ((f * f7) * 0.5f), f9 - ((f2 * f7) * 0.5f));
        float f11 = worldData.f31151f;
        matrix.preScale(f11, f11);
        return matrix;
    }

    /* JADX INFO: renamed from: k */
    public final int m15933k() {
        return this.f31417l;
    }

    /* JADX INFO: renamed from: l */
    public final Integer m15934l() {
        return this.f31425t;
    }

    /* JADX INFO: renamed from: m */
    public final float m15935m() {
        return this.f31414i;
    }

    /* JADX INFO: renamed from: n */
    public final int m15936n() {
        return this.f31415j;
    }

    /* JADX INFO: renamed from: o */
    public final float m15937o() {
        return this.f31420o;
    }

    /* JADX INFO: renamed from: q */
    public final float m15938q() {
        return this.f31411f;
    }

    /* JADX INFO: renamed from: r */
    public final float m15939r() {
        return this.f31412g;
    }

    /* JADX INFO: renamed from: s */
    public final float m15940s() {
        return this.f31413h;
    }

    /* JADX INFO: renamed from: t */
    public final TextPaint m15941t() {
        TextPaint textPaint = new TextPaint(1);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(80.0f);
        textPaint.setColor(this.f31416k);
        textPaint.setAlpha((this.f31417l * 255) / 100);
        textPaint.setTypeface(pu3.m34134d().m34135a(m15929g()));
        m15923a(textPaint);
        return textPaint;
    }

    /* JADX INFO: renamed from: u */
    public final String m15942u() {
        String str = this.f31422q;
        return str == null ? "" : str;
    }

    /* JADX INFO: renamed from: v */
    public final TextAlign m15943v() {
        return this.f31424s;
    }

    /* JADX INFO: renamed from: w */
    public final int m15944w() {
        return this.f31406a;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.f31406a);
        parcel.writeFloat(this.f31407b);
        parcel.writeFloat(this.f31408c);
        parcel.writeInt(this.f31409d);
        parcel.writeInt(this.f31410e);
        parcel.writeInt(this.f31417l);
        parcel.writeFloat(this.f31411f);
        parcel.writeFloat(this.f31412g);
        parcel.writeFloat(this.f31413h);
        parcel.writeFloat(this.f31414i);
        parcel.writeInt(this.f31415j);
        parcel.writeInt(this.f31416k);
        parcel.writeInt(this.f31418m);
        parcel.writeFloat(this.f31419n);
        parcel.writeFloat(this.f31420o);
        parcel.writeString(this.f31421p);
        parcel.writeString(this.f31422q);
        TextAlign textAlign = this.f31424s;
        parcel.writeInt(textAlign == null ? -1 : textAlign.ordinal());
        Integer num = this.f31425t;
        parcel.writeInt(num != null ? num.intValue() : -1);
    }

    /* JADX INFO: renamed from: x */
    public final int m15945x() {
        return this.f31409d;
    }

    /* JADX INFO: renamed from: y */
    public final void m15946y(float f, float f2) {
        this.f31407b += f;
        this.f31408c += f2;
    }

    /* JADX INFO: renamed from: z */
    public final void m15947z(float f) {
        double radians = Math.toRadians(f % 360.0f);
        float f2 = this.f31407b - 0.5f;
        float f3 = this.f31408c - 0.5f;
        this.f31407b = ((((float) Math.cos(radians)) * f2) - (((float) Math.sin(radians)) * f3)) + 0.5f;
        this.f31408c = (f3 * ((float) Math.cos(radians))) + (f2 * ((float) Math.sin(radians))) + 0.5f;
        this.f31414i = ((this.f31414i + 360.0f) + f) % 360.0f;
    }

    public PSSticker(int i, int i2) {
        this();
        this.f31409d = i;
        this.f31410e = i2;
    }

    public PSSticker(PSStickerImage pSStickerImage) {
        this(pSStickerImage.m15951d(), pSStickerImage.m15948a());
        int iM15950c = pSStickerImage.m15950c();
        if (iM15950c == 0) {
            this.f31406a = 0;
        } else if (iM15950c == 1) {
            this.f31406a = 1;
        } else if (iM15950c == 3) {
            this.f31406a = 3;
        }
        this.f31421p = pSStickerImage.m15949b();
    }

    public PSSticker(String str, int i) {
        this();
        this.f31406a = 2;
        this.f31422q = str;
        this.f31409d = i;
    }

    public PSSticker(int i, int i2, int i3) {
        this(i2, i3);
        this.f31406a = 4;
        this.f31425t = Integer.valueOf(i);
    }

    public PSSticker() {
        this.f31407b = 0.5f;
        this.f31408c = 0.5f;
        this.f31409d = 1;
        this.f31410e = 1;
        this.f31411f = 1.0f;
        this.f31412g = 1.0f;
        this.f31413h = 1.0f;
        this.f31414i = 0.0f;
        this.f31415j = 0;
        this.f31416k = -16777216;
        this.f31417l = 100;
        this.f31418m = 0;
        this.f31419n = 1.0f;
        this.f31420o = 1.0f;
        this.f31421p = null;
        this.f31422q = null;
        this.f31423r = null;
        this.f31424s = null;
        this.f31425t = null;
    }
}
