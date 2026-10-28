.class public Lcom/indymobile/app/model/PSDocumentProcessInfo;
.super Ljava/lang/Object;
.source "r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8"

# interfaces
.implements Landroid/os/Parcelable;


# static fields
.field public static final CREATOR:Landroid/os/Parcelable$Creator;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Landroid/os/Parcelable$Creator<",
            "Lcom/indymobile/app/model/PSDocumentProcessInfo;",
            ">;"
        }
    .end annotation
.end field


# instance fields
.field public autoDetectRect:Lcom/indymobile/app/model/PSRect;

.field public bottomLines:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/indymobile/app/model/PSLine;",
            ">;"
        }
    .end annotation
.end field

.field public colorType:Lyu3;

.field public enhanceType:Lcv3;

.field public fixToFitImage:Z

.field public height:F

.field public leftLines:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/indymobile/app/model/PSLine;",
            ">;"
        }
    .end annotation
.end field

.field public rightLines:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/indymobile/app/model/PSLine;",
            ">;"
        }
    .end annotation
.end field

.field public topLines:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/indymobile/app/model/PSLine;",
            ">;"
        }
    .end annotation
.end field

.field public userRotateDegree:F

.field public userSelectRect:Lcom/indymobile/app/model/PSRect;

.field public width:F


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 1
    new-instance v0, Lcom/indymobile/app/model/PSDocumentProcessInfo$1;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    .line 4
    .line 5
    .line 6
    sput-object v0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->CREATOR:Landroid/os/Parcelable$Creator;

    .line 7
    .line 8
    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    sget-object v0, Lcv3;->kPSEnhanceTypeUndefined:Lcv3;

    .line 5
    .line 6
    iput-object v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 7
    .line 8
    return-void
.end method


# virtual methods
.method public final describeContents()I
    .locals 0

    .line 1
    const/4 p0, 0x0

    .line 2
    return p0
.end method

.method public final writeToParcel(Landroid/os/Parcel;I)V
    .locals 1

    .line 1
    iget v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->width:F

    .line 2
    .line 3
    invoke-virtual {p1, v0}, Landroid/os/Parcel;->writeFloat(F)V

    .line 4
    .line 5
    .line 6
    iget v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->height:F

    .line 7
    .line 8
    invoke-virtual {p1, v0}, Landroid/os/Parcel;->writeFloat(F)V

    .line 9
    .line 10
    .line 11
    iget-boolean v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->fixToFitImage:Z

    .line 12
    .line 13
    invoke-virtual {p1, v0}, Landroid/os/Parcel;->writeByte(B)V

    .line 14
    .line 15
    .line 16
    iget-object v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->autoDetectRect:Lcom/indymobile/app/model/PSRect;

    .line 17
    .line 18
    invoke-virtual {p1, v0, p2}, Landroid/os/Parcel;->writeParcelable(Landroid/os/Parcelable;I)V

    .line 19
    .line 20
    .line 21
    iget-object v0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userSelectRect:Lcom/indymobile/app/model/PSRect;

    .line 22
    .line 23
    invoke-virtual {p1, v0, p2}, Landroid/os/Parcel;->writeParcelable(Landroid/os/Parcelable;I)V

    .line 24
    .line 25
    .line 26
    iget-object p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->leftLines:Ljava/util/List;

    .line 27
    .line 28
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeTypedList(Ljava/util/List;)V

    .line 29
    .line 30
    .line 31
    iget-object p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->topLines:Ljava/util/List;

    .line 32
    .line 33
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeTypedList(Ljava/util/List;)V

    .line 34
    .line 35
    .line 36
    iget-object p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->bottomLines:Ljava/util/List;

    .line 37
    .line 38
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeTypedList(Ljava/util/List;)V

    .line 39
    .line 40
    .line 41
    iget-object p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->rightLines:Ljava/util/List;

    .line 42
    .line 43
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeTypedList(Ljava/util/List;)V

    .line 44
    .line 45
    .line 46
    iget p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->userRotateDegree:F

    .line 47
    .line 48
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeFloat(F)V

    .line 49
    .line 50
    .line 51
    iget-object p2, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->enhanceType:Lcv3;

    .line 52
    .line 53
    const/4 v0, -0x1

    .line 54
    if-nez p2, :cond_0

    .line 55
    .line 56
    move p2, v0

    .line 57
    goto :goto_0

    .line 58
    :cond_0
    invoke-virtual {p2}, Ljava/lang/Enum;->ordinal()I

    .line 59
    .line 60
    .line 61
    move-result p2

    .line 62
    :goto_0
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeInt(I)V

    .line 63
    .line 64
    .line 65
    iget-object p0, p0, Lcom/indymobile/app/model/PSDocumentProcessInfo;->colorType:Lyu3;

    .line 66
    .line 67
    if-nez p0, :cond_1

    .line 68
    .line 69
    goto :goto_1

    .line 70
    :cond_1
    invoke-virtual {p0}, Ljava/lang/Enum;->ordinal()I

    .line 71
    .line 72
    .line 73
    move-result v0

    .line 74
    :goto_1
    invoke-virtual {p1, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 75
    .line 76
    .line 77
    return-void
.end method
