package com.indymobile.app.model;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSPaperSizeGroup {
    public static final String TABLE_NAME = "paper_size_group";
    public static int columnCount = 2;
    public int paperSizeGroupID;
    public String paperSizeGroupName;

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSPaperSize) && this.paperSizeGroupID == ((PSPaperSizeGroup) obj).paperSizeGroupID;
    }

    public final int hashCode() {
        return this.paperSizeGroupID;
    }
}
