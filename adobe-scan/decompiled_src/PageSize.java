package com.adobe.t5.pdf;

/* JADX INFO: loaded from: d:\Desktop\apk-rev\adobe-scan\extracted\classes3.dex */
public final class PageSize {
    public final float height;
    public final float width;

    private PageSize(float f, float f2) {
        this.width = f;
        this.height = f2;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == PageSize.class) {
            PageSize pageSize = (PageSize) obj;
            if (pageSize.width == this.width && pageSize.height == this.height) {
                return true;
            }
        }
        return false;
    }
}
