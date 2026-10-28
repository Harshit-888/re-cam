package com.indymobile.app.theme;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.AbsSavedState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.AbstractActivityC0619o;
import androidx.preference.Preference;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.indymobileapp.document.scanner.R;
import p000.c64;
import p000.f93;
import p000.n35;
import p000.uc1;
import p000.v63;
import p000.y83;
import p000.zy3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class ThemeColorPreference extends Preference {

    /* JADX INFO: renamed from: M */
    public int f31464M;

    public ThemeColorPreference(Context context) {
        super(context, null);
        this.f31464M = zy3.f67262f[0];
        this.f16141E = R.layout.pref_color_layout;
    }

    /* JADX INFO: renamed from: B */
    public final void m16007B(int i) {
        boolean zMo3448z = mo3448z();
        this.f31464M = i;
        m3466u(i);
        boolean zMo3448z2 = mo3448z();
        if (zMo3448z2 != zMo3448z) {
            mo3462j(zMo3448z2);
        }
        mo3442i();
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: m */
    public final void mo3438m(c64 c64Var) {
        GradientDrawable gradientDrawable;
        super.mo3438m(c64Var);
        View viewM4644a = c64Var.m4644a(R.id.color_view);
        if (viewM4644a instanceof ImageView) {
            ImageView imageView = (ImageView) viewM4644a;
            int i = this.f31464M;
            Resources resources = imageView.getContext().getResources();
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof GradientDrawable) {
                gradientDrawable = (GradientDrawable) drawable;
            } else {
                gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(1);
            }
            int iRgb = Color.rgb((Color.red(i) * 192) / NotificationCompat.FLAG_LOCAL_ONLY, (Color.green(i) * 192) / NotificationCompat.FLAG_LOCAL_ONLY, (Color.blue(i) * 192) / NotificationCompat.FLAG_LOCAL_ONLY);
            gradientDrawable.setColor(i);
            gradientDrawable.setStroke((int) TypedValue.applyDimension(1, 2.0f, resources.getDisplayMetrics()), iRgb);
            imageView.setImageDrawable(gradientDrawable);
        }
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: n */
    public final void mo3440n() {
        int i;
        AbstractActivityC0619o abstractActivityC0619o = (AbstractActivityC0619o) this.f16149a;
        y83 y83Var = new y83(abstractActivityC0619o);
        y83Var.f65525D = true;
        y83Var.f65526E = true;
        y83Var.f65550b = abstractActivityC0619o.getText(R.string.menu_theme_color);
        y83Var.m38056b(LayoutInflater.from(abstractActivityC0619o).inflate(R.layout.dialog_color_theme, (ViewGroup) null), true);
        y83Var.m38062h();
        f93 f93Var = new f93(y83Var);
        RecyclerView recyclerView = (RecyclerView) f93Var.f47957c.f65566p.findViewById(R.id.list_color);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager());
        recyclerView.m3536f(new n35(abstractActivityC0619o.getResources().getDimensionPixelSize(R.dimen.theme_color_div)));
        uc1 uc1Var = new uc1(this);
        recyclerView.setAdapter(uc1Var);
        uc1Var.f62147b = new v63(17, this, f93Var);
        int i2 = this.f31464M;
        int[] iArr = zy3.f67262f;
        int i3 = 0;
        if (iArr != null) {
            i = 0;
            while (true) {
                if (i >= iArr.length) {
                    i = -1;
                    break;
                } else if (i2 == iArr[i]) {
                    break;
                } else {
                    i++;
                }
            }
        } else {
            i = -1;
            break;
        }
        if (i >= 0 && i < 15) {
            i3 = i;
        }
        recyclerView.m3537f0(i3);
        f93Var.show();
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: p */
    public final Object mo3444p(TypedArray typedArray, int i) {
        return typedArray.getString(i);
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: q */
    public final void mo3445q(Parcelable parcelable) {
        if (!parcelable.getClass().equals(SavedState.class)) {
            super.mo3445q(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.mo3445q(savedState.getSuperState());
        m16007B(savedState.f31465a);
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: r */
    public final Parcelable mo3446r() {
        super.mo3446r();
        AbsSavedState absSavedState = AbsSavedState.EMPTY_STATE;
        if (this.f16165q) {
            return absSavedState;
        }
        SavedState savedState = new SavedState();
        savedState.f31465a = this.f31464M;
        return savedState;
    }

    @Override // androidx.preference.Preference
    /* JADX INFO: renamed from: s */
    public final void mo3447s(Object obj) {
        if (obj == null) {
            obj = Integer.valueOf(zy3.f67262f[0]);
        }
        m16007B(m3459d(((Integer) obj).intValue()));
    }

    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new C6681a();

        /* JADX INFO: renamed from: a */
        public int f31465a;

        public SavedState(Parcel parcel) {
            super(parcel);
            this.f31465a = parcel.readInt();
        }

        @Override // android.view.AbsSavedState, android.os.Parcelable
        public final void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.f31465a);
        }

        public SavedState() {
            super(AbsSavedState.EMPTY_STATE);
        }
    }

    public ThemeColorPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.f31464M = zy3.f67262f[0];
        this.f16141E = R.layout.pref_color_layout;
    }

    public ThemeColorPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.f31464M = zy3.f67262f[0];
        this.f16141E = R.layout.pref_color_layout;
    }

    public ThemeColorPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.f31464M = zy3.f67262f[0];
        this.f16141E = R.layout.pref_color_layout;
    }
}
