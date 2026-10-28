.class public abstract Lwv3;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# static fields
.field public static final a:Ljava/util/Date;

.field public static final b:I

.field public static final c:I

.field public static final d:I

.field public static final e:I

.field public static final f:I

.field public static final g:I


# direct methods
.method static constructor <clinit>()V
    .locals 4

    .line 1
    new-instance v0, Ljava/util/Date;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/util/Date;-><init>()V

    .line 4
    .line 5
    .line 6
    sput-object v0, Lwv3;->a:Ljava/util/Date;

    .line 7
    .line 8
    const-string v0, "22.03.1999 13:06:07"

    .line 9
    .line 10
    new-instance v1, Ljava/text/SimpleDateFormat;

    .line 11
    .line 12
    const-string v2, "dd.MM.yyyy HH:mm:ss"

    .line 13
    .line 14
    sget-object v3, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    .line 15
    .line 16
    invoke-direct {v1, v2, v3}, Ljava/text/SimpleDateFormat;-><init>(Ljava/lang/String;Ljava/util/Locale;)V

    .line 17
    .line 18
    .line 19
    :try_start_0
    invoke-virtual {v1, v0}, Ljava/text/DateFormat;->parse(Ljava/lang/String;)Ljava/util/Date;

    .line 20
    .line 21
    .line 22
    move-result-object v0

    .line 23
    sput-object v0, Lwv3;->a:Ljava/util/Date;
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 24
    .line 25
    goto :goto_0

    .line 26
    :catch_0
    move-exception v0

    .line 27
    invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V

    .line 28
    .line 29
    .line 30
    :goto_0
    const/16 v0, 0x50

    .line 31
    .line 32
    sput v0, Lwv3;->b:I

    .line 33
    .line 34
    sput v0, Lwv3;->c:I

    .line 35
    .line 36
    const/16 v0, 0x46

    .line 37
    .line 38
    sput v0, Lwv3;->d:I

    .line 39
    .line 40
    const/16 v0, 0x3c

    .line 41
    .line 42
    sput v0, Lwv3;->e:I

    .line 43
    .line 44
    const/16 v0, 0x64

    .line 45
    .line 46
    sput v0, Lwv3;->f:I

    .line 47
    .line 48
    const/16 v0, 0x78

    .line 49
    .line 50
    sput v0, Lwv3;->g:I

    .line 51
    .line 52
    return-void
.end method

.method public static a(Landroid/app/Activity;Lcom/indymobile/app/util/PSException;)V
    .locals 9

    .line 1
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 6
    .line 7
    .line 8
    move-result-object v1

    .line 9
    iget v1, v1, Ldy3;->a0:I

    .line 10
    .line 11
    add-int/lit8 v1, v1, 0x1

    .line 12
    .line 13
    iput v1, v0, Ldy3;->a0:I

    .line 14
    .line 15
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 16
    .line 17
    .line 18
    move-result-object v0

    .line 19
    invoke-virtual {v0}, Ldy3;->e()V

    .line 20
    .line 21
    .line 22
    invoke-virtual {p1}, Ljava/lang/Throwable;->getCause()Ljava/lang/Throwable;

    .line 23
    .line 24
    .line 25
    move-result-object v0

    .line 26
    if-eqz v0, :cond_0

    .line 27
    .line 28
    invoke-virtual {v0}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;

    .line 29
    .line 30
    .line 31
    move-result-object v1

    .line 32
    invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V

    .line 33
    .line 34
    .line 35
    invoke-static {v0}, Lws3;->e(Ljava/lang/Throwable;)V

    .line 36
    .line 37
    .line 38
    goto :goto_0

    .line 39
    :cond_0
    invoke-virtual {p1}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;

    .line 40
    .line 41
    .line 42
    move-result-object v1

    .line 43
    invoke-virtual {p1}, Ljava/lang/Throwable;->printStackTrace()V

    .line 44
    .line 45
    .line 46
    invoke-static {p1}, Lws3;->e(Ljava/lang/Throwable;)V

    .line 47
    .line 48
    .line 49
    :goto_0
    new-instance v0, Ljava/lang/StringBuilder;

    .line 50
    .line 51
    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 52
    .line 53
    .line 54
    const v2, 0x7f120307

    .line 55
    .line 56
    .line 57
    invoke-static {v2}, Lwv3;->b(I)Ljava/lang/String;

    .line 58
    .line 59
    .line 60
    move-result-object v2

    .line 61
    const v3, 0x7f1200da

    .line 62
    .line 63
    .line 64
    invoke-static {v3}, Lwv3;->b(I)Ljava/lang/String;

    .line 65
    .line 66
    .line 67
    move-result-object v3

    .line 68
    filled-new-array {v3}, [Ljava/lang/Object;

    .line 69
    .line 70
    .line 71
    move-result-object v3

    .line 72
    invoke-static {v2, v3}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    .line 73
    .line 74
    .line 75
    move-result-object v2

    .line 76
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 77
    .line 78
    .line 79
    const-string v2, "\n\nCaused by :"

    .line 80
    .line 81
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 82
    .line 83
    .line 84
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 85
    .line 86
    .line 87
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 88
    .line 89
    .line 90
    move-result-object v6

    .line 91
    new-instance v3, Lyw;

    .line 92
    .line 93
    const/4 v4, 0x7

    .line 94
    const/4 v8, 0x0

    .line 95
    move-object v5, p0

    .line 96
    move-object v7, p1

    .line 97
    invoke-direct/range {v3 .. v8}, Lyw;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Z)V

    .line 98
    .line 99
    .line 100
    invoke-virtual {v5, v3}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 101
    .line 102
    .line 103
    return-void
