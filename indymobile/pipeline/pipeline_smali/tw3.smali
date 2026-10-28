.class public final Ltw3;
.super Lpr1;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# instance fields
.field public b:Landroid/graphics/Bitmap;

.field public c:Lyu3;

.field public d:Lry;

.field public e:F

.field public f:F


# direct methods
.method public static E(Landroid/graphics/Bitmap;FFLyu3;)Lry;
    .locals 9

    .line 1
    new-instance v0, Lorg/opencv/core/Mat;

    .line 2
    .line 3
    invoke-direct {v0}, Lorg/opencv/core/Mat;-><init>()V

    .line 4
    .line 5
    .line 6
    invoke-static {p0, v0}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 7
    .line 8
    .line 9
    new-instance p0, Lorg/opencv/core/Mat;

    .line 10
    .line 11
    invoke-virtual {v0}, Lorg/opencv/core/Mat;->e()I

    .line 12
    .line 13
    .line 14
    move-result v1

    .line 15
    invoke-virtual {v0}, Lorg/opencv/core/Mat;->a()I

    .line 16
    .line 17
    .line 18
    move-result v2

    .line 19
    invoke-virtual {v0}, Lorg/opencv/core/Mat;->f()I

    .line 20
    .line 21
    .line 22
    move-result v3

    .line 23
    invoke-direct {p0, v1, v2, v3}, Lorg/opencv/core/Mat;-><init>(III)V

    .line 24
    .line 25
    .line 26
    sget-object v1, Lyu3;->kPSColorTypeGrayScale:Lyu3;

    .line 27
    .line 28
    if-ne p3, v1, :cond_0

    .line 29
    .line 30
    const/4 p3, 0x2

    .line 31
    :goto_0
    move v8, p3

    .line 32
    goto :goto_1

    .line 33
    :cond_0
    const/4 p3, 0x1

    .line 34
    goto :goto_0

    .line 35
    :goto_1
    sget-object v1, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 36
    .line 37
    float-to-int v6, p2

    .line 38
    float-to-int v7, p1

    .line 39
    iget-wide v2, v0, Lorg/opencv/core/Mat;->a:J

    .line 40
    .line 41
    iget-wide v4, p0, Lorg/opencv/core/Mat;->a:J

    .line 42
    .line 43
    invoke-static/range {v1 .. v8}, Lcom/indymobile/app/PSOpenCV;->enhancePhoto(Landroid/content/Context;JJIII)V

    .line 44
    .line 45
    .line 46
    invoke-virtual {p0}, Lorg/opencv/core/Mat;->a()I

    .line 47
    .line 48
    .line 49
    move-result p1

    .line 50
    invoke-virtual {p0}, Lorg/opencv/core/Mat;->e()I

    .line 51
    .line 52
    .line 53
    move-result p2

    .line 54
    sget-object p3, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 55
    .line 56
    invoke-static {p1, p2, p3}, Landroid/graphics/Bitmap;->createBitmap(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 57
    .line 58
    .line 59
    move-result-object p1

    .line 60
    invoke-static {p1, p0}, Lorg/opencv/android/Utils;->b(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 61
    .line 62
    .line 63
    new-instance p0, Lry;

    .line 64
    .line 65
    const/4 p2, -0x1

    .line 66
    invoke-direct {p0, p1, p2}, Lry;-><init>(Landroid/graphics/Bitmap;I)V

    .line 67
    .line 68
    .line 69
    return-object p0
.end method


# virtual methods
.method public final n(Lfq4;)V
    .locals 3

    .line 1
    new-instance v0, Ltt3;

    .line 2
    .line 3
    const/4 v1, 0x7

    .line 4
    invoke-direct {v0, v1, p0}, Ltt3;-><init>(ILjava/lang/Object;)V

    .line 5
    .line 6
    .line 7
    new-instance v1, Ljo3;

    .line 8
    .line 9
    const/4 v2, 0x0

    .line 10
    invoke-direct {v1, v2, v0}, Ljo3;-><init>(ILjava/lang/Object;)V

    .line 11
    .line 12
    .line 13
    invoke-virtual {v1, p1}, Lgo3;->d(Lfq4;)Lto3;

    .line 14
    .line 15
    .line 16
    move-result-object p1

    .line 17
    invoke-static {}, Lpd;->a()Lfq4;

    .line 18
    .line 19
    .line 20
    move-result-object v0

    .line 21
    invoke-virtual {p1, v0}, Lgo3;->a(Lfq4;)Lno3;

    .line 22
    .line 23
    .line 24
    move-result-object p1

    .line 25
    new-instance v0, Ldu3;

    .line 26
    .line 27
    const/4 v1, 0x3

    .line 28
    invoke-direct {v0, v1, p0}, Ldu3;-><init>(ILjava/lang/Object;)V

    .line 29
    .line 30
    .line 31
    invoke-virtual {p1, v0}, Lgo3;->b(Lyo3;)V

    .line 32
    .line 33
    .line 34
    return-void
.end method
