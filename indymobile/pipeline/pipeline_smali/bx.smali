.class public final Lbx;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"


# instance fields
.field public a:Ljava/util/List;

.field public b:Ljava/lang/Boolean;

.field public c:Lcv3;

.field public d:Lyu3;

.field public e:Lax;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    new-instance v0, Ljava/util/ArrayList;

    .line 5
    .line 6
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 7
    .line 8
    .line 9
    iput-object v0, p0, Lbx;->a:Ljava/util/List;

    .line 10
    .line 11
    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

    .line 12
    .line 13
    iput-object v0, p0, Lbx;->b:Ljava/lang/Boolean;

    .line 14
    .line 15
    sget-object v0, Lcv3;->kPSEnhanceTypeUndefined:Lcv3;

    .line 16
    .line 17
    iput-object v0, p0, Lbx;->c:Lcv3;

    .line 18
    .line 19
    sget-object v0, Lyu3;->kPSColorTypeColor:Lyu3;

    .line 20
    .line 21
    iput-object v0, p0, Lbx;->d:Lyu3;

    .line 22
    .line 23
    return-void
.end method

.method public static a(Ljava/util/List;Ljava/lang/Boolean;Lcv3;Lyu3;Lt62;)V
    .locals 8

    .line 1
    new-instance v0, Ljava/util/ArrayList;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 4
    .line 5
    .line 6
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    .line 7
    .line 8
    .line 9
    move-result-object p0

    .line 10
    const/4 v1, 0x0

    .line 11
    move-object v2, v1

    .line 12
    :cond_0
    :goto_0
    invoke-interface {p0}, Ljava/util/Iterator;->hasNext()Z

    .line 13
    .line 14
    .line 15
    move-result v3

    .line 16
    if-eqz v3, :cond_c

    .line 17
    .line 18
    invoke-interface {p0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 19
    .line 20
    .line 21
    move-result-object v3

    .line 22
    check-cast v3, Lcom/indymobile/app/model/PSPage;

    .line 23
    .line 24
    :try_start_0
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->n()Z

    .line 25
    .line 26
    .line 27
    move-result v4

    .line 28
    if-nez v4, :cond_1

    .line 29
    .line 30
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->d()Ljava/io/File;

    .line 31
    .line 32
    .line 33
    move-result-object v4

    .line 34
    invoke-virtual {v4}, Ljava/io/File;->exists()Z

    .line 35
    .line 36
    .line 37
    move-result v4

    .line 38
    if-nez v4, :cond_0

    .line 39
    .line 40
    invoke-static {}, Lvx3;->K()Lvx3;

    .line 41
    .line 42
    .line 43
    move-result-object v4

    .line 44
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 45
    .line 46
    .line 47
    invoke-static {v3}, Lvx3;->C(Lcom/indymobile/app/model/PSPage;)Z

    .line 48
    .line 49
    .line 50
    goto :goto_0

    .line 51
    :catch_0
    move-exception v3

    .line 52
    goto/16 :goto_4

    .line 53
    .line 54
    :cond_1
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 55
    .line 56
    .line 57
    move-result-object v4

    .line 58
    if-nez v4, :cond_4

    .line 59
    .line 60
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 61
    .line 62
    .line 63
    move-result-object v4

    .line 64
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 65
    .line 66
    .line 67
    sget-object v4, Ltu3;->MLCV:Ltu3;

    .line 68
    .line 69
    sget-object v5, Ltu3;->CV:Ltu3;

    .line 70
    .line 71
    if-ne v4, v5, :cond_2

    .line 72
    .line 73
    invoke-static {v1, v3}, Lqt3;->b(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V

    .line 74
    .line 75
    .line 76
    goto :goto_1

    .line 77
    :cond_2
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 78
    .line 79
    .line 80
    move-result-object v5

    .line 81
    invoke-virtual {v5}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 82
    .line 83
    .line 84
    sget-object v5, Ltu3;->ML:Ltu3;

    .line 85
    .line 86
    if-ne v4, v5, :cond_3

    .line 87
    .line 88
    invoke-static {v1, v3}, Lqt3;->d(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V

    .line 89
    .line 90
    .line 91
    goto :goto_1

    .line 92
    :cond_3
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 93
    .line 94
    .line 95
    move-result-object v4

    .line 96
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 97
    .line 98
    .line 99
    invoke-static {v1, v3}, Lqt3;->c(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)V

    .line 100
    .line 101
    .line 102
    :goto_1
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 103
    .line 104
    .line 105
    move-result-object v4

    .line 106
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    .line 107
    .line 108
    .line 109
    move-result v5

    .line 110
    xor-int/lit8 v5, v5, 0x1

    .line 111
    .line 112
    iput-boolean v5, v4, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 113
    .line 114
    :cond_4
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 115
    .line 116
    .line 117
    move-result-object v4

    .line 118
    iput-object p2, v4, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 119
    .line 120
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 121
    .line 122
    .line 123
    move-result-object v4

    .line 124
    iput-object p3, v4, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 125
    .line 126
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->t()V

    .line 127
    .line 128
    .line 129
    invoke-static {v1, v3}, Lt41;->F(Landroid/graphics/Bitmap;Lcom/indymobile/app/model/PSPage;)Landroid/graphics/Bitmap;

    .line 130
    .line 131
    .line 132
    move-result-object v4

    .line 133
    invoke-static {v4}, Lfa1;->H(Landroid/graphics/Bitmap;)Lcom/indymobile/app/model/PSImageEstimationInfoBean;

    .line 134
    .line 135
    .line 136
    move-result-object v5

    .line 137
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 138
    .line 139
    .line 140
    move-result-object v6

    .line 141
    iget-object v6, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 142
    .line 143
    sget-object v7, Lcv3;->kPSEnhanceTypeOriginal:Lcv3;

    .line 144
    .line 145
    if-ne v6, v7, :cond_5

    .line 146
    .line 147
    invoke-static {v4, p3}, Lqx3;->E(Landroid/graphics/Bitmap;Lyu3;)Lry;

    .line 148
    .line 149
    .line 150
    move-result-object v5

    .line 151
    goto :goto_3

    .line 152
    :cond_5
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 153
    .line 154
    .line 155
    move-result-object v6

    .line 156
    iget-object v6, v6, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 157
    .line 158
    sget-object v7, Lcv3;->kPSEnhanceTypeLighten:Lcv3;

    .line 159
    .line 160
    if-ne v6, v7, :cond_6

    .line 161
    .line 162
    iget v6, v5, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->whitePoint:F

    .line 163
    .line 164
    iget v5, v5, Lcom/indymobile/app/model/PSImageEstimationInfoBean;->blackPoint:F

    .line 165
    .line 166
    invoke-static {v4, v6, v5, p3}, Ltw3;->E(Landroid/graphics/Bitmap;FFLyu3;)Lry;

    .line 167
    .line 168
    .line 169
    move-result-object v5

    .line 170
    goto :goto_3

    .line 171
    :cond_6
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 172
    .line 173
    .line 174
    move-result-object v5

    .line 175
    iget-object v5, v5, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 176
    .line 177
    sget-object v6, Lcv3;->kPSEnhanceTypeDocumentLevel1:Lcv3;

    .line 178
    .line 179
    if-eq v5, v6, :cond_8

    .line 180
    .line 181
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->k()Lcom/indymobile/app/model/PSDocumentProcessInfo;

    .line 182
    .line 183
    .line 184
    move-result-object v5

    .line 185
    iget-object v5, v5, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 186
    .line 187
    sget-object v6, Lcv3;->kPSEnhanceTypeDocumentLevel2:Lcv3;

    .line 188
    .line 189
    if-ne v5, v6, :cond_7

    .line 190
    .line 191
    goto :goto_2

    .line 192
    :cond_7
    move-object v5, v1

    .line 193
    goto :goto_3

    .line 194
    :cond_8
    :goto_2
    invoke-static {v4, p2, p3}, Llu3;->E(Landroid/graphics/Bitmap;Lcv3;Lyu3;)Lry;

    .line 195
    .line 196
    .line 197
    move-result-object v5

    .line 198
    :goto_3
    iget-object v6, v5, Lry;->a:Landroid/graphics/Bitmap;

    .line 199
    .line 200
    if-eq v4, v6, :cond_9

    .line 201
    .line 202
    invoke-static {v4}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 203
    .line 204
    .line 205
    :cond_9
    invoke-virtual {v3}, Lcom/indymobile/app/model/PSPage;->j()Lcom/indymobile/app/model/PSDocumentFilterInfo;

    .line 206
    .line 207
    .line 208
    move-result-object v4

    .line 209
    iget v5, v5, Lry;->b:I

    .line 210
    .line 211
    iput v5, v4, Lcom/indymobile/app/model/PSDocumentFilterInfo;->bgColor:I

    .line 212
    .line 213
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 214
    .line 215
    .line 216
    move-result-object v4

    .line 217
    iget-boolean v4, v4, Ldy3;->U:Z

    .line 218
    .line 219
    invoke-static {}, Ldy3;->f()Ldy3;

    .line 220
    .line 221
    .line 222
    move-result-object v5

    .line 223
    invoke-virtual {v5}, Ldy3;->c()Ldv3;

    .line 224
    .line 225
    .line 226
    move-result-object v5

    .line 227
    invoke-static {v3, v6, v1, v4, v5}, Lyn0;->f(Lcom/indymobile/app/model/PSPage;Landroid/graphics/Bitmap;Landroid/graphics/Paint;ZLdv3;)Ljava/io/File;

    .line 228
    .line 229
    .line 230
    move-result-object v4

    .line 231
    if-eqz v4, :cond_a

    .line 232
    .line 233
    invoke-virtual {v0, v4}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 234
    .line 235
    .line 236
    :cond_a
    invoke-static {v6}, Ls46;->O(Landroid/graphics/Bitmap;)V

    .line 237
    .line 238
    .line 239
    iget-object v4, p4, Lt62;->b:Ljava/lang/Object;

    .line 240
    .line 241
    check-cast v4, Lio3;

    .line 242
    .line 243
    invoke-virtual {v4, v3}, Lio3;->k(Ljava/lang/Object;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    .line 244
    .line 245
    .line 246
    goto/16 :goto_0

    .line 247
    .line 248
    :goto_4
    if-eqz v2, :cond_0

    .line 249
    .line 250
    instance-of v2, v3, Lcom/indymobile/app/util/PSException;

    .line 251
    .line 252
    if-eqz v2, :cond_b

    .line 253
    .line 254
    check-cast v3, Lcom/indymobile/app/util/PSException;

    .line 255
    .line 256
    move-object v2, v3

    .line 257
    goto/16 :goto_0

    .line 258
    .line 259
    :cond_b
    new-instance v2, Lcom/indymobile/app/util/PSException;

    .line 260
    .line 261
    invoke-direct {v2, v3}, Lcom/indymobile/app/util/PSException;-><init>(Ljava/lang/Throwable;)V

    .line 262
    .line 263
    .line 264
    goto/16 :goto_0

    .line 265
    .line 266
    :cond_c
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    .line 267
    .line 268
    .line 269
    move-result p0

    .line 270
    if-lez p0, :cond_d

    .line 271
    .line 272
    sget-object p0, Lcom/indymobile/app/PSApplication;->d:Landroid/content/Context;

    .line 273
    .line 274
    invoke-static {p0, v0, v1}, Lxo1;->j(Landroid/content/Context;Ljava/util/ArrayList;Lk53;)V

    .line 275
    .line 276
    .line 277
    :cond_d
    if-nez v2, :cond_e

    .line 278
    .line 279
    return-void

    .line 280
    :cond_e
    throw v2
.end method
