package com.indymobile.app.model;

import android.content.ContentValues;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSSyncStatusPage {
    public static final String TABLE_NAME = "sync_stats_page";
    public static int columnCount = 5;
    public String etagImageLocal;
    public String etagImageRemote;
    public String etagModelLocal;
    public String etagModelRemote;
    public int pageId;

    /* JADX INFO: renamed from: a */
    public final ContentValues m15864a() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("i_page_id", Integer.valueOf(this.pageId));
        contentValues.put("etag_model_local", this.etagModelLocal);
        contentValues.put("etag_model_remote", this.etagModelRemote);
        contentValues.put("etag_image_local", this.etagImageLocal);
        contentValues.put("etag_image_remote", this.etagImageRemote);
        return contentValues;
    }
}
