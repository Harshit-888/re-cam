.class public final Lqx3;
.super Lpr1;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# instance fields
.field public b:Landroid/graphics/Bitmap;

.field public c:Lry;

.field public d:Lyu3;


# direct methods
.method public static E(Landroid/graphics/Bitmap;Lyu3;)Lry;
    .locals 3

    .line 1
    sget-object v0, Lyu3;->kPSColorTypeColor:Lyu3;

    .line 2
    .line 3
    const/4 v1, -0x1

    .line 4
    if-ne p1, v0, :cond_0

    .line 5
    .line 6
    new-instance p1, Lry;

    .line 7
    .line 8
    invoke-direct {p1, p0, v1}, Lry;-><init>(Landroid/graphics/Bitmap;I)V

    .line 9
    .line 10
    .line 11
    return-object p1

    .line 12
    :cond_0
    new-instance p1, Lorg/opencv/core/Mat;

    .line 13
    .line 14
    invoke-direct {p1}, Lorg/opencv/core/Mat;-><init>()V

    .line 15
    .line 16
    .line 17
    invoke-static {p0, p1}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 18
    .line 19
    .line 20
    invoke-static {p1, p1}, Lorg/opencv/imgproc/Imgproc;->a(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;)V

    .line 21
    .line 22
    .line 23
    const/16 p0, 0x9

    .line 24
    .line 25
    invoke-static {p1, p1, p0}, Lorg/opencv/imgproc/Imgproc;->b(Lorg/opencv/core/Mat;Lorg/opencv/core/Mat;I)V

    .line 26
    .line 27
    .line 28
    invoke-virtual {p1}, Lorg/opencv/core/Mat;->a()I

    .line 29
    .line 30
    .line 31
    move-result p0

    .line 32
    invoke-virtual {p1}, Lorg/opencv/core/Mat;->e()I

    .line 33
    .line 34
    .line 35
    move-result v0

    .line 36
    sget-object v2, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 37
    .line 38
    invoke-static {p0, v0, v2}, Landroid/graphics/Bitmap;->createBitmap(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 39
    .line 40
    .line 41
    move-result-object p0

    .line 42
    invoke-static {p0, p1}, Lorg/opencv/android/Utils;->b(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 43
    .line 44
    .line 45
    new-instance p1, Lry;

    .line 46
    .line 47
    invoke-direct {p1, p0, v1}, Lry;-><init>(Landroid/graphics/Bitmap;I)V

    .line 48
    .line 49
    .line 50
    return-object p1
.end method


# virtual methods
.method public final n(Lfq4;)V
    .locals 3

    .line 1
    new-instance v0, Ldu3;

    .line 2
    .line 3
    const/4 v1, 0x7

    .line 4
    invoke-direct {v0, v1, p0}, Ldu3;-><init>(ILjava/lang/Object;)V

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
    new-instance v0, Lmu3;

    .line 26
    .line 27
    const/4 v1, 0x5

    .line 28
    invoke-direct {v0, v1, p0}, Lmu3;-><init>(ILjava/lang/Object;)V

    .line 29
    .line 30
    .line 31
    invoke-virtual {p1, v0}, Lgo3;->b(Lyo3;)V

    .line 32
    .line 33
    .line 34
    return-void
.end method