.end method

.method public static b(I)Ljava/lang/String;
    .locals 1

    .line 1
    sget-object v0, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 2
    .line 3
    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    .line 4
    .line 5
    .line 6
    move-result-object v0

    .line 7
    invoke-virtual {v0, p0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    .line 8
    .line 9
    .line 10
    move-result-object p0

    .line 11
    return-object p0
.end method

.method public static c(Landroid/app/Activity;)V
    .locals 1

    .line 1
    if-eqz p0, :cond_0

    .line 2
    .line 3
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    .line 4
    .line 5
    .line 6
    move-result-object p0

    .line 7
    const/16 v0, 0x10

    .line 8
    .line 9
    invoke-virtual {p0, v0, v0}, Landroid/view/Window;->setFlags(II)V

    .line 10
    .line 11
    .line 12
    :cond_0
    return-void
.end method

.method public static d(Landroid/app/Activity;)V
    .locals 1

    .line 1
    if-eqz p0, :cond_0

    .line 2
    .line 3
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    .line 4
    .line 5
    .line 6
    move-result-object p0

    .line 7
    const/16 v0, 0x10

    .line 8
    .line 9
    invoke-virtual {p0, v0}, Landroid/view/Window;->clearFlags(I)V

    .line 10
    .line 11
    .line 12
    :cond_0
    return-void
.end method

.method public static e(Landroid/content/Context;Landroid/widget/Toast;)V
    .locals 2

    .line 1
    const/high16 v0, 0x42700000    # 60.0f

    .line 2
    .line 3
    invoke-static {p0, v0}, Loe6;->e(Landroid/content/Context;F)F

    .line 4
    .line 5
    .line 6
    move-result p0

    .line 7
    float-to-int p0, p0

    .line 8
    const/16 v0, 0x51

    .line 9
    .line 10
    const/4 v1, 0x0

    .line 11
    invoke-virtual {p1, v0, v1, p0}, Landroid/widget/Toast;->setGravity(III)V

    .line 12
    .line 13
    .line 14
    return-void
.end method

.method public static f()I
    .locals 3

    .line 1
    :try_start_0
    invoke-static {}, Ljava/util/Locale;->getDefault()Ljava/util/Locale;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-virtual {v0}, Ljava/util/Locale;->getCountry()Ljava/lang/String;

    .line 6
    .line 7
    .line 8
    move-result-object v0

    .line 9
    const-string v1, "_US"

    .line 10
    .line 11
    invoke-virtual {v0, v1}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I

    .line 12
    .line 13
    .line 14
    move-result v1

    .line 15
    const/16 v2, 0x23

    .line 16
    .line 17
    if-ltz v1, :cond_0

    .line 18
    .line 19
    return v2

    .line 20
    :cond_0
    const-string v1, "_CA"

    .line 21
    .line 22
    invoke-virtual {v0, v1}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I

    .line 23
    .line 24
    .line 25
    move-result v0
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 26
    if-ltz v0, :cond_1

    .line 27
    .line 28
    return v2

    .line 29
    :catch_0
    move-exception v0

    .line 30
    invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V

    .line 31
    .line 32
    .line 33
    :cond_1
    const/4 v0, 0x5

    .line 34
    return v0
.end method

.method public static g(Landroid/net/Uri;)V
    .locals 3

    .line 1
    sget-object v0, Llf2;->o:Llf2;

    .line 2
    .line 3
    const-string v1, "ImagePipelineFactory was not initialized!"

    .line 4
    .line 5
    invoke-static {v0, v1}, Lpr0;->i(Ljava/lang/Object;Ljava/lang/String;)V

    .line 6
    .line 7
    .line 8
    invoke-virtual {v0}, Llf2;->e()Lif2;

    .line 9
    .line 10
    .line 11
    move-result-object v0

    .line 12
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 13
    .line 14
    .line 15
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 16
    .line 17
    .line 18
    new-instance v1, Lwz;

    .line 19
    .line 20
    const/16 v2, 0x1b

    .line 21
    .line 22
    invoke-direct {v1, v2, p0}, Lwz;-><init>(ILjava/lang/Object;)V

    .line 23
    .line 24
    .line 25
    iget-object v2, v0, Lif2;->e:Luc3;

    .line 26
    .line 27
    invoke-interface {v2, v1}, Luc3;->e(Lwz;)I

    .line 28
    .line 29
    .line 30
    iget-object v2, v0, Lif2;->f:Luc3;

    .line 31
    .line 32
    invoke-interface {v2, v1}, Luc3;->e(Lwz;)I

    .line 33
    .line 34
    .line 35
    invoke-static {p0}, Lag2;->c(Landroid/net/Uri;)Lag2;

    .line 36
    .line 37
    .line 38
    move-result-object p0

    .line 39
    invoke-virtual {p0}, Lag2;->a()Lzf2;

    .line 40
    .line 41
    .line 42
    move-result-object p0

    .line 43
    iget-object v1, v0, Lif2;->g:Lb41;

    .line 44
    .line 45
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 46
    .line 47
    .line 48
    iget-object p0, p0, Lzf2;->b:Landroid/net/Uri;

    .line 49
    .line 50
    invoke-static {p0}, Lb41;->r(Landroid/net/Uri;)Lz05;

    .line 51
    .line 52
    .line 53
    move-result-object p0

    .line 54
    iget-object v0, v0, Lif2;->b:Ll95;

    .line 55
    .line 56
    invoke-interface {v0}, Ll95;->get()Ljava/lang/Object;

    .line 57
    .line 58
    .line 59
    move-result-object v0

    .line 60
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 61
    .line 62
    .line 63
    check-cast v0, Lma1;

    .line 64
    .line 65
    invoke-virtual {v0}, Lma1;->a()Le10;

    .line 66
    .line 67
    .line 68
    move-result-object v1

    .line 69
    invoke-virtual {v1, p0}, Le10;->b(Lz05;)V

    .line 70
    .line 71
    .line 72
    iget-object v1, v0, Lma1;->d:Lcv2;

    .line 73
    .line 74
    invoke-interface {v1}, Lcv2;->getValue()Ljava/lang/Object;

    .line 75
    .line 76
    .line 77
    move-result-object v1

    .line 78
    check-cast v1, Le10;

    .line 79
    .line 80
    invoke-virtual {v1, p0}, Le10;->b(Lz05;)V

    .line 81
    .line 82
    .line 83
    iget-object v0, v0, Lma1;->f:Lcv2;

    .line 84
    .line 85
    invoke-interface {v0}, Lcv2;->getValue()Ljava/lang/Object;

    .line 86
    .line 87
    .line 88
    move-result-object v0

    .line 89
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 90
    .line 91
    .line 92
    check-cast v0, Lyg2;

    .line 93
    .line 94
    invoke-virtual {v0}, Ljava/util/HashMap;->entrySet()Ljava/util/Set;

    .line 95
    .line 96
    .line 97
    move-result-object v0

    .line 98
    invoke-interface {v0}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    .line 99
    .line 100
    .line 101
    move-result-object v0

    .line 102
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    .line 103
    .line 104
    .line 105
    move-result v1

    .line 106
    if-eqz v1, :cond_0

    .line 107
    .line 108
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 109
    .line 110
    .line 111
    move-result-object v1

    .line 112
    check-cast v1, Ljava/util/Map$Entry;

    .line 113
    .line 114
    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    .line 115
    .line 116
    .line 117
    move-result-object v1

    .line 118
    check-cast v1, Le10;

    .line 119
    .line 120
    invoke-virtual {v1, p0}, Le10;->b(Lz05;)V

    .line 121
    .line 122
    .line 123
    goto :goto_0

    .line 124
    :cond_0
    return-void
.end method

.method public static h(Lmu;Landroid/view/View;Ljava/lang/String;Ljava/lang/String;)V
    .locals 2

    .line 1
    new-instance v0, Lcw5;

    .line 2
    .line 3
    invoke-direct {v0, p1, p2, p3}, Lcw5;-><init>(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;)V

    .line 4
    .line 5
    .line 6
    sget p1, Lzy3;->c:I

    .line 7
    .line 8
    iput p1, v0, Lcw5;->f:I

    .line 9
    .line 10
    const p1, 0x7f060054

    .line 11
    .line 12
    .line 13
    iput p1, v0, Lcw5;->g:I

    .line 14
    .line 15
    const/16 p2, 0x10

    .line 16
    .line 17
    iput p2, v0, Lcw5;->k:I

    .line 18
    .line 19
    const p2, 0x7f0605c4

    .line 20
    .line 21
    .line 22
    iput p2, v0, Lcw5;->i:I

    .line 23
    .line 24
    iput p2, v0, Lcw5;->j:I

    .line 25
    .line 26
    iput p1, v0, Lcw5;->h:I

    .line 27
    .line 28
    const/4 p1, 0x1

    .line 29
    iput-boolean p1, v0, Lcw5;->l:Z

    .line 30
    .line 31
    const/4 p1, 0x0

    .line 32
    iput-boolean p1, v0, Lcw5;->m:Z

    .line 33
    .line 34
    const/16 p1, 0x3c

    .line 35
    .line 36
    iput p1, v0, Lcw5;->c:I

    .line 37
    .line 38
    sget p1, Lfd5;->q0:I

    .line 39
    .line 40
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;

    .line 41
    .line 42
    .line 43
    move-result-object p1

    .line 44
    invoke-virtual {p1}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    .line 45
    .line 46
    .line 47
    move-result-object p1

    .line 48
    check-cast p1, Landroid/view/ViewGroup;

    .line 49
    .line 50
    new-instance p2, Landroid/view/ViewGroup$LayoutParams;

    .line 51
    .line 52
    const/4 p3, -0x1

    .line 53
    invoke-direct {p2, p3, p3}, Landroid/view/ViewGroup$LayoutParams;-><init>(II)V

    .line 54
    .line 55
    .line 56
    const p3, 0x1020002

    .line 57
    .line 58
    .line 59
    invoke-virtual {p1, p3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    .line 60
    .line 61
    .line 62
    move-result-object p3

    .line 63
    check-cast p3, Landroid/view/ViewGroup;

    .line 64
    .line 65
    new-instance v1, Lfd5;

    .line 66
    .line 67
    invoke-direct {v1, p0, p1, p3, v0}, Lfd5;-><init>(Lmu;Landroid/view/ViewGroup;Landroid/view/ViewGroup;Lcw5;)V

    .line 68
    .line 69
    .line 70
    invoke-virtual {p1, v1, p2}, Landroid/view/ViewGroup;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 71
    .line 72
    .line 73
    return-void
.end method

.method public static i(Landroid/app/Activity;Ljava/lang/String;)V
    .locals 2

    .line 1
    new-instance v0, Lo42;

    .line 2
    .line 3
    const/16 v1, 0xb

    .line 4
    .line 5
    invoke-direct {v0, v1, p0, p1}, Lo42;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V

    .line 6
    .line 7
    .line 8
    invoke-virtual {p0, v0}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 9
    .line 10
    .line 11
    return-void
.end method

.method public static j(Landroid/app/Activity;Ljava/lang/String;)V
    .locals 2

    .line 1
    new-instance v0, Lqu3;

    .line 2
    .line 3
    const/4 v1, 0x0

    .line 4
    invoke-direct {v0, p0, p1, v1}, Lqu3;-><init>(Landroid/app/Activity;Ljava/lang/String;I)V

    .line 5
    .line 6
    .line 7
    invoke-virtual {p0, v0}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 8
    .line 9
    .line 10
    return-void
.end method

.method public static k(Landroid/app/Activity;Ljava/lang/String;)V
    .locals 2

    .line 1
    new-instance v0, Lqu3;

    .line 2
    .line 3
    const/4 v1, 0x1

    .line 4
    invoke-direct {v0, p0, p1, v1}, Lqu3;-><init>(Landroid/app/Activity;Ljava/lang/String;I)V

    .line 5
    .line 6
    .line 7
    invoke-virtual {p0, v0}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 8
    .line 9
    .line 10
    return-void
.end method
