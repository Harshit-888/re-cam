package com.indymobile.app.model;

import p000.fu4;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSLauncher {

    @fu4("className")
    public String className;

    @fu4("packageName")
    public String packageName;

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PSLauncher)) {
            return false;
        }
        PSLauncher pSLauncher = (PSLauncher) obj;
        String str = this.packageName;
        String str2 = pSLauncher.packageName;
        boolean z = str == str2 || (str != null && str.equals(str2));
        String str3 = this.className;
        String str4 = pSLauncher.className;
        return z && (str3 == str4 || (str3 != null && str3.equals(str4)));
    }
}
