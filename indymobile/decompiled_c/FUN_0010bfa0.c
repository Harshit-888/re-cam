
void FUN_0010bfa0(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3)

{
  undefined8 *puVar1;
  long lVar2;
  long lVar3;
  bool bVar4;
  undefined8 *puVar5;
  ulong uVar6;
  code *pcVar7;
  undefined8 *puVar8;
  long lVar9;
  int iVar10;
  long lVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  undefined8 uVar14;
  undefined8 local_d0;
  undefined8 uStack_c8;
  undefined8 local_c0;
  long local_68;
  
  lVar2 = tpidr_el0;
  uVar6 = ((long)param_2 - (long)param_1 >> 3) * -0x5555555555555555;
  local_68 = *(long *)(lVar2 + 0x28);
  if ((long)uVar6 < 3) {
    if (1 < uVar6) {
      if (uVar6 == 2) {
        uVar6 = (*(code *)*param_3)(param_2 + -3,param_1);
        if ((uVar6 & 1) != 0) {
          uVar13 = *param_1;
          *param_1 = param_2[-3];
          param_2[-3] = uVar13;
          uVar13 = param_2[-2];
          uVar14 = param_1[2];
          uVar12 = param_1[1];
          param_1[2] = param_2[-1];
          param_1[1] = uVar13;
          param_2[-1] = uVar14;
          param_2[-2] = uVar12;
        }
      }
      else {
LAB_0010c088:
        FUN_0010bb30(param_1,param_1 + 3,param_1 + 6,param_3);
        if (param_1 + 9 != param_2) {
          lVar9 = 0;
          iVar10 = 0;
          puVar5 = param_1 + 6;
          puVar8 = param_1 + 9;
          do {
            uVar6 = (*(code *)*param_3)(puVar8,puVar5);
            if ((uVar6 & 1) != 0) {
              uStack_c8 = puVar8[1];
              local_d0 = *puVar8;
              local_c0 = puVar8[2];
              lVar3 = lVar9;
              do {
                lVar11 = lVar3;
                *(undefined8 *)((long)param_1 + lVar11 + 0x48) =
                     *(undefined8 *)((long)param_1 + lVar11 + 0x30);
                *(undefined8 *)((long)param_1 + lVar11 + 0x58) =
                     *(undefined8 *)((long)param_1 + lVar11 + 0x40);
                *(undefined8 *)((long)param_1 + lVar11 + 0x50) =
                     *(undefined8 *)((long)param_1 + lVar11 + 0x38);
                puVar5 = param_1;
                if (lVar11 == -0x30) goto LAB_0010c0c4;
                uVar6 = (*(code *)*param_3)(&local_d0,(long)param_1 + lVar11 + 0x18);
                lVar3 = lVar11 + -0x18;
              } while ((uVar6 & 1) != 0);
              puVar5 = (undefined8 *)((long)param_1 + lVar11 + 0x30);
LAB_0010c0c4:
              iVar10 = iVar10 + 1;
              *puVar5 = local_d0;
              *(undefined8 *)((long)param_1 + lVar11 + 0x40) = local_c0;
              *(undefined8 *)((long)param_1 + lVar11 + 0x38) = uStack_c8;
              if (iVar10 == 8) {
                bVar4 = puVar8 + 3 == param_2;
                goto LAB_0010c21c;
              }
            }
            puVar1 = puVar8 + 3;
            lVar9 = lVar9 + 0x18;
            puVar5 = puVar8;
            puVar8 = puVar1;
          } while (puVar1 != param_2);
        }
      }
    }
  }
  else if (uVar6 == 3) {
    FUN_0010bb30(param_1,param_1 + 3,param_2 + -3,param_3);
  }
  else if (uVar6 == 4) {
    FUN_0010bb30(param_1,param_1 + 3,param_1 + 6,param_3);
    uVar6 = (*(code *)*param_3)(param_2 + -3,param_1 + 6);
    if ((uVar6 & 1) != 0) {
      uVar13 = param_1[6];
      param_1[6] = param_2[-3];
      param_2[-3] = uVar13;
      uVar13 = param_2[-2];
      uVar14 = param_1[8];
      uVar12 = param_1[7];
      param_1[8] = param_2[-1];
      param_1[7] = uVar13;
      param_2[-1] = uVar14;
      param_2[-2] = uVar12;
      uVar6 = (*(code *)*param_3)(param_1 + 6,param_1 + 3);
      if ((uVar6 & 1) != 0) {
        uVar14 = param_1[5];
        uVar12 = param_1[4];
        param_1[5] = param_1[8];
        param_1[4] = param_1[7];
        uVar13 = param_1[3];
        param_1[8] = uVar14;
        param_1[7] = uVar12;
        pcVar7 = (code *)*param_3;
        param_1[3] = param_1[6];
        param_1[6] = uVar13;
        uVar6 = (*pcVar7)(param_1 + 3,param_1);
        if ((uVar6 & 1) != 0) {
          uVar14 = *param_1;
          bVar4 = true;
          *param_1 = param_1[3];
          uVar12 = param_1[2];
          uVar13 = param_1[1];
          param_1[3] = uVar14;
          param_1[2] = param_1[5];
          param_1[1] = param_1[4];
          param_1[5] = uVar12;
          param_1[4] = uVar13;
          goto LAB_0010c21c;
        }
      }
    }
  }
  else {
    if (uVar6 != 5) goto LAB_0010c088;
    FUN_0010c260(param_1,param_1 + 3,param_1 + 6,param_1 + 9,param_2 + -3,param_3);
  }
  bVar4 = true;
LAB_0010c21c:
  if (*(long *)(lVar2 + 0x28) != local_68) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(bVar4);
  }
  return;
}

