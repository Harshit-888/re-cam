
bool FUN_00117478(undefined1 (*param_1) [16],undefined1 (*param_2) [16],undefined8 param_3)

{
  int iVar1;
  long lVar2;
  ulong uVar3;
  undefined1 (*pauVar4) [16];
  int iVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  long lVar8;
  int iVar9;
  undefined1 (*pauVar10) [16];
  undefined1 (*pauVar11) [16];
  long lVar12;
  undefined1 auVar13 [16];
  
  uVar3 = (long)param_2 - (long)param_1 >> 3;
  if ((long)uVar3 < 3) {
    if (uVar3 < 2) {
      return true;
    }
    if (uVar3 == 2) {
      if (*(int *)(*param_1 + 4) <= *(int *)(param_2[-1] + 0xc)) {
        return true;
      }
      uVar6 = *(undefined8 *)*param_1;
      *(undefined8 *)*param_1 = *(undefined8 *)(param_2[-1] + 8);
      *(undefined8 *)(param_2[-1] + 8) = uVar6;
      return true;
    }
  }
  else {
    if (uVar3 == 3) {
      iVar9 = *(int *)(*param_1 + 0xc);
      if (*(int *)(*param_1 + 4) <= iVar9) {
        if (iVar9 <= *(int *)(param_2[-1] + 0xc)) {
          return true;
        }
        uVar6 = *(undefined8 *)(*param_1 + 8);
        *(undefined8 *)(*param_1 + 8) = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar6;
        if (*(int *)(*param_1 + 4) <= *(int *)(*param_1 + 0xc)) {
          return true;
        }
        auVar13 = NEON_ext(*param_1,*param_1,8,1);
        *(long *)(*param_1 + 8) = auVar13._8_8_;
        *(long *)*param_1 = auVar13._0_8_;
        return true;
      }
      uVar6 = *(undefined8 *)*param_1;
      if (*(int *)(param_2[-1] + 0xc) < iVar9) {
        *(undefined8 *)*param_1 = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar6;
        return true;
      }
      *(undefined8 *)*param_1 = *(undefined8 *)(*param_1 + 8);
      *(undefined8 *)(*param_1 + 8) = uVar6;
      if ((int)((ulong)uVar6 >> 0x20) <= *(int *)(param_2[-1] + 0xc)) {
        return true;
      }
      *(undefined8 *)(*param_1 + 8) = *(undefined8 *)(param_2[-1] + 8);
      *(undefined8 *)(param_2[-1] + 8) = uVar6;
      return true;
    }
    if (uVar3 == 4) {
      FUN_00117730(param_1,*param_1 + 8,param_1 + 1,param_2[-1] + 8,param_3);
      return true;
    }
    if (uVar3 == 5) {
      FUN_00117730(param_1,*param_1 + 8,param_1 + 1,param_1[1] + 8,param_3);
      if (*(int *)(param_1[1] + 0xc) <= *(int *)(param_2[-1] + 0xc)) {
        return true;
      }
      uVar6 = *(undefined8 *)(param_1[1] + 8);
      *(undefined8 *)(param_1[1] + 8) = *(undefined8 *)(param_2[-1] + 8);
      *(undefined8 *)(param_2[-1] + 8) = uVar6;
      if (*(int *)(param_1[1] + 4) <= *(int *)(param_1[1] + 0xc)) {
        return true;
      }
      uVar6 = *(undefined8 *)param_1[1];
      uVar7 = *(undefined8 *)(param_1[1] + 8);
      iVar9 = *(int *)(*param_1 + 0xc);
      *(undefined8 *)param_1[1] = uVar7;
      *(undefined8 *)(param_1[1] + 8) = uVar6;
      iVar5 = (int)((ulong)uVar7 >> 0x20);
      if (iVar9 <= iVar5) {
        return true;
      }
      iVar9 = *(int *)(*param_1 + 4);
      uVar6 = *(undefined8 *)(*param_1 + 8);
      *(undefined8 *)(*param_1 + 8) = uVar7;
      *(undefined8 *)param_1[1] = uVar6;
      if (iVar9 <= iVar5) {
        return true;
      }
      uVar6 = *(undefined8 *)*param_1;
      *(undefined8 *)*param_1 = uVar7;
      *(undefined8 *)(*param_1 + 8) = uVar6;
      return true;
    }
  }
  iVar9 = *(int *)(*param_1 + 0xc);
  iVar5 = *(int *)(*param_1 + 4);
  iVar1 = *(int *)(param_1[1] + 4);
  if (iVar9 < iVar5) {
    uVar6 = *(undefined8 *)*param_1;
    if (iVar1 < iVar9) {
      uVar7 = *(undefined8 *)param_1[1];
      *(undefined8 *)param_1[1] = uVar6;
      *(undefined8 *)*param_1 = uVar7;
    }
    else {
      *(undefined8 *)*param_1 = *(undefined8 *)(*param_1 + 8);
      *(undefined8 *)(*param_1 + 8) = uVar6;
      if (iVar1 < (int)((ulong)uVar6 >> 0x20)) {
        *(undefined8 *)(*param_1 + 8) = *(undefined8 *)param_1[1];
        *(undefined8 *)param_1[1] = uVar6;
      }
    }
  }
  else if (iVar1 < iVar9) {
    uVar6 = *(undefined8 *)(*param_1 + 8);
    uVar7 = *(undefined8 *)param_1[1];
    *(undefined8 *)(*param_1 + 8) = uVar7;
    *(undefined8 *)param_1[1] = uVar6;
    if ((int)((ulong)uVar7 >> 0x20) < iVar5) {
      uVar6 = *(undefined8 *)*param_1;
      *(undefined8 *)*param_1 = uVar7;
      *(undefined8 *)(*param_1 + 8) = uVar6;
    }
  }
  if ((undefined1 (*) [16])(param_1[1] + 8) != param_2) {
    lVar8 = 0;
    iVar9 = 0;
    pauVar11 = (undefined1 (*) [16])(param_1[1] + 8);
    pauVar10 = param_1 + 1;
    do {
      pauVar4 = pauVar11;
      if (*(int *)(*pauVar4 + 4) < *(int *)(*pauVar10 + 4)) {
        uVar6 = *(undefined8 *)*pauVar4;
        lVar2 = lVar8;
        do {
          lVar12 = lVar2;
          *(undefined8 *)(param_1[1] + lVar12 + 8) = *(undefined8 *)(param_1[1] + lVar12);
          pauVar11 = param_1;
          if (lVar12 == -0x10) goto LAB_0011769c;
          lVar2 = lVar12 + -8;
        } while ((int)((ulong)uVar6 >> 0x20) < *(int *)(*param_1 + lVar12 + 0xc));
        pauVar11 = (undefined1 (*) [16])(param_1[1] + lVar12);
LAB_0011769c:
        iVar9 = iVar9 + 1;
        *(undefined8 *)*pauVar11 = uVar6;
        if (iVar9 == 8) {
          return (undefined1 (*) [16])(*pauVar4 + 8) == param_2;
        }
      }
      lVar8 = lVar8 + 8;
      pauVar11 = (undefined1 (*) [16])(*pauVar4 + 8);
      pauVar10 = pauVar4;
    } while ((undefined1 (*) [16])(*pauVar4 + 8) != param_2);
  }
  return true;
}

