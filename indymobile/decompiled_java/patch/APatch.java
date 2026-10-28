package com.indymobile.app.patch;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public abstract class APatch implements IPatch {

    /* JADX INFO: renamed from: a */
    public boolean f31444a;

    @Override // com.indymobile.app.patch.IPatch
    /* JADX INFO: renamed from: b */
    public final void mo15978b(boolean z) {
        this.f31444a = z;
    }

    @Override // com.indymobile.app.patch.IPatch
    public final String getId() {
        return getClass().getName();
    }

    @Override // com.indymobile.app.patch.IPatch
    public boolean isDone() {
        return this.f31444a;
    }
}
