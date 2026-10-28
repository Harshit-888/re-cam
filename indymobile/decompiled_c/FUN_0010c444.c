
undefined8 *
FUN_0010c444(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3,undefined8 *param_4)

{
  long lVar1;
  bool bVar2;
  ulong uVar3;
  ulong uVar4;
  ulong uVar5;
  ulong uVar6;
  undefined8 *puVar7;
  undefined8 *puVar8;
  ulong uVar9;
  ulong uVar10;
  undefined8 *puVar11;
  undefined8 *puVar12;
  ulong uVar13;
  undefined8 *puVar14;
  ulong uVar15;
  undefined8 uVar16;
  undefined8 uVar17;
  undefined8 uVar18;
  undefined8 uVar19;
  undefined8 local_90;
  undefined8 uStack_88;
  undefined8 local_80;
  long local_78;
  
  lVar1 = tpidr_el0;
  local_78 = *(long *)(lVar1 + 0x28);
  puVar7 = param_3;
  if (param_1 != param_2) {
    uVar15 = (long)param_2 - (long)param_1;
    uVar10 = ((long)uVar15 >> 3) * -0x5555555555555555;
    if (0x18 < (long)uVar15) {
      uVar13 = uVar10 - 2 >> 1;
      uVar5 = uVar13;
      do {
        if ((0x2f < uVar15) && ((long)uVar5 <= (long)uVar13)) {
          uVar6 = uVar5 << 1 | 1;
          uVar4 = uVar5 * 2 + 2;
          puVar11 = param_1 + uVar6 * 3;
          uVar9 = uVar6;
          puVar7 = puVar11;
          if ((long)uVar4 < (long)uVar10) {
            uVar3 = (*(code *)*param_4)(puVar11,puVar11 + 3);
            uVar9 = uVar4;
            puVar7 = puVar11 + 3;
            if ((uVar3 & 1) == 0) {
              uVar9 = uVar6;
              puVar7 = puVar11;
            }
          }
          puVar11 = param_1 + uVar5 * 3;
          uVar4 = (*(code *)*param_4)(puVar7,puVar11);
          if ((uVar4 & 1) == 0) {
            uStack_88 = puVar11[1];
            local_90 = *puVar11;
            local_80 = puVar11[2];
            do {
              puVar12 = puVar7;
              *puVar11 = *puVar12;
              uVar18 = puVar12[1];
              puVar11[2] = puVar12[2];
              puVar11[1] = uVar18;
              if ((long)uVar13 < (long)uVar9) break;
              uVar6 = uVar9 << 1 | 1;
              uVar4 = uVar9 * 2 + 2;
              puVar11 = param_1 + uVar6 * 3;
              puVar7 = puVar11;
              uVar9 = uVar6;
              if ((long)uVar4 < (long)uVar10) {
                uVar3 = (*(code *)*param_4)(puVar11,puVar11 + 3);
                puVar7 = puVar11 + 3;
                uVar9 = uVar4;
                if ((uVar3 & 1) == 0) {
                  puVar7 = puVar11;
                  uVar9 = uVar6;
                }
              }
              uVar4 = (*(code *)*param_4)(puVar7,&local_90);
              puVar11 = puVar12;
            } while ((uVar4 & 1) == 0);
            *puVar12 = local_90;
            puVar12[2] = local_80;
            puVar12[1] = uStack_88;
          }
        }
        bVar2 = 0 < (long)uVar5;
        uVar5 = uVar5 - 1;
      } while (bVar2);
    }
    puVar7 = param_2;
    if (param_2 != param_3) {
      puVar11 = param_1 + 3;
      puVar12 = param_2;
      do {
        uVar5 = (*(code *)*param_4)(puVar12,param_1);
        if ((uVar5 & 1) != 0) {
          uVar18 = *puVar12;
          *puVar12 = *param_1;
          *param_1 = uVar18;
          uVar18 = param_1[1];
          uVar19 = puVar12[2];
          uVar17 = puVar12[1];
          puVar12[2] = param_1[2];
          puVar12[1] = uVar18;
          param_1[2] = uVar19;
          param_1[1] = uVar17;
          if (0x2f < (long)uVar15) {
            if (uVar10 < 3) {
              uVar5 = 1;
              puVar7 = puVar11;
            }
            else {
              uVar5 = (*(code *)*param_4)(puVar11,param_1 + 6);
              bVar2 = (uVar5 & 1) == 0;
              puVar7 = param_1 + 6;
              if (bVar2) {
                puVar7 = puVar11;
              }
              uVar5 = 1;
              if (!bVar2) {
                uVar5 = 2;
              }
            }
            uVar13 = (*(code *)*param_4)(puVar7,param_1);
            if ((uVar13 & 1) == 0) {
              local_80 = param_1[2];
              uStack_88 = param_1[1];
              local_90 = *param_1;
              puVar8 = param_1;
              do {
                puVar14 = puVar7;
                *puVar8 = *puVar14;
                uVar18 = puVar14[1];
                puVar8[2] = puVar14[2];
                puVar8[1] = uVar18;
                if ((long)(uVar10 - 2 >> 1) < (long)uVar5) break;
                uVar4 = uVar5 << 1 | 1;
                uVar13 = uVar5 * 2 + 2;
                puVar8 = param_1 + uVar4 * 3;
                uVar5 = uVar4;
                puVar7 = puVar8;
                if ((long)uVar13 < (long)uVar10) {
                  uVar6 = (*(code *)*param_4)(puVar8,puVar8 + 3);
                  uVar5 = uVar13;
                  puVar7 = puVar8 + 3;
                  if ((uVar6 & 1) == 0) {
                    uVar5 = uVar4;
                    puVar7 = puVar8;
                  }
                }
                uVar13 = (*(code *)*param_4)(puVar7,&local_90);
                puVar8 = puVar14;
              } while ((uVar13 & 1) == 0);
              *puVar14 = local_90;
              puVar14[2] = local_80;
              puVar14[1] = uStack_88;
            }
          }
        }
        puVar12 = puVar12 + 3;
        puVar7 = param_3;
      } while (puVar12 != param_3);
    }
    if (0x18 < (long)uVar15) {
      uVar10 = uVar15 / 0x18;
      do {
        uVar17 = param_1[2];
        uVar18 = param_1[1];
        uVar19 = *param_1;
        uVar15 = 0;
        puVar11 = param_1;
        do {
          uVar13 = uVar15 << 1 | 1;
          uVar5 = uVar15 * 2 + 2;
          puVar12 = puVar11 + uVar15 * 3 + 3;
          uVar4 = uVar13;
          puVar8 = puVar12;
          if ((long)uVar5 < (long)uVar10) {
            uVar6 = (*(code *)*param_4)(puVar12,puVar11 + uVar15 * 3 + 6);
            uVar4 = uVar5;
            puVar8 = puVar11 + uVar15 * 3 + 6;
            if ((uVar6 & 1) == 0) {
              uVar4 = uVar13;
              puVar8 = puVar12;
            }
          }
          uVar15 = uVar4;
          *puVar11 = *puVar8;
          uVar16 = puVar8[1];
          puVar11[2] = puVar8[2];
          puVar11[1] = uVar16;
          puVar11 = puVar8;
        } while ((long)uVar15 <= (long)(uVar10 - 2 >> 1));
        if (puVar8 == param_2 + -3) {
          *puVar8 = uVar19;
          puVar8[2] = uVar17;
          puVar8[1] = uVar18;
        }
        else {
          uVar15 = (long)puVar8 + (0x18 - (long)param_1);
          *puVar8 = param_2[-3];
          uVar16 = param_2[-2];
          puVar8[2] = param_2[-1];
          puVar8[1] = uVar16;
          param_2[-3] = uVar19;
          param_2[-1] = uVar17;
          param_2[-2] = uVar18;
          if (0x18 < (long)uVar15) {
            uVar5 = uVar15 / 0x18 - 2 >> 1;
            uVar15 = (*(code *)*param_4)(param_1 + uVar5 * 3,puVar8);
            if ((uVar15 & 1) != 0) {
              local_80 = puVar8[2];
              uStack_88 = puVar8[1];
              local_90 = *puVar8;
              puVar11 = param_1 + uVar5 * 3;
              do {
                puVar12 = puVar11;
                *puVar8 = *puVar12;
                uVar18 = puVar12[1];
                puVar8[2] = puVar12[2];
                puVar8[1] = uVar18;
                if (uVar5 == 0) break;
                uVar5 = uVar5 - 1 >> 1;
                uVar15 = (*(code *)*param_4)(param_1 + uVar5 * 3,&local_90);
                puVar11 = param_1 + uVar5 * 3;
                puVar8 = puVar12;
              } while ((uVar15 & 1) != 0);
              *puVar12 = local_90;
              puVar12[2] = local_80;
              puVar12[1] = uStack_88;
            }
          }
        }
        bVar2 = 2 < (long)uVar10;
        param_2 = param_2 + -3;
        uVar10 = uVar10 - 1;
      } while (bVar2);
    }
  }
  if (*(long *)(lVar1 + 0x28) == local_78) {
    return puVar7;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

