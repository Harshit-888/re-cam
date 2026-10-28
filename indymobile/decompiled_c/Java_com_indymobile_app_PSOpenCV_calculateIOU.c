
float Java_com_indymobile_app_PSOpenCV_calculateIOU
                (_JNIEnv *param_1,undefined8 param_2,_jobject *param_3,undefined8 param_4,
                undefined8 param_5)

{
  long lVar1;
  ulong uVar2;
  float *pfVar3;
  float *pfVar4;
  float fVar5;
  double dVar6;
  float fVar7;
  float fVar8;
  float fVar9;
  float fVar10;
  float fVar11;
  float fVar12;
  float fVar13;
  float fVar14;
  float fVar15;
  float fVar16;
  float fVar17;
  float fVar18;
  float fVar19;
  float fVar20;
  float fVar21;
  float fVar22;
  float fVar23;
  float fVar24;
  float fVar25;
  float fVar26;
  double local_d0;
  double dStack_c8;
  double dStack_c0;
  double dStack_b8;
  double local_b0;
  double dStack_a8;
  double dStack_a0;
  double dStack_98;
  double local_90;
  double dStack_88;
  double dStack_80;
  double dStack_78;
  double local_70;
  double dStack_68;
  double dStack_60;
  double dStack_58;
  long local_48;
  
  lVar1 = tpidr_el0;
  local_48 = *(long *)(lVar1 + 0x28);
  uVar2 = OpenCVUtil::checkAuthentication(param_1,param_3);
  fVar14 = 0.0;
  if ((uVar2 & 1) != 0) {
    pfVar3 = (float *)(**(code **)(*(long *)param_1 + 0x5e8))(param_1,param_4,0);
    pfVar4 = (float *)(**(code **)(*(long *)param_1 + 0x5e8))(param_1,param_5,0);
    fVar9 = pfVar3[4];
    fVar8 = pfVar3[6];
    fVar11 = pfVar3[2];
    fVar13 = *pfVar3;
    fVar14 = fVar8;
    if (fVar8 <= fVar9) {
      fVar14 = fVar9;
    }
    fVar16 = fVar11;
    if (fVar11 < fVar14) {
      fVar16 = fVar14;
    }
    if (fVar16 <= fVar13) {
      fVar16 = fVar13;
    }
    fVar7 = pfVar4[4];
    fVar5 = pfVar4[6];
    fVar10 = pfVar4[2];
    fVar12 = *pfVar4;
    fVar14 = fVar5;
    if (fVar5 <= fVar7) {
      fVar14 = fVar7;
    }
    fVar17 = fVar10;
    if (fVar10 < fVar14) {
      fVar17 = fVar14;
    }
    if (fVar17 <= fVar12) {
      fVar17 = fVar12;
    }
    fVar20 = pfVar3[5];
    fVar19 = pfVar3[7];
    fVar22 = pfVar3[3];
    fVar25 = pfVar3[1];
    fVar14 = fVar19;
    if (fVar19 <= fVar20) {
      fVar14 = fVar20;
    }
    fVar26 = fVar22;
    if (fVar22 < fVar14) {
      fVar26 = fVar14;
    }
    if (fVar26 <= fVar25) {
      fVar26 = fVar25;
    }
    fVar18 = pfVar4[5];
    fVar15 = pfVar4[7];
    fVar21 = pfVar4[3];
    fVar23 = pfVar4[1];
    fVar14 = fVar15;
    if (fVar15 <= fVar18) {
      fVar14 = fVar18;
    }
    fVar24 = fVar21;
    if (fVar21 < fVar14) {
      fVar24 = fVar14;
    }
    if (fVar24 <= fVar23) {
      fVar24 = fVar23;
    }
    if (fVar17 <= fVar16) {
      fVar17 = fVar16;
    }
    if (fVar24 <= fVar26) {
      fVar24 = fVar26;
    }
    if (fVar17 <= fVar24) {
      fVar17 = fVar24;
    }
    fVar17 = fVar17 / 100.0;
    local_90 = (double)(fVar13 / fVar17);
    dStack_88 = (double)(fVar25 / fVar17);
    dStack_80 = (double)(fVar11 / fVar17);
    dStack_78 = (double)(fVar22 / fVar17);
    local_70 = (double)(fVar9 / fVar17);
    dStack_68 = (double)(fVar20 / fVar17);
    dStack_60 = (double)(fVar8 / fVar17);
    dStack_58 = (double)(fVar19 / fVar17);
    local_d0 = (double)(fVar12 / fVar17);
    dStack_c8 = (double)(fVar23 / fVar17);
    dStack_c0 = (double)(fVar10 / fVar17);
    dStack_b8 = (double)(fVar21 / fVar17);
    local_b0 = (double)(fVar7 / fVar17);
    dStack_a8 = (double)(fVar18 / fVar17);
    dStack_a0 = (double)(fVar5 / fVar17);
    dStack_98 = (double)(fVar15 / fVar17);
    dVar6 = (double)IOU::iou((Quad *)&local_90,(Quad *)&local_d0);
    fVar14 = (float)dVar6;
  }
  if (*(long *)(lVar1 + 0x28) != local_48) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return fVar14;
}

