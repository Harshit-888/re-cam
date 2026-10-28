package com.indymobile.app.patch;

import p000.iy3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PatchStorage extends APatch {
    @Override // com.indymobile.app.patch.IPatch
    /* JADX INFO: renamed from: a */
    public final void mo15979a() throws Throwable {
        iy3.m31217b().m31220d();
    }

    @Override // com.indymobile.app.patch.APatch, com.indymobile.app.patch.IPatch
    public final boolean isDone() {
        return !iy3.m31217b().m31219c();
    }
}
