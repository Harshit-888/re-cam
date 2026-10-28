
/* OpenCVUtil::detectLineDirectionOnHoughLines(cv::Size_<int>, std::__ndk1::vector<cv::Vec<float,
   2>, std::__ndk1::allocator<cv::Vec<float, 2> > >&, std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> > >&, std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> > >&, std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> > >&, std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> > >&) */

void OpenCVUtil::detectLineDirectionOnHoughLines
               (long param_1,long *param_2,undefined8 *param_3,undefined8 *param_4,
               undefined8 *param_5,undefined8 *param_6)

{
  undefined4 *puVar1;
  float *pfVar2;
  int iVar3;
  int iVar4;
  long lVar5;
  float fVar6;
  float fVar7;
  bool bVar8;
  bool bVar9;
  int iVar10;
  undefined4 *puVar11;
  undefined4 *puVar12;
  float *pfVar13;
  ulong uVar14;
  int iVar15;
  undefined4 *puVar16;
  float *pfVar17;
  ulong uVar18;
  undefined4 *puVar19;
  float *pfVar20;
  int iVar21;
  ulong uVar22;
  ulong uVar23;
  undefined8 *puVar24;
  undefined8 *puVar25;
  float *pfVar26;
  undefined8 *puVar27;
  float *pfVar28;
  long lVar29;
  float *pfVar30;
  float *pfVar31;
  undefined4 *puVar32;
  undefined4 *puVar33;
  ulong uVar34;
  float fVar35;
  float fVar36;
  undefined8 uVar37;
  float fVar38;
  float fVar39;
  undefined8 uVar40;
  undefined8 uVar41;
  undefined8 uVar42;
  undefined8 uVar43;
  double dVar44;
  undefined8 uVar45;
  undefined8 uVar46;
  float fVar47;
  float fVar48;
  float local_f8 [2];
  float local_f0 [2];
  float local_e8 [2];
  float local_e0 [2];
  float local_d8 [2];
  float local_d0;
  float fStack_cc;
  float local_c8;
  float fStack_c4;
  undefined8 local_c0;
  undefined8 local_b8;
  long local_b0;
  
  iVar10 = (int)param_1;
  fVar6 = (float)iVar10 / 2.0;
  lVar5 = tpidr_el0;
  local_d0 = (float)iVar10;
  iVar15 = (int)((ulong)param_1 >> 0x20);
  fVar7 = (float)iVar15 / 2.0;
  local_b0 = *(long *)(lVar5 + 0x28);
  local_c0 = DAT_001044f8;
  local_c8 = (float)iVar15;
  lVar29 = *param_2;
  local_b8 = 0;
  fStack_c4 = 1.5707964;
  fStack_cc = 0.0;
  local_d8[1] = 0.0;
  local_e0[1] = 1.5707964;
  local_e0[0] = fVar7;
  local_d8[0] = fVar6;
  if (param_2[1] != lVar29) {
    iVar21 = iVar10;
    if (iVar15 <= iVar10) {
      iVar21 = iVar15;
    }
    dVar44 = (double)iVar15;
    iVar3 = iVar10;
    if (iVar10 < 0) {
      iVar3 = iVar10 + 1;
    }
    iVar4 = iVar15 + -2;
    if (param_1 < 0) {
      iVar15 = iVar15 + 1;
    }
    uVar34 = 0;
    iVar21 = (int)((double)iVar21 * 0.16666666666666666);
    fVar47 = local_d0 + (float)((double)iVar10 * DAT_00104500);
    fVar38 = -(float)((double)iVar10 * DAT_00104500);
    fVar39 = local_c8 + (float)(dVar44 * DAT_00104500);
    fVar35 = -(float)(dVar44 * DAT_00104500);
    do {
      pfVar2 = (float *)(lVar29 + uVar34 * 8);
      fVar48 = *pfVar2;
      fVar36 = (float)MathUtil::thetaToRadian(pfVar2[1]);
      bVar8 = false;
      bVar9 = false;
      if (20.0 < fVar36) {
        bVar8 = false;
        bVar9 = true;
        if (!NAN(fVar36)) {
          bVar8 = fVar36 < 160.0;
          bVar9 = false;
        }
      }
      if (bVar8 == bVar9) {
        if ((fVar36 != 0.0) || (fVar48 < (float)(iVar10 + -2) && 2.0 < fVar48)) {
          lVar29 = uVar34 * 8;
          local_f0[0] = 0.0;
          local_f0[1] = 0.0;
          local_e8[0] = 0.0;
          local_e8[1] = 0.0;
          local_f8[0] = 0.0;
          local_f8[1] = 0.0;
          MathUtil::polarIntersect((Vec *)local_e0,(Vec *)(*param_2 + lVar29),(Point_ *)local_e8);
          MathUtil::polarIntersect((Vec *)&local_c0,(Vec *)(*param_2 + lVar29),(Point_ *)local_f0);
          MathUtil::polarIntersect((Vec *)&local_c8,(Vec *)(*param_2 + lVar29),(Point_ *)local_f8);
          if (fVar6 <= local_e8[0]) {
            if (((local_f0[0] <= fVar47) && (local_f8[0] <= fVar47)) &&
               ((float)((iVar3 >> 1) + iVar21) <= local_e8[0])) {
              puVar32 = (undefined4 *)param_6[1];
              puVar16 = (undefined4 *)(*param_2 + uVar34 * 8);
              if (puVar32 < (undefined4 *)param_6[2]) {
                puVar11 = puVar32 + 2;
                *puVar32 = *puVar16;
                puVar32[1] = puVar16[1];
              }
              else {
                puVar12 = (undefined4 *)*param_6;
                lVar29 = (long)puVar32 - (long)puVar12 >> 3;
                uVar22 = lVar29 + 1;
                if (uVar22 >> 0x3d != 0) goto LAB_001147bc;
                uVar14 = (long)param_6[2] - (long)puVar12;
                uVar18 = (long)uVar14 >> 2;
                if (uVar18 <= uVar22) {
                  uVar18 = uVar22;
                }
                if (0x7ffffffffffffff7 < uVar14) {
                  uVar18 = 0x1fffffffffffffff;
                }
                if (uVar18 == 0) {
                  puVar33 = (undefined4 *)0x0;
                }
                else {
                  if (uVar18 >> 0x3d != 0) goto LAB_00114788;
                  puVar33 = operator_new(uVar18 << 3);
                }
                puVar11 = puVar33 + lVar29 * 2;
                *puVar11 = *puVar16;
                puVar11[1] = puVar16[1];
                if (puVar12 != puVar32) {
                  uVar22 = ((long)puVar32 - (long)puVar12) - 8;
                  puVar16 = puVar12;
                  puVar19 = puVar33;
                  if ((0x37 < uVar22) &&
                     (((undefined4 *)((long)puVar12 + (uVar22 & 0xfffffffffffffff8) + 8) <= puVar33
                      || ((undefined4 *)((long)puVar33 + (uVar22 & 0xfffffffffffffff8) + 8) <=
                          puVar12)))) {
                    uVar22 = (uVar22 >> 3) + 1;
                    uVar23 = uVar22 & 0x3ffffffffffffff8;
                    puVar16 = puVar12 + uVar23 * 2;
                    puVar19 = puVar33 + uVar23 * 2;
                    puVar24 = (undefined8 *)(puVar33 + 8);
                    puVar25 = (undefined8 *)(puVar12 + 8);
                    uVar14 = uVar23;
                    do {
                      uVar37 = puVar25[-4];
                      uVar41 = puVar25[-1];
                      uVar40 = puVar25[-2];
                      uVar14 = uVar14 - 8;
                      uVar46 = puVar25[1];
                      uVar45 = *puVar25;
                      uVar43 = puVar25[3];
                      uVar42 = puVar25[2];
                      puVar24[-3] = puVar25[-3];
                      puVar24[-4] = uVar37;
                      puVar24[-1] = uVar41;
                      puVar24[-2] = uVar40;
                      puVar24[1] = uVar46;
                      *puVar24 = uVar45;
                      puVar24[3] = uVar43;
                      puVar24[2] = uVar42;
                      puVar24 = puVar24 + 8;
                      puVar25 = puVar25 + 8;
                    } while (uVar14 != 0);
                    if (uVar22 == uVar23) goto LAB_00114004;
                  }
                  do {
                    *puVar19 = *puVar16;
                    puVar1 = puVar16 + 1;
                    puVar16 = puVar16 + 2;
                    puVar19[1] = *puVar1;
                    puVar19 = puVar19 + 2;
                  } while (puVar16 != puVar32);
                }
LAB_00114004:
                puVar11 = puVar11 + 2;
                *param_6 = puVar33;
                param_6[1] = puVar11;
                param_6[2] = puVar33 + uVar18 * 2;
                if (puVar12 != (undefined4 *)0x0) {
                  operator_delete(puVar12);
                }
              }
              param_6[1] = puVar11;
            }
          }
          else if (((fVar38 <= local_f0[0]) && (fVar38 <= local_f8[0])) &&
                  (local_e8[0] <= (float)((iVar3 >> 1) - iVar21))) {
            puVar32 = (undefined4 *)param_3[1];
            puVar16 = (undefined4 *)(*param_2 + uVar34 * 8);
            if (puVar32 < (undefined4 *)param_3[2]) {
              puVar12 = puVar32 + 2;
              *puVar32 = *puVar16;
              puVar32[1] = puVar16[1];
            }
            else {
              puVar33 = (undefined4 *)*param_3;
              lVar29 = (long)puVar32 - (long)puVar33 >> 3;
              uVar22 = lVar29 + 1;
              if (uVar22 >> 0x3d != 0) goto LAB_001147a0;
              uVar14 = (long)param_3[2] - (long)puVar33;
              uVar18 = (long)uVar14 >> 2;
              if (uVar18 <= uVar22) {
                uVar18 = uVar22;
              }
              if (0x7ffffffffffffff7 < uVar14) {
                uVar18 = 0x1fffffffffffffff;
              }
              if (uVar18 == 0) {
                puVar11 = (undefined4 *)0x0;
              }
              else {
                if (uVar18 >> 0x3d != 0) goto LAB_00114788;
                puVar11 = operator_new(uVar18 << 3);
              }
              puVar12 = puVar11 + lVar29 * 2;
              *puVar12 = *puVar16;
              puVar12[1] = puVar16[1];
              if (puVar33 != puVar32) {
                uVar22 = ((long)puVar32 - (long)puVar33) - 8;
                puVar16 = puVar33;
                puVar19 = puVar11;
                if ((0x37 < uVar22) &&
                   (((undefined4 *)((long)puVar33 + (uVar22 & 0xfffffffffffffff8) + 8) <= puVar11 ||
                    ((undefined4 *)((long)puVar11 + (uVar22 & 0xfffffffffffffff8) + 8) <= puVar33)))
                   ) {
                  uVar22 = (uVar22 >> 3) + 1;
                  uVar23 = uVar22 & 0x3ffffffffffffff8;
                  puVar16 = puVar33 + uVar23 * 2;
                  puVar19 = puVar11 + uVar23 * 2;
                  puVar24 = (undefined8 *)(puVar11 + 8);
                  puVar25 = (undefined8 *)(puVar33 + 8);
                  uVar14 = uVar23;
                  do {
                    uVar37 = puVar25[-4];
                    uVar41 = puVar25[-1];
                    uVar40 = puVar25[-2];
                    uVar14 = uVar14 - 8;
                    uVar46 = puVar25[1];
                    uVar45 = *puVar25;
                    uVar43 = puVar25[3];
                    uVar42 = puVar25[2];
                    puVar24[-3] = puVar25[-3];
                    puVar24[-4] = uVar37;
                    puVar24[-1] = uVar41;
                    puVar24[-2] = uVar40;
                    puVar24[1] = uVar46;
                    *puVar24 = uVar45;
                    puVar24[3] = uVar43;
                    puVar24[2] = uVar42;
                    puVar24 = puVar24 + 8;
                    puVar25 = puVar25 + 8;
                  } while (uVar14 != 0);
                  if (uVar22 == uVar23) goto LAB_00113f14;
                }
                do {
                  *puVar19 = *puVar16;
                  puVar1 = puVar16 + 1;
                  puVar16 = puVar16 + 2;
                  puVar19[1] = *puVar1;
                  puVar19 = puVar19 + 2;
                } while (puVar16 != puVar32);
              }
LAB_00113f14:
              puVar12 = puVar12 + 2;
              *param_3 = puVar11;
              param_3[1] = puVar12;
              param_3[2] = puVar11 + uVar18 * 2;
              if (puVar33 != (undefined4 *)0x0) {
                operator_delete(puVar33);
              }
            }
            param_3[1] = puVar12;
          }
        }
      }
      else {
        bVar8 = false;
        bVar9 = true;
        if (70.0 <= fVar36) {
          bVar8 = false;
          bVar9 = true;
          if (!NAN(fVar36)) {
            bVar8 = fVar36 == 110.0;
            bVar9 = 110.0 <= fVar36;
          }
        }
        if (!bVar9 || bVar8) {
          if ((fVar36 != 90.0) || (fVar48 < (float)iVar4 && 2.0 < fVar48)) {
            lVar29 = uVar34 * 8;
            local_f0[0] = 0.0;
            local_f0[1] = 0.0;
            local_e8[0] = 0.0;
            local_e8[1] = 0.0;
            local_f8[0] = 0.0;
            local_f8[1] = 0.0;
            MathUtil::polarIntersect((Vec *)local_d8,(Vec *)(*param_2 + lVar29),(Point_ *)local_e8);
            MathUtil::polarIntersect((Vec *)&local_b8,(Vec *)(*param_2 + lVar29),(Point_ *)local_f0)
            ;
            MathUtil::polarIntersect((Vec *)&local_d0,(Vec *)(*param_2 + lVar29),(Point_ *)local_f8)
            ;
            if (fVar7 <= local_e8[1]) {
              if (((local_f0[1] <= fVar39) && (local_f8[1] <= fVar39)) &&
                 ((float)((iVar15 >> 1) + iVar21) <= local_e8[1])) {
                puVar32 = (undefined4 *)param_5[1];
                puVar16 = (undefined4 *)(*param_2 + uVar34 * 8);
                if (puVar32 < (undefined4 *)param_5[2]) {
                  puVar11 = puVar32 + 2;
                  *puVar32 = *puVar16;
                  puVar32[1] = puVar16[1];
                }
                else {
                  puVar12 = (undefined4 *)*param_5;
                  lVar29 = (long)puVar32 - (long)puVar12 >> 3;
                  uVar22 = lVar29 + 1;
                  if (uVar22 >> 0x3d != 0) goto LAB_001147f4;
                  uVar14 = (long)param_5[2] - (long)puVar12;
                  uVar18 = (long)uVar14 >> 2;
                  if (uVar18 <= uVar22) {
                    uVar18 = uVar22;
                  }
                  if (0x7ffffffffffffff7 < uVar14) {
                    uVar18 = 0x1fffffffffffffff;
                  }
                  if (uVar18 == 0) {
                    puVar33 = (undefined4 *)0x0;
                  }
                  else {
                    if (uVar18 >> 0x3d != 0) goto LAB_00114788;
                    puVar33 = operator_new(uVar18 << 3);
                  }
                  puVar11 = puVar33 + lVar29 * 2;
                  *puVar11 = *puVar16;
                  puVar11[1] = puVar16[1];
                  if (puVar12 != puVar32) {
                    uVar22 = ((long)puVar32 - (long)puVar12) - 8;
                    puVar16 = puVar12;
                    puVar19 = puVar33;
                    if ((0x37 < uVar22) &&
                       (((undefined4 *)((long)puVar12 + (uVar22 & 0xfffffffffffffff8) + 8) <=
                         puVar33 ||
                        ((undefined4 *)((long)puVar33 + (uVar22 & 0xfffffffffffffff8) + 8) <=
                         puVar12)))) {
                      uVar22 = (uVar22 >> 3) + 1;
                      uVar23 = uVar22 & 0x3ffffffffffffff8;
                      puVar16 = puVar12 + uVar23 * 2;
                      puVar19 = puVar33 + uVar23 * 2;
                      puVar24 = (undefined8 *)(puVar33 + 8);
                      puVar25 = (undefined8 *)(puVar12 + 8);
                      uVar14 = uVar23;
                      do {
                        uVar37 = puVar25[-4];
                        uVar41 = puVar25[-1];
                        uVar40 = puVar25[-2];
                        uVar14 = uVar14 - 8;
                        uVar46 = puVar25[1];
                        uVar45 = *puVar25;
                        uVar43 = puVar25[3];
                        uVar42 = puVar25[2];
                        puVar24[-3] = puVar25[-3];
                        puVar24[-4] = uVar37;
                        puVar24[-1] = uVar41;
                        puVar24[-2] = uVar40;
                        puVar24[1] = uVar46;
                        *puVar24 = uVar45;
                        puVar24[3] = uVar43;
                        puVar24[2] = uVar42;
                        puVar24 = puVar24 + 8;
                        puVar25 = puVar25 + 8;
                      } while (uVar14 != 0);
                      if (uVar22 == uVar23) goto LAB_001141f4;
                    }
                    do {
                      *puVar19 = *puVar16;
                      puVar1 = puVar16 + 1;
                      puVar16 = puVar16 + 2;
                      puVar19[1] = *puVar1;
                      puVar19 = puVar19 + 2;
                    } while (puVar16 != puVar32);
                  }
LAB_001141f4:
                  puVar11 = puVar11 + 2;
                  *param_5 = puVar33;
                  param_5[1] = puVar11;
                  param_5[2] = puVar33 + uVar18 * 2;
                  if (puVar12 != (undefined4 *)0x0) {
                    operator_delete(puVar12);
                  }
                }
                param_5[1] = puVar11;
              }
            }
            else if (((fVar35 <= local_f0[1]) && (fVar35 <= local_f8[1])) &&
                    (local_e8[1] <= (float)((iVar15 >> 1) - iVar21))) {
              puVar32 = (undefined4 *)param_4[1];
              puVar16 = (undefined4 *)(*param_2 + uVar34 * 8);
              if (puVar32 < (undefined4 *)param_4[2]) {
                puVar11 = puVar32 + 2;
                *puVar32 = *puVar16;
                puVar32[1] = puVar16[1];
              }
              else {
                puVar12 = (undefined4 *)*param_4;
                lVar29 = (long)puVar32 - (long)puVar12 >> 3;
                uVar22 = lVar29 + 1;
                if (uVar22 >> 0x3d != 0) goto LAB_001147d8;
                uVar14 = (long)param_4[2] - (long)puVar12;
                uVar18 = (long)uVar14 >> 2;
                if (uVar18 <= uVar22) {
                  uVar18 = uVar22;
                }
                if (0x7ffffffffffffff7 < uVar14) {
                  uVar18 = 0x1fffffffffffffff;
                }
                if (uVar18 == 0) {
                  puVar33 = (undefined4 *)0x0;
                }
                else {
                  if (uVar18 >> 0x3d != 0) goto LAB_00114788;
                  puVar33 = operator_new(uVar18 << 3);
                }
                puVar11 = puVar33 + lVar29 * 2;
                *puVar11 = *puVar16;
                puVar11[1] = puVar16[1];
                if (puVar12 != puVar32) {
                  uVar22 = ((long)puVar32 - (long)puVar12) - 8;
                  puVar16 = puVar12;
                  puVar19 = puVar33;
                  if ((0x37 < uVar22) &&
                     (((undefined4 *)((long)puVar12 + (uVar22 & 0xfffffffffffffff8) + 8) <= puVar33
                      || ((undefined4 *)((long)puVar33 + (uVar22 & 0xfffffffffffffff8) + 8) <=
                          puVar12)))) {
                    uVar22 = (uVar22 >> 3) + 1;
                    uVar23 = uVar22 & 0x3ffffffffffffff8;
                    puVar16 = puVar12 + uVar23 * 2;
                    puVar19 = puVar33 + uVar23 * 2;
                    puVar24 = (undefined8 *)(puVar33 + 8);
                    puVar25 = (undefined8 *)(puVar12 + 8);
                    uVar14 = uVar23;
                    do {
                      uVar37 = puVar25[-4];
                      uVar41 = puVar25[-1];
                      uVar40 = puVar25[-2];
                      uVar14 = uVar14 - 8;
                      uVar46 = puVar25[1];
                      uVar45 = *puVar25;
                      uVar43 = puVar25[3];
                      uVar42 = puVar25[2];
                      puVar24[-3] = puVar25[-3];
                      puVar24[-4] = uVar37;
                      puVar24[-1] = uVar41;
                      puVar24[-2] = uVar40;
                      puVar24[1] = uVar46;
                      *puVar24 = uVar45;
                      puVar24[3] = uVar43;
                      puVar24[2] = uVar42;
                      puVar24 = puVar24 + 8;
                      puVar25 = puVar25 + 8;
                    } while (uVar14 != 0);
                    if (uVar22 == uVar23) goto LAB_001140fc;
                  }
                  do {
                    *puVar19 = *puVar16;
                    puVar1 = puVar16 + 1;
                    puVar16 = puVar16 + 2;
                    puVar19[1] = *puVar1;
                    puVar19 = puVar19 + 2;
                  } while (puVar16 != puVar32);
                }
LAB_001140fc:
                puVar11 = puVar11 + 2;
                *param_4 = puVar33;
                param_4[1] = puVar11;
                param_4[2] = puVar33 + uVar18 * 2;
                if (puVar12 != (undefined4 *)0x0) {
                  operator_delete(puVar12);
                }
              }
              param_4[1] = puVar11;
            }
          }
        }
      }
      lVar29 = *param_2;
      uVar34 = uVar34 + 1;
    } while (uVar34 < (ulong)(param_2[1] - lVar29 >> 3));
  }
  puVar16 = (undefined4 *)param_3[1];
  if (puVar16 < (undefined4 *)param_3[2]) {
    puVar24 = (undefined8 *)(puVar16 + 2);
    *puVar16 = (undefined4)local_b8;
    puVar16[1] = local_b8._4_4_;
LAB_00114368:
    param_3[1] = puVar24;
    pfVar2 = (float *)param_6[1];
    if (pfVar2 < (float *)param_6[2]) {
      pfVar30 = pfVar2 + 2;
      *pfVar2 = local_d0;
      pfVar2[1] = fStack_cc;
    }
    else {
      pfVar31 = (float *)*param_6;
      lVar29 = (long)pfVar2 - (long)pfVar31 >> 3;
      uVar34 = lVar29 + 1;
      if (uVar34 >> 0x3d != 0) goto LAB_001147bc;
      uVar18 = (long)param_6[2] - (long)pfVar31;
      uVar22 = (long)uVar18 >> 2;
      if (uVar22 <= uVar34) {
        uVar22 = uVar34;
      }
      if (0x7ffffffffffffff7 < uVar18) {
        uVar22 = 0x1fffffffffffffff;
      }
      if (uVar22 == 0) {
        pfVar13 = (float *)0x0;
      }
      else {
        if (uVar22 >> 0x3d != 0) goto LAB_00114788;
        pfVar13 = operator_new(uVar22 << 3);
      }
      pfVar30 = pfVar13 + lVar29 * 2 + 2;
      *(ulong *)(pfVar13 + lVar29 * 2) = CONCAT44(fStack_cc,local_d0);
      if (pfVar31 != pfVar2) {
        uVar34 = ((long)pfVar2 - (long)pfVar31) - 8;
        pfVar17 = pfVar31;
        pfVar20 = pfVar13;
        if ((0x57 < uVar34) &&
           ((lVar29 = (uVar34 & 0xfffffffffffffff8) + 8,
            (float *)((long)pfVar31 + lVar29) <= pfVar13 ||
            ((float *)((long)pfVar13 + lVar29) <= pfVar31)))) {
          uVar34 = (uVar34 >> 3) + 1;
          uVar14 = uVar34 & 0x3ffffffffffffff8;
          pfVar17 = pfVar31 + uVar14 * 2;
          pfVar20 = pfVar13 + uVar14 * 2;
          pfVar26 = pfVar13 + 8;
          pfVar28 = pfVar31 + 8;
          uVar18 = uVar14;
          do {
            uVar37 = *(undefined8 *)(pfVar28 + -8);
            uVar41 = *(undefined8 *)(pfVar28 + -2);
            uVar40 = *(undefined8 *)(pfVar28 + -4);
            uVar18 = uVar18 - 8;
            uVar46 = *(undefined8 *)(pfVar28 + 2);
            uVar45 = *(undefined8 *)pfVar28;
            uVar43 = *(undefined8 *)(pfVar28 + 6);
            uVar42 = *(undefined8 *)(pfVar28 + 4);
            *(undefined8 *)(pfVar26 + -6) = *(undefined8 *)(pfVar28 + -6);
            *(undefined8 *)(pfVar26 + -8) = uVar37;
            *(undefined8 *)(pfVar26 + -2) = uVar41;
            *(undefined8 *)(pfVar26 + -4) = uVar40;
            *(undefined8 *)(pfVar26 + 2) = uVar46;
            *(undefined8 *)pfVar26 = uVar45;
            *(undefined8 *)(pfVar26 + 6) = uVar43;
            *(undefined8 *)(pfVar26 + 4) = uVar42;
            pfVar26 = pfVar26 + 0x10;
            pfVar28 = pfVar28 + 0x10;
          } while (uVar18 != 0);
          if (uVar34 == uVar14) goto LAB_0011449c;
        }
        do {
          *pfVar20 = *pfVar17;
          pfVar26 = pfVar17 + 1;
          pfVar17 = pfVar17 + 2;
          pfVar20[1] = *pfVar26;
          pfVar20 = pfVar20 + 2;
        } while (pfVar17 != pfVar2);
      }
LAB_0011449c:
      *param_6 = pfVar13;
      param_6[1] = pfVar30;
      param_6[2] = pfVar13 + uVar22 * 2;
      if (pfVar31 != (float *)0x0) {
        operator_delete(pfVar31);
      }
    }
    param_6[1] = pfVar30;
    puVar16 = (undefined4 *)param_4[1];
    if (puVar16 < (undefined4 *)param_4[2]) {
      puVar24 = (undefined8 *)(puVar16 + 2);
      *puVar16 = (undefined4)local_c0;
      puVar16[1] = local_c0._4_4_;
    }
    else {
      puVar32 = (undefined4 *)*param_4;
      lVar29 = (long)puVar16 - (long)puVar32 >> 3;
      uVar34 = lVar29 + 1;
      if (uVar34 >> 0x3d != 0) goto LAB_001147d8;
      uVar18 = (long)param_4[2] - (long)puVar32;
      uVar22 = (long)uVar18 >> 2;
      if (uVar22 <= uVar34) {
        uVar22 = uVar34;
      }
      if (0x7ffffffffffffff7 < uVar18) {
        uVar22 = 0x1fffffffffffffff;
      }
      if (uVar22 == 0) {
        puVar12 = (undefined4 *)0x0;
      }
      else {
        if (uVar22 >> 0x3d != 0) goto LAB_00114788;
        puVar12 = operator_new(uVar22 << 3);
      }
      puVar24 = (undefined8 *)((long)(puVar12 + lVar29 * 2) + 8);
      *(undefined8 *)(puVar12 + lVar29 * 2) = local_c0;
      if (puVar32 != puVar16) {
        uVar34 = ((long)puVar16 - (long)puVar32) - 8;
        puVar33 = puVar32;
        puVar11 = puVar12;
        if ((0x57 < uVar34) &&
           ((lVar29 = (uVar34 & 0xfffffffffffffff8) + 8,
            (undefined4 *)((long)puVar32 + lVar29) <= puVar12 ||
            ((undefined4 *)((long)puVar12 + lVar29) <= puVar32)))) {
          uVar34 = (uVar34 >> 3) + 1;
          uVar14 = uVar34 & 0x3ffffffffffffff8;
          puVar33 = puVar32 + uVar14 * 2;
          puVar11 = puVar12 + uVar14 * 2;
          puVar25 = (undefined8 *)(puVar12 + 8);
          puVar27 = (undefined8 *)(puVar32 + 8);
          uVar18 = uVar14;
          do {
            uVar37 = puVar27[-4];
            uVar41 = puVar27[-1];
            uVar40 = puVar27[-2];
            uVar18 = uVar18 - 8;
            uVar46 = puVar27[1];
            uVar45 = *puVar27;
            uVar43 = puVar27[3];
            uVar42 = puVar27[2];
            puVar25[-3] = puVar27[-3];
            puVar25[-4] = uVar37;
            puVar25[-1] = uVar41;
            puVar25[-2] = uVar40;
            puVar25[1] = uVar46;
            *puVar25 = uVar45;
            puVar25[3] = uVar43;
            puVar25[2] = uVar42;
            puVar25 = puVar25 + 8;
            puVar27 = puVar27 + 8;
          } while (uVar18 != 0);
          if (uVar34 == uVar14) goto LAB_001145e4;
        }
        do {
          *puVar11 = *puVar33;
          puVar19 = puVar33 + 1;
          puVar33 = puVar33 + 2;
          puVar11[1] = *puVar19;
          puVar11 = puVar11 + 2;
        } while (puVar33 != puVar16);
      }
LAB_001145e4:
      *param_4 = puVar12;
      param_4[1] = puVar24;
      param_4[2] = puVar12 + uVar22 * 2;
      if (puVar32 != (undefined4 *)0x0) {
        operator_delete(puVar32);
      }
    }
    param_4[1] = puVar24;
    pfVar2 = (float *)param_5[1];
    if (pfVar2 < (float *)param_5[2]) {
      pfVar30 = pfVar2 + 2;
      *pfVar2 = local_c8;
      pfVar2[1] = fStack_c4;
    }
    else {
      pfVar31 = (float *)*param_5;
      lVar29 = (long)pfVar2 - (long)pfVar31 >> 3;
      uVar34 = lVar29 + 1;
      if (uVar34 >> 0x3d != 0) goto LAB_001147f4;
      uVar18 = (long)param_5[2] - (long)pfVar31;
      uVar22 = (long)uVar18 >> 2;
      if (uVar22 <= uVar34) {
        uVar22 = uVar34;
      }
      if (0x7ffffffffffffff7 < uVar18) {
        uVar22 = 0x1fffffffffffffff;
      }
      if (uVar22 == 0) {
        pfVar13 = (float *)0x0;
      }
      else {
        if (uVar22 >> 0x3d != 0) goto LAB_00114788;
        pfVar13 = operator_new(uVar22 << 3);
      }
      pfVar30 = pfVar13 + lVar29 * 2 + 2;
      *(ulong *)(pfVar13 + lVar29 * 2) = CONCAT44(fStack_c4,local_c8);
      if (pfVar31 != pfVar2) {
        uVar34 = ((long)pfVar2 - (long)pfVar31) - 8;
        pfVar17 = pfVar31;
        pfVar20 = pfVar13;
        if ((0x57 < uVar34) &&
           ((lVar29 = (uVar34 & 0xfffffffffffffff8) + 8,
            (float *)((long)pfVar31 + lVar29) <= pfVar13 ||
            ((float *)((long)pfVar13 + lVar29) <= pfVar31)))) {
          uVar34 = (uVar34 >> 3) + 1;
          uVar14 = uVar34 & 0x3ffffffffffffff8;
          pfVar17 = pfVar31 + uVar14 * 2;
          pfVar20 = pfVar13 + uVar14 * 2;
          pfVar26 = pfVar13 + 8;
          pfVar28 = pfVar31 + 8;
          uVar18 = uVar14;
          do {
            uVar37 = *(undefined8 *)(pfVar28 + -8);
            uVar41 = *(undefined8 *)(pfVar28 + -2);
            uVar40 = *(undefined8 *)(pfVar28 + -4);
            uVar18 = uVar18 - 8;
            uVar46 = *(undefined8 *)(pfVar28 + 2);
            uVar45 = *(undefined8 *)pfVar28;
            uVar43 = *(undefined8 *)(pfVar28 + 6);
            uVar42 = *(undefined8 *)(pfVar28 + 4);
            *(undefined8 *)(pfVar26 + -6) = *(undefined8 *)(pfVar28 + -6);
            *(undefined8 *)(pfVar26 + -8) = uVar37;
            *(undefined8 *)(pfVar26 + -2) = uVar41;
            *(undefined8 *)(pfVar26 + -4) = uVar40;
            *(undefined8 *)(pfVar26 + 2) = uVar46;
            *(undefined8 *)pfVar26 = uVar45;
            *(undefined8 *)(pfVar26 + 6) = uVar43;
            *(undefined8 *)(pfVar26 + 4) = uVar42;
            pfVar26 = pfVar26 + 0x10;
            pfVar28 = pfVar28 + 0x10;
          } while (uVar18 != 0);
          if (uVar34 == uVar14) goto LAB_0011472c;
        }
        do {
          *pfVar20 = *pfVar17;
          pfVar26 = pfVar17 + 1;
          pfVar17 = pfVar17 + 2;
          pfVar20[1] = *pfVar26;
          pfVar20 = pfVar20 + 2;
        } while (pfVar17 != pfVar2);
      }
LAB_0011472c:
      *param_5 = pfVar13;
      param_5[1] = pfVar30;
      param_5[2] = pfVar13 + uVar22 * 2;
      if (pfVar31 != (float *)0x0) {
        operator_delete(pfVar31);
      }
    }
    param_5[1] = pfVar30;
    if (*(long *)(lVar5 + 0x28) == local_b0) {
      return;
    }
  }
  else {
    puVar32 = (undefined4 *)*param_3;
    lVar29 = (long)puVar16 - (long)puVar32 >> 3;
    uVar34 = lVar29 + 1;
    if (uVar34 >> 0x3d == 0) {
      uVar18 = (long)param_3[2] - (long)puVar32;
      uVar22 = (long)uVar18 >> 2;
      if (uVar22 <= uVar34) {
        uVar22 = uVar34;
      }
      if (0x7ffffffffffffff7 < uVar18) {
        uVar22 = 0x1fffffffffffffff;
      }
      if (uVar22 == 0) {
        puVar12 = (undefined4 *)0x0;
LAB_0011429c:
        puVar24 = (undefined8 *)((long)(puVar12 + lVar29 * 2) + 8);
        *(undefined8 *)(puVar12 + lVar29 * 2) = local_b8;
        if (puVar32 != puVar16) {
          uVar34 = ((long)puVar16 - (long)puVar32) - 8;
          puVar33 = puVar32;
          puVar11 = puVar12;
          if ((0x57 < uVar34) &&
             ((lVar29 = (uVar34 & 0xfffffffffffffff8) + 8,
              (undefined4 *)((long)puVar32 + lVar29) <= puVar12 ||
              ((undefined4 *)((long)puVar12 + lVar29) <= puVar32)))) {
            uVar34 = (uVar34 >> 3) + 1;
            uVar14 = uVar34 & 0x3ffffffffffffff8;
            puVar33 = puVar32 + uVar14 * 2;
            puVar11 = puVar12 + uVar14 * 2;
            puVar25 = (undefined8 *)(puVar12 + 8);
            puVar27 = (undefined8 *)(puVar32 + 8);
            uVar18 = uVar14;
            do {
              uVar37 = puVar27[-4];
              uVar41 = puVar27[-1];
              uVar40 = puVar27[-2];
              uVar18 = uVar18 - 8;
              uVar46 = puVar27[1];
              uVar45 = *puVar27;
              uVar43 = puVar27[3];
              uVar42 = puVar27[2];
              puVar25[-3] = puVar27[-3];
              puVar25[-4] = uVar37;
              puVar25[-1] = uVar41;
              puVar25[-2] = uVar40;
              puVar25[1] = uVar46;
              *puVar25 = uVar45;
              puVar25[3] = uVar43;
              puVar25[2] = uVar42;
              puVar25 = puVar25 + 8;
              puVar27 = puVar27 + 8;
            } while (uVar18 != 0);
            if (uVar34 == uVar14) goto LAB_00114354;
          }
          do {
            *puVar11 = *puVar33;
            puVar19 = puVar33 + 1;
            puVar33 = puVar33 + 2;
            puVar11[1] = *puVar19;
            puVar11 = puVar11 + 2;
          } while (puVar33 != puVar16);
        }
LAB_00114354:
        *param_3 = puVar12;
        param_3[1] = puVar24;
        param_3[2] = puVar12 + uVar22 * 2;
        if (puVar32 != (undefined4 *)0x0) {
          operator_delete(puVar32);
        }
        goto LAB_00114368;
      }
      if (uVar22 >> 0x3d == 0) {
        puVar12 = operator_new(uVar22 << 3);
        goto LAB_0011429c;
      }
LAB_00114788:
      if (*(long *)(lVar5 + 0x28) == local_b0) {
                    /* WARNING: Subroutine does not return */
        FUN_0010b4cc();
      }
      goto LAB_00114810;
    }
LAB_001147a0:
    if (*(long *)(lVar5 + 0x28) != local_b0) goto LAB_00114810;
    FUN_00117b48(param_3);
LAB_001147bc:
    if (*(long *)(lVar5 + 0x28) != local_b0) goto LAB_00114810;
    FUN_00117b48(param_6);
LAB_001147d8:
    if (*(long *)(lVar5 + 0x28) != local_b0) goto LAB_00114810;
    FUN_00117b48(param_4);
LAB_001147f4:
    if (*(long *)(lVar5 + 0x28) == local_b0) {
      FUN_00117b48(param_5);
    }
  }
LAB_00114810:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

