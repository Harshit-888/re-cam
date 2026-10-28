.class public final Lcom/indymobile/app/activity/editor/markup/a;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"

# interfaces
.implements Lxy3;


# instance fields
.field public final synthetic a:Landroid/content/Intent;

.field public final synthetic b:Lcom/indymobile/app/activity/editor/markup/MarkupActivity;


# direct methods
.method public constructor <init>(Lcom/indymobile/app/activity/editor/markup/MarkupActivity;Landroid/content/Intent;)V
    .locals 0

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    iput-object p1, p0, Lcom/indymobile/app/activity/editor/markup/a;->b:Lcom/indymobile/app/activity/editor/markup/MarkupActivity;

    .line 5
    .line 6
    iput-object p2, p0, Lcom/indymobile/app/activity/editor/markup/a;->a:Landroid/content/Intent;

    .line 7
    .line 8
    return-void
.end method


# virtual methods
.method public final t(Lio3;)Ljava/lang/Object;
    .locals 10

    .line 1
    const/4 p1, 0x0

    .line 2
    iget-object v0, p0, Lcom/indymobile/app/activity/editor/markup/a;->a:Landroid/content/Intent;

    .line 3
    .line 4
    if-eqz v0, :cond_0

    .line 5
    .line 6
    const-string v1, "CROP_IMAGE_EXTRA_RESULT"

    .line 7
    .line 8
    invoke-virtual {v0, v1}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;

    .line 9
    .line 10
    .line 11
    move-result-object v0

    .line 12
    check-cast v0, Lcom/theartofdev/edmodo/cropper/CropImage$ActivityResult;

    .line 13
    .line 14
    goto :goto_0

    .line 15
    :cond_0
    move-object v0, p1

    .line 16
    :goto_0
    iget-object v0, v0, Lcom/theartofdev/edmodo/cropper/CropImage$ActivityResult;->b:Landroid/net/Uri;

    .line 17
    .line 18
    if-eqz v0, :cond_1

    .line 19
    .line 20
    invoke-virtual {v0}, Landroid/net/Uri;->getPath()Ljava/lang/String;

    .line 21
    .line 22
    .line 23
    move-result-object v0

    .line 24
    goto :goto_1

    .line 25
    :cond_1
    move-object v0, p1

    .line 26
    :goto_1
    if-eqz v0, :cond_5

    .line 27
    .line 28
    iget-object p0, p0, Lcom/indymobile/app/activity/editor/markup/a;->b:Lcom/indymobile/app/activity/editor/markup/MarkupActivity;

    .line 29
    .line 30
    iget-boolean v1, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->Y:Z

    .line 31
    .line 32
    const/4 v2, 0x1

    .line 33
    if-eqz v1, :cond_3

    .line 34
    .line 35
    const/16 v1, 0x4b0

    .line 36
    .line 37
    invoke-static {v1, v0}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 38
    .line 39
    .line 40
    move-result-object v1

    .line 41
    new-instance v3, Lorg/opencv/core/Mat;

    .line 42
    .line 43
    invoke-direct {v3}, Lorg/opencv/core/Mat;-><init>()V

    .line 44
    .line 45
    .line 46
    new-instance v4, Lorg/opencv/core/Mat;

    .line 47
    .line 48
    invoke-direct {v4}, Lorg/opencv/core/Mat;-><init>()V

    .line 49
    .line 50
    .line 51
    invoke-static {v1, v3}, Ls46;->e(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 52
    .line 53
    .line 54
    sget-object v5, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 55
    .line 56
    iget-wide v6, v3, Lorg/opencv/core/Mat;->a:J

    .line 57
    .line 58
    iget-wide v8, v4, Lorg/opencv/core/Mat;->a:J

    .line 59
    .line 60
    invoke-static {v5, v6, v7, v8, v9}, Lcom/indymobile/app/PSOpenCV;->enhanceSignature(Landroid/content/Context;JJ)V

    .line 61
    .line 62
    .line 63
    invoke-virtual {v4}, Lorg/opencv/core/Mat;->a()I

    .line 64
    .line 65
    .line 66
    move-result v3

    .line 67
    invoke-virtual {v4}, Lorg/opencv/core/Mat;->e()I

    .line 68
    .line 69
    .line 70
    move-result v5

    .line 71
    sget-object v6, Landroid/graphics/Bitmap$Config;->ARGB_8888:Landroid/graphics/Bitmap$Config;

    .line 72
    .line 73
    invoke-static {v3, v5, v6}, Landroid/graphics/Bitmap;->createBitmap(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;

    .line 74
    .line 75
    .line 76
    move-result-object v3

    .line 77
    invoke-static {v3, v4}, Lorg/opencv/android/Utils;->b(Landroid/graphics/Bitmap;Lorg/opencv/core/Mat;)V

    .line 78
    .line 79
    .line 80
    invoke-virtual {v1}, Landroid/graphics/Bitmap;->recycle()V

    .line 81
    .line 82
    .line 83
    iget-object v1, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->S:Lgy3;

    .line 84
    .line 85
    invoke-virtual {v1, v3, v2}, Lgy3;->a(Landroid/graphics/Bitmap;I)Lcom/indymobile/app/model/editor/PSStickerImage;

    .line 86
    .line 87
    .line 88
    move-result-object v1

    .line 89
    iget-object v2, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->S:Lgy3;

    .line 90
    .line 91
    iget-object v3, v2, Lgy3;->g:Ljava/lang/Object;

    .line 92
    .line 93
    check-cast v3, Ljava/io/File;

    .line 94
    .line 95
    invoke-virtual {v2, v3}, Lgy3;->t(Ljava/io/File;)V

    .line 96
    .line 97
    .line 98
    iget-object v2, v2, Lgy3;->f:Ljava/lang/Object;

    .line 99
    .line 100
    check-cast v2, Ljava/io/File;

    .line 101
    .line 102
    :try_start_0
    invoke-virtual {v2}, Ljava/io/File;->exists()Z

    .line 103
    .line 104
    .line 105
    move-result v3

    .line 106
    if-eqz v3, :cond_2

    .line 107
    .line 108
    invoke-virtual {v2}, Ljava/io/File;->delete()Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 109
    .line 110
    .line 111
    goto :goto_2

    .line 112
    :catchall_0
    move-exception v2

    .line 113
    invoke-virtual {v2}, Ljava/lang/Throwable;->printStackTrace()V

    .line 114
    .line 115
    .line 116
    :cond_2
    :goto_2
    iget-object v2, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->W:Lcom/indymobile/app/activity/editor/markup/MarkupView;

    .line 117
    .line 118
    invoke-virtual {v2, v1}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->h(Lcom/indymobile/app/model/editor/PSStickerImage;)V

    .line 119
    .line 120
    .line 121
    iget-object v2, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->S:Lgy3;

    .line 122
    .line 123
    iget-object v2, v2, Lgy3;->c:Ljava/util/ArrayList;

    .line 124
    .line 125
    invoke-virtual {v2, v1}, Ljava/util/ArrayList;->indexOf(Ljava/lang/Object;)I

    .line 126
    .line 127
    .line 128
    move-result v1

    .line 129
    if-ltz v1, :cond_4

    .line 130
    .line 131
    new-instance v2, Lt63;

    .line 132
    .line 133
    const/4 v3, 0x0

    .line 134
    invoke-direct {v2, p0, v1, v3}, Lt63;-><init>(Lcom/indymobile/app/activity/editor/markup/MarkupActivity;II)V

    .line 135
    .line 136
    .line 137
    invoke-virtual {p0, v2}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 138
    .line 139
    .line 140
    goto :goto_4

    .line 141
    :cond_3
    const/16 v1, 0x3e8

    .line 142
    .line 143
    invoke-static {v1, v0}, Ls46;->l(ILjava/lang/String;)Landroid/graphics/Bitmap;

    .line 144
    .line 145
    .line 146
    move-result-object v1

    .line 147
    iget-object p0, p0, Lcom/indymobile/app/activity/editor/markup/MarkupActivity;->W:Lcom/indymobile/app/activity/editor/markup/MarkupView;

    .line 148
    .line 149
    iget-object v3, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->c:Lgx3;

    .line 150
    .line 151
    iget-object v4, v3, Lgx3;->e:Lgy3;

    .line 152
    .line 153
    const/4 v5, 0x3

    .line 154
    invoke-virtual {v4, v1, v5}, Lgy3;->a(Landroid/graphics/Bitmap;I)Lcom/indymobile/app/model/editor/PSStickerImage;

    .line 155
    .line 156
    .line 157
    move-result-object v1

    .line 158
    :try_start_1
    iget-object v5, v4, Lgy3;->f:Ljava/lang/Object;

    .line 159
    .line 160
    check-cast v5, Ljava/io/File;

    .line 161
    .line 162
    invoke-virtual {v4, v5}, Lgy3;->t(Ljava/io/File;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 163
    .line 164
    .line 165
    goto :goto_3

    .line 166
    :catchall_1
    move-exception v4

    .line 167
    invoke-virtual {v4}, Ljava/lang/Throwable;->printStackTrace()V

    .line 168
    .line 169
    .line 170
    :goto_3
    new-instance v4, Lcom/indymobile/app/model/editor/PSSticker;

    .line 171
    .line 172
    invoke-direct {v4, v1}, Lcom/indymobile/app/model/editor/PSSticker;-><init>(Lcom/indymobile/app/model/editor/PSStickerImage;)V

    .line 173
    .line 174
    .line 175
    iget-object v1, v3, Lgx3;->m:Lcom/indymobile/app/model/editor/PSMarkup;

    .line 176
    .line 177
    invoke-virtual {v1, v4}, Lcom/indymobile/app/model/editor/PSMarkup;->i(Lcom/indymobile/app/model/editor/PSSticker;)V

    .line 178
    .line 179
    .line 180
    iget-object v1, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->c:Lgx3;

    .line 181
    .line 182
    iget-object v1, v1, Lgx3;->e:Lgy3;

    .line 183
    .line 184
    invoke-static {v4, v1}, Lxk1;->a(Lcom/indymobile/app/model/editor/PSSticker;Lgy3;)Lxe2;

    .line 185
    .line 186
    .line 187
    move-result-object v1

    .line 188
    iget-object v3, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->b:Lcom/indymobile/app/activity/editor/markup/MarkupView$a;

    .line 189
    .line 190
    invoke-static {v3}, Lcom/indymobile/app/activity/editor/markup/MarkupView$a;->a(Lcom/indymobile/app/activity/editor/markup/MarkupView$a;)I

    .line 191
    .line 192
    .line 193
    move-result v3

    .line 194
    invoke-virtual {v1, v3}, Lgy;->i(I)V

    .line 195
    .line 196
    .line 197
    invoke-virtual {p0, v1}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->o(Lxe2;)V

    .line 198
    .line 199
    .line 200
    invoke-virtual {p0, v1}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->A(Lxk1;)V

    .line 201
    .line 202
    .line 203
    iget-object v3, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->c:Lgx3;

    .line 204
    .line 205
    invoke-virtual {v3}, Lgx3;->a()V

    .line 206
    .line 207
    .line 208
    iget-object v3, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->k:Ljava/util/Vector;

    .line 209
    .line 210
    invoke-virtual {v3, v1}, Ljava/util/Vector;->add(Ljava/lang/Object;)Z

    .line 211
    .line 212
    .line 213
    invoke-virtual {p0, v1}, Lcom/indymobile/app/activity/editor/markup/MarkupView;->E(Lxk1;)V

    .line 214
    .line 215
    .line 216
    iput-boolean v2, p0, Lcom/indymobile/app/activity/editor/markup/MarkupView;->e:Z

    .line 217
    .line 218
    :cond_4
    :goto_4
    new-instance p0, Ljava/io/File;

    .line 219
    .line 220
    invoke-direct {p0, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 221
    .line 222
    .line 223
    invoke-virtual {p0}, Ljava/io/File;->delete()Z

    .line 224
    .line 225
    .line 226
    return-object p1

    .line 227
    :cond_5
    new-instance p0, Lcom/indymobile/app/util/PSException;

    .line 228
    .line 229
    const-string p1, "Crop image is failed."

    .line 230
    .line 231
    invoke-direct {p0, p1}, Lcom/indymobile/app/util/PSException;-><init>(Ljava/lang/String;)V

    .line 232
    .line 233
    .line 234
    throw p0
.end method
