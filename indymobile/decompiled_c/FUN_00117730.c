
void FUN_00117730(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3,undefined8 *param_4)

{
  int iVar1;
  int iVar2;
  undefined8 uVar3;
  int iVar4;
  
  iVar1 = *(int *)((long)param_2 + 4);
  iVar2 = *(int *)((long)param_3 + 4);
  if (iVar1 < *(int *)((long)param_1 + 4)) {
    uVar3 = *param_1;
    iVar4 = (int)((ulong)uVar3 >> 0x20);
    if (iVar2 < iVar1) {
      *param_1 = *param_3;
    }
    else {
      *param_1 = *param_2;
      *param_2 = uVar3;
      iVar2 = *(int *)((long)param_3 + 4);
      if (iVar4 <= iVar2) goto LAB_001177d4;
      *param_2 = *param_3;
    }
    *param_3 = uVar3;
    iVar2 = iVar4;
  }
  else if (iVar2 < iVar1) {
    uVar3 = *param_2;
    *param_2 = *param_3;
    *param_3 = uVar3;
    if (*(int *)((long)param_2 + 4) < *(int *)((long)param_1 + 4)) {
      uVar3 = *param_1;
      *param_1 = *param_2;
      *param_2 = uVar3;
      iVar2 = *(int *)((long)param_3 + 4);
    }
    else {
      iVar2 = (int)((ulong)uVar3 >> 0x20);
    }
  }
LAB_001177d4:
  if (*(int *)((long)param_4 + 4) < iVar2) {
    uVar3 = *param_3;
    *param_3 = *param_4;
    *param_4 = uVar3;
    if (*(int *)((long)param_3 + 4) < *(int *)((long)param_2 + 4)) {
      uVar3 = *param_2;
      *param_2 = *param_3;
      *param_3 = uVar3;
      if (*(int *)((long)param_2 + 4) < *(int *)((long)param_1 + 4)) {
        uVar3 = *param_1;
        *param_1 = *param_2;
        *param_2 = uVar3;
      }
    }
  }
  return;
}

