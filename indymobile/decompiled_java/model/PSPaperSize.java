package com.indymobile.app.model;

import android.database.Cursor;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSPaperSize {
    public static final String TABLE_NAME = "paper_size";
    public static int columnCount = 8;
    public int heightInPoint;
    public boolean isBuiltin;
    public boolean isShowInSelectList;
    public int paperSizeGroupID;
    public int paperSizeID;
    public String paperSizeName;
    public String unitDisplay;
    public int widthInPoint;

    public PSPaperSize(Cursor cursor) {
        this.paperSizeID = cursor.getInt(0);
        this.paperSizeName = cursor.getString(1);
        this.widthInPoint = cursor.getInt(2);
        this.heightInPoint = cursor.getInt(3);
        this.isBuiltin = cursor.getInt(4) != 0;
        this.isShowInSelectList = cursor.getInt(5) != 0;
        this.paperSizeGroupID = cursor.getInt(6);
        this.unitDisplay = cursor.getString(7);
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSPaperSize) && this.paperSizeID == ((PSPaperSize) obj).paperSizeID;
    }

    public final int hashCode() {
        return this.paperSizeID;
    }

    public PSPaperSize() {
    }
}
