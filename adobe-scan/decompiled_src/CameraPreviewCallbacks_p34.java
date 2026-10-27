package defpackage;

import android.view.View;
import androidx.camera.core.e;

/* JADX INFO: compiled from: CameraPreview.kt */
/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes.dex */
public final class p34 {
    public final g5a<pdb, d6m> a;
    public final g5a<e, d6m> b;
    public final g5a<j0h, d6m> c;
    public final w5a<kx3, Integer, Boolean, d6m> d;
    public final e5a<d6m> e;
    public final g5a<q7g.e, d6m> f;
    public final View.OnTouchListener g;

    /* JADX WARN: Multi-variable type inference failed */
    public p34(g5a<? super pdb, d6m> g5aVar, g5a<? super e, d6m> g5aVar2, g5a<? super j0h, d6m> g5aVar3, w5a<? super kx3, ? super Integer, ? super Boolean, d6m> w5aVar, e5a<d6m> e5aVar, g5a<? super q7g.e, d6m> g5aVar4, View.OnTouchListener onTouchListener) {
        this.a = g5aVar;
        this.b = g5aVar2;
        this.c = g5aVar3;
        this.d = w5aVar;
        this.e = e5aVar;
        this.f = g5aVar4;
        this.g = onTouchListener;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof p34)) {
            return false;
        }
        p34 p34Var = (p34) obj;
        return cub.b(this.a, p34Var.a) && cub.b(this.b, p34Var.b) && cub.b(this.c, p34Var.c) && cub.b(this.d, p34Var.d) && cub.b(this.e, p34Var.e) && cub.b(this.f, p34Var.f) && cub.b(this.g, p34Var.g);
    }

    public final int hashCode() {
        return this.g.hashCode() + tu2.a(this.f, mn8.a((this.d.hashCode() + tu2.a(this.c, tu2.a(this.b, this.a.hashCode() * 31, 31), 31)) * 31, 31, this.e), 31);
    }

    public final String toString() {
        return "CameraPreviewCallbacks(onImageCaptureChanged=" + this.a + ", onImageAvailable=" + this.b + ", cameraPreviewRect=" + this.c + ", onCameraBoundToLifecycle=" + this.d + ", onCameraUnbound=" + this.e + ", onCameraPreviewStreamState=" + this.f + ", cameraAdornTouchListener=" + this.g + ")";
    }
}
