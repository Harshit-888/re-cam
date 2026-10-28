
void FUN_00119b9c(int *param_1,int *param_2,undefined8 param_3)

{
  int iVar1;
  long lVar2;
  int *piVar3;
  int *piVar4;
  bool bVar5;
  void *pvVar6;
  ulong uVar7;
  int *piVar8;
  undefined8 uVar9;
  int *piVar10;
  undefined8 uVar11;
  int *piVar12;
  int iVar13;
  int *piVar14;
  undefined8 uVar15;
  undefined8 uVar16;
  undefined8 uVar17;
  undefined8 uVar18;
  undefined8 uVar19;
  undefined8 uVar20;
  undefined8 uVar21;
  undefined8 uVar22;
  int *local_88;
  int *piStack_80;
  long local_78;
  
  lVar2 = tpidr_el0;
  uVar7 = (long)param_2 - (long)param_1 >> 6;
  local_78 = *(long *)(lVar2 + 0x28);
  local_88 = param_2;
  piStack_80 = param_1;
  if ((long)uVar7 < 3) {
    if (1 < uVar7) {
      if (uVar7 != 2) goto LAB_00119c70;
      local_88 = param_2 + -0x10;
      if (param_1[0xe] < param_2[-2]) {
        FUN_001190dc(&piStack_80,&local_88);
        bVar5 = true;
        goto LAB_00119df0;
      }
    }
  }
  else {
    if (uVar7 == 3) {
      FUN_0011953c(param_1,param_1 + 0x10,param_2 + -0x10,param_3);
      bVar5 = true;
      goto LAB_00119df0;
    }
    if (uVar7 == 4) {
      FUN_00119e3c(param_1,param_1 + 0x10,param_1 + 0x20,param_2 + -0x10,param_3);
      bVar5 = true;
      goto LAB_00119df0;
    }
    if (uVar7 == 5) {
      FUN_00119f0c(param_1,param_1 + 0x10,param_1 + 0x20,param_1 + 0x30,param_2 + -0x10);
      bVar5 = true;
      goto LAB_00119df0;
    }
LAB_00119c70:
    FUN_0011953c(param_1,param_1 + 0x10,param_1 + 0x20,param_3);
    piVar4 = piStack_80;
    piVar3 = local_88;
    if (param_1 + 0x30 != param_2) {
      iVar13 = 0;
      piVar14 = param_1 + 0x1e;
      piVar10 = param_1 + 0x20;
      piVar12 = param_1 + 0x30;
      do {
        iVar1 = piVar12[0xe];
        if (piVar10[0xe] < iVar1) {
          uVar21 = *(undefined8 *)(piVar12 + 2);
          uVar20 = *(undefined8 *)piVar12;
          uVar15 = *(undefined8 *)(piVar12 + 4);
          uVar17 = *(undefined8 *)(piVar12 + 8);
          uVar16 = *(undefined8 *)(piVar12 + 6);
          uVar11 = *(undefined8 *)(piVar12 + 10);
          uVar22 = *(undefined8 *)(piVar12 + 0xc);
          piVar12[0] = 0;
          piVar12[1] = 0;
          piVar12[2] = 0;
          piVar12[3] = 0;
          piVar12[4] = 0;
          piVar12[5] = 0;
          piVar12[6] = 0;
          piVar12[7] = 0;
          piVar12[8] = 0;
          piVar12[9] = 0;
          piVar12[10] = 0;
          piVar12[0xb] = 0;
          piVar10 = piVar14;
          do {
            piVar8 = piVar10;
            pvVar6 = *(void **)(piVar8 + 0x12);
            if (pvVar6 != (void *)0x0) {
              *(void **)(piVar8 + 0x14) = pvVar6;
              operator_delete(pvVar6);
              piVar8[0x14] = 0;
              piVar8[0x15] = 0;
              piVar8[0x16] = 0;
              piVar8[0x17] = 0;
            }
            uVar18 = *(undefined8 *)(piVar8 + 4);
            uVar9 = *(undefined8 *)(piVar8 + 6);
            piVar10 = piVar8 + 2;
            pvVar6 = *(void **)(piVar8 + 0x18);
            piVar8[4] = 0;
            piVar8[5] = 0;
            piVar8[6] = 0;
            piVar8[7] = 0;
            *(undefined8 *)(piVar8 + 0x14) = uVar18;
            *(undefined8 *)(piVar8 + 0x12) = *(undefined8 *)(piVar8 + 2);
            *(undefined8 *)(piVar8 + 0x16) = uVar9;
            piVar10[0] = 0;
            piVar10[1] = 0;
            if (pvVar6 != (void *)0x0) {
              *(void **)(piVar8 + 0x1a) = pvVar6;
              operator_delete(pvVar6);
              piVar8[0x1a] = 0;
              piVar8[0x1b] = 0;
              piVar8[0x1c] = 0;
              piVar8[0x1d] = 0;
            }
            uVar9 = *(undefined8 *)(piVar8 + 0xc);
            uVar19 = *(undefined8 *)(piVar8 + 10);
            uVar18 = *(undefined8 *)(piVar8 + 8);
            piVar8[10] = 0;
            piVar8[0xb] = 0;
            piVar8[0xc] = 0;
            piVar8[0xd] = 0;
            piVar8[8] = 0;
            piVar8[9] = 0;
            *(undefined8 *)(piVar8 + 0x1a) = uVar19;
            *(undefined8 *)(piVar8 + 0x18) = uVar18;
            *(undefined8 *)(piVar8 + 0x1c) = uVar9;
            *(undefined8 *)(piVar8 + 0x1e) = *(undefined8 *)(piVar8 + 0xe);
            piVar8[0x20] = piVar8[0x10];
          } while ((piVar10 != piVar4) && (piVar10 = piVar8 + -0x10, *piVar8 < iVar1));
          pvVar6 = *(void **)(piVar8 + 2);
          if (pvVar6 != (void *)0x0) {
            *(void **)(piVar8 + 4) = pvVar6;
            operator_delete(pvVar6);
          }
          pvVar6 = *(void **)(piVar8 + 8);
          *(undefined8 *)(piVar8 + 6) = uVar15;
          *(undefined8 *)(piVar8 + 4) = uVar21;
          *(undefined8 *)(piVar8 + 2) = uVar20;
          if (pvVar6 != (void *)0x0) {
            *(void **)(piVar8 + 10) = pvVar6;
            operator_delete(pvVar6);
          }
          iVar13 = iVar13 + 1;
          *(undefined8 *)(piVar8 + 0xc) = uVar11;
          *(undefined8 *)(piVar8 + 10) = uVar17;
          *(undefined8 *)(piVar8 + 8) = uVar16;
          *(undefined8 *)(piVar8 + 0xe) = uVar22;
          piVar8[0x10] = iVar1;
          param_2 = piVar3;
          if (iVar13 == 8) {
            bVar5 = piVar12 + 0x10 == piVar3;
            goto LAB_00119df0;
          }
        }
        piVar8 = piVar12 + 0x10;
        piVar14 = piVar14 + 0x10;
        piVar10 = piVar12;
        piVar12 = piVar8;
      } while (piVar8 != param_2);
    }
  }
  bVar5 = true;
LAB_00119df0:
  if (*(long *)(lVar2 + 0x28) == local_78) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(bVar5);
}

