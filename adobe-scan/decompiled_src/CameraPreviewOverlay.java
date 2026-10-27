package defpackage;

import com.adobe.scan.android.R;

/* JADX INFO: compiled from: CameraPreviewOverlay.kt */
/* JADX INFO: loaded from: classes.dex */
public abstract class f4g {
    public final int a;

    /* JADX INFO: compiled from: CameraPreviewOverlay.kt */
    /* JADX INFO: loaded from: classes2.dex */
    public static final class a extends f4g {
        public static final a b = new a(R.string.camera_permission_required);

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof a);
        }

        public final int hashCode() {
            return 343830858;
        }

        public final String toString() {
            return "CameraPermissionsNeeded";
        }
    }

    /* JADX INFO: compiled from: CameraPreviewOverlay.kt */
    /* JADX INFO: loaded from: classes2.dex */
    public static final class b extends f4g {
        public static final b b = new b(R.string.camera_sleeping_message);

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof b);
        }

        public final int hashCode() {
            return -1004616038;
        }

        public final String toString() {
            return "CameraSleeping";
        }
    }

    /* JADX INFO: compiled from: CameraPreviewOverlay.kt */
    /* JADX INFO: loaded from: classes2.dex */
    public static final class c extends f4g {
        public static final c b = new c(R.string.multiwindow_not_supported_message);

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof c);
        }

        public final int hashCode() {
            return -1465032554;
        }

        public final String toString() {
            return "MultiWindowUnsupported";
        }
    }

    /* JADX INFO: compiled from: CameraPreviewOverlay.kt */
    public static final class d extends f4g {
        public static final d b = new d(0);

        public final boolean equals(Object obj) {
            return this == obj || (obj instanceof d);
        }

        public final int hashCode() {
            return -1436005790;
        }

        public final String toString() {
            return "None";
        }
    }

    public f4g(int i) {
        this.a = i;
    }
}
