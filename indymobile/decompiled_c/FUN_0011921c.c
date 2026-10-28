
void FUN_0011921c(long *param_1,long *param_2)

{
  long *plVar1;
  undefined8 *puVar2;
  undefined4 uVar3;
  long lVar4;
  long lVar5;
  void *pvVar6;
  long *plVar7;
  undefined8 uVar8;
  long *plVar9;
  long lVar10;
  long lVar11;
  long lVar12;
  long lVar13;
  long lVar14;
  undefined8 uVar15;
  long lVar16;
  undefined8 uVar17;
  undefined8 uVar18;
  long lVar19;
  long lVar20;
  long lVar21;
  
  if ((param_1 != param_2) && (param_1 + 8 != param_2)) {
    lVar11 = 0;
    plVar7 = param_1;
    plVar9 = param_1 + 8;
    do {
      if ((int)plVar7[7] < (int)plVar7[0xf]) {
        lVar20 = plVar9[1];
        lVar19 = *plVar9;
        lVar13 = plVar7[10];
        *plVar9 = 0;
        plVar9[1] = 0;
        plVar9[2] = 0;
        lVar12 = plVar7[0xb];
        lVar16 = plVar7[0xd];
        lVar14 = plVar7[0xc];
        lVar21 = plVar7[0xe];
        lVar4 = plVar7[0xf];
        plVar7[0xc] = 0;
        plVar7[0xd] = 0;
        plVar7[0xb] = 0;
        lVar5 = lVar11;
        do {
          lVar10 = lVar5;
          puVar2 = (undefined8 *)((long)param_1 + lVar10);
          pvVar6 = (void *)puVar2[8];
          if (pvVar6 != (void *)0x0) {
            puVar2[9] = pvVar6;
            operator_delete(pvVar6);
            puVar2[9] = 0;
            puVar2[10] = 0;
          }
          uVar17 = puVar2[1];
          uVar8 = puVar2[2];
          pvVar6 = (void *)puVar2[0xb];
          puVar2[1] = 0;
          puVar2[2] = 0;
          puVar2[9] = uVar17;
          puVar2[8] = *puVar2;
          puVar2[10] = uVar8;
          *puVar2 = 0;
          if (pvVar6 != (void *)0x0) {
            *(void **)((long)param_1 + lVar10 + 0x60) = pvVar6;
            operator_delete(pvVar6);
            *(undefined8 *)((long)param_1 + lVar10 + 0x60) = 0;
            *(undefined8 *)((long)param_1 + lVar10 + 0x68) = 0;
          }
          uVar8 = *(undefined8 *)((long)param_1 + lVar10 + 0x28);
          uVar17 = *(undefined8 *)((long)param_1 + lVar10 + 0x30);
          uVar18 = *(undefined8 *)((long)param_1 + lVar10 + 0x20);
          uVar15 = *(undefined8 *)((long)param_1 + lVar10 + 0x18);
          uVar3 = *(undefined4 *)((long)param_1 + lVar10 + 0x38);
          *(undefined8 *)((long)param_1 + lVar10 + 0x20) = 0;
          *(undefined8 *)((long)param_1 + lVar10 + 0x28) = 0;
          *(undefined8 *)((long)param_1 + lVar10 + 0x18) = 0;
          puVar2[0xc] = uVar18;
          puVar2[0xb] = uVar15;
          *(undefined8 *)((long)param_1 + lVar10 + 0x68) = uVar8;
          *(undefined8 *)((long)param_1 + lVar10 + 0x70) = uVar17;
          *(undefined4 *)((long)param_1 + lVar10 + 0x78) = uVar3;
          if (lVar10 == 0) {
            pvVar6 = (void *)*param_1;
            plVar7 = param_1;
            if (pvVar6 == (void *)0x0) goto LAB_00119384;
            goto LAB_0011937c;
          }
          lVar5 = lVar10 + -0x40;
        } while (*(int *)((long)param_1 + lVar10 + -8) < (int)lVar4);
        plVar7 = (long *)((long)param_1 + lVar10);
        pvVar6 = (void *)*plVar7;
        if (pvVar6 != (void *)0x0) {
LAB_0011937c:
          plVar7[1] = (long)pvVar6;
          operator_delete(pvVar6);
        }
LAB_00119384:
        plVar7[2] = lVar13;
        plVar7[1] = lVar20;
        *plVar7 = lVar19;
        pvVar6 = *(void **)((long)param_1 + lVar10 + 0x18);
        if (pvVar6 != (void *)0x0) {
          plVar7[4] = (long)pvVar6;
          operator_delete(pvVar6);
          *(undefined8 *)((long)param_1 + lVar10 + 0x20) = 0;
          *(undefined8 *)((long)param_1 + lVar10 + 0x28) = 0;
        }
        *(long *)((long)param_1 + lVar10 + 0x18) = lVar12;
        plVar7[5] = lVar16;
        plVar7[4] = lVar14;
        *(long *)((long)param_1 + lVar10 + 0x30) = lVar21;
        *(int *)((long)param_1 + lVar10 + 0x38) = (int)lVar4;
      }
      plVar1 = plVar9 + 8;
      lVar11 = lVar11 + 0x40;
      plVar7 = plVar9;
      plVar9 = plVar1;
    } while (plVar1 != param_2);
  }
  return;
}

