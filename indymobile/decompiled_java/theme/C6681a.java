package com.indymobile.app.theme;

import android.os.Parcel;
import android.os.Parcelable;

/* JADX INFO: renamed from: com.indymobile.app.theme.a */
/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public final class C6681a implements Parcelable.Creator {
    @Override // android.os.Parcelable.Creator
    public final Object createFromParcel(Parcel parcel) {
        return new ThemeColorPreference.SavedState(parcel);
    }

    @Override // android.os.Parcelable.Creator
    public final Object[] newArray(int i) {
        return new ThemeColorPreference.SavedState[i];
    }
}
