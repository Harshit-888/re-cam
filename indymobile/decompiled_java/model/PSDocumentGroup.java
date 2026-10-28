package com.indymobile.app.model;

import java.util.Date;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSDocumentGroup {
    public static final String TABLE_NAME = "document_group";
    public static int columnCount = 5;
    public Date dateCreate;
    public Date dateModify;
    public int documentGroupID;
    public String documentGroupName;
    public int order;

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSDocumentGroup) && this.documentGroupID == ((PSDocumentGroup) obj).documentGroupID;
    }

    public final int hashCode() {
        return this.documentGroupID;
    }
}
