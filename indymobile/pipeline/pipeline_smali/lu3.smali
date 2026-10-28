.class public final Llu3;
.super Lpr1;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# instance fields
.field public final b:Landroid/graphics/Bitmap;

.field public final c:Lcv3;

.field public d:Lry;

.field public final e:Lyu3;


# direct methods
.method public constructor <init>(Landroid/graphics/Bitmap;Lcv3;Lyu3;Ldf1;)V
    .locals 0

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    iput-object p1, p0, Llu3;->b:Landroid/graphics/Bitmap;

    .line 5
    .line 6
    iput-object p2, p0, Llu3;->c:Lcv3;

    .line 7
    .line 8
    iput-object p3, p0, Llu3;->e:Lyu3;

    .line 9
    .line 10
    iput-object p4, p0, Lpr1;->a:Ljava/lang/Object;

    .line 11
    .line 12
    return-void
.end method

.method public static E(Landroid/graphics/Bitmap;Lcv3;Lyu3;)Lry;
    .locals 11

    .line 1
    new-instance v0, Lorg/opencv/core/Mat;

    .line 2
    .line 3
    invoke-direct {v0}, Lorg/opencv/core/Mat;-><init>()V

    .line 4
    .line 5
    .line 6
    new-instance v1, Lorg/opencv/core/Mat;

    .line 7
    .line 8
    invoke-direct {v1}, Lorg/opencv/core/Mat;-><init>()V

    .line 9
    .line 10
    .line 11
    invoke-static {p0, v0}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 12
    .line 13
    .line 14
    sget-object p0, Lcv3;->kPSEnhanceTypeDocumentLevel2:Lcv3;

    .line 15
    .line 16
    const/4 v2, 0x2

    .line 17
    const/4 v3, 0x1

    .line 18
    if-ne p1, p0, :cond_0

    .line 19
    .line 20
    move v9, v2

    .line 21
    goto :goto_0

    .line 22
    :cond_0
    move v9, v3

    .line 23
    :goto_0
    sget-object p0, Lyu3;->kPSColorTypeGrayScale:Lyu3;

    .line 24
    .line 25
    if-ne p2, p0, :cond_1

    .line 26
    .line 27
    move v10, v2

    .line 28
    goto :goto_1

    .line 29
    :cond_1
    move v10, v3

    .line 30
    :goto_1
    sget-object v4, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 31
    .line 32
    iget-wide v5, v0, Lorg/opencv/core/Mat;->a:J

    .line 33
    .line 34
    iget-wide v7, v1, Lorg/opencv/core/Mat;->a:J

    .line 35
    .line 36
    invoke-static/range {v4 .. v10}, Lcom/indymobile/app/PSOpenCV;->enhanceDocument(Landroid/content/Context;JJII)I

    .line 37
    .line 38
    .line 39
    move-result p0

    .line 40
    shr-int/lit8 p1, p0, 0x10

    .line 41
    .line 42
    and-int/lit16 p1, p1, 0xff

    .line 43
    .line 44
    shr-int/lit8 p2, p0, 0x8

    .line 45
    .line 46
    and-int/lit16 p2, p2, 0xff

    .line 47
    .line 48
    and-int/lit16 p0, p0, 0xff

    .line 49
    .line 50
    invoke-static {p1, p2, p0}, Landroid/graphics/Color;->rgb(III)I

    .line 51
    .line 52
    .line 53
    move-result p0

    .line 54
    invoke-virtual {v1}, Lorg/opencv/core/Mat;->a()I

    .line 55
    .line 56
    .line 57
    move-result p1

    .line 58
    invoke-virtual {v1}, Lorg/opencv/core/Mat;->e()I

    .line 59
    .line 60
    .line 61
    move-result p2

    .line 62
    sget-object v0, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 63
    .line 64
    invoke-static {p1, p2, v0}, Landroid/graphics/Bitmap;->createBitmap(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 65
    .line 66
    .line 67
    move-result-object p1

    .line 68
    invoke-static {p1, v1}, Lorg/opencv/android/Utils;->b(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 69
    .line 70
    .line 71
    new-instance p2, Lry;

    .line 72
    .line 73
    invoke-direct {p2, p1, p0}, Lry;-><init>(Landroid/graphics/Bitmap;I)V

    .line 74
    .line 75
    .line 76
    return-object p2
.end method


# virtual methods
.method public final n(Lfq4;)V
    .locals 3

    .line 1
    new-instance v0, Ltt3;

    .line 2
    .line 3
    const/4 v1, 0x5

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
    const/4 v1, 0x1

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
