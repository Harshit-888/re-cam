
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */
/* getNewShape(int*, int*, int, int) */

void getNewShape(int *param_1,int *param_2,int param_3,int param_4)

{
  uint uVar1;
  undefined8 *puVar2;
  bool bVar3;
  uint uVar4;
  long lVar5;
  int iVar7;
  undefined8 uVar6;
  int iVar9;
  undefined8 uVar8;
  
  uVar4 = 1;
  do {
    param_3 = param_1[param_3];
    uVar1 = uVar4 + 1;
    if (param_4 <= param_3) break;
    bVar3 = uVar4 < 10;
    uVar4 = uVar1;
  } while (bVar3);
  lVar5 = 0;
  uVar6 = _DAT_00105040;
  uVar8 = _UNK_00105048;
  do {
    iVar7 = (int)((ulong)uVar6 >> 0x20);
    iVar9 = (int)((ulong)uVar8 >> 0x20);
    puVar2 = (undefined8 *)((long)param_2 + lVar5);
    lVar5 = lVar5 + 0x20;
    puVar2[1] = uVar8;
    *puVar2 = uVar6;
    puVar2[3] = CONCAT44(iVar9 + 4,(int)uVar8 + 4);
    puVar2[2] = CONCAT44(iVar7 + 4,(int)uVar6 + 4);
    uVar6 = CONCAT44(iVar7 + 8,(int)uVar6 + 8);
    uVar8 = CONCAT44(iVar9 + 8,(int)uVar8 + 8);
  } while (lVar5 != 0x400);
  uVar4 = 1;
  do {
    lVar5 = 0;
    do {
      *(int *)((long)param_2 + lVar5) = param_1[*(int *)((long)param_2 + lVar5)];
      lVar5 = lVar5 + 4;
    } while (lVar5 != 0x400);
    uVar4 = uVar4 + 1;
  } while (uVar1 != uVar4);
  return;
}

