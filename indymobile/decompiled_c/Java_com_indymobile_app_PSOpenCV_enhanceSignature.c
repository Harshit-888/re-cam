
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */

void Java_com_indymobile_app_PSOpenCV_enhanceSignature
               (_JNIEnv *param_1,undefined8 param_2,_jobject *param_3,long param_4,long param_5)

{
  uint uVar1;
  long lVar2;
  long lVar3;
  undefined4 uVar4;
  int iVar5;
  uint uVar6;
  uint uVar7;
  long lVar8;
  ColorRGB CVar9;
  bool bVar10;
  int iVar11;
  ulong uVar12;
  int *__s;
  ColorRGB *__ptr;
  ColorizingRadius *this;
  uint *puVar13;
  uint *puVar14;
  uint *puVar15;
  uint *puVar16;
  uint *puVar17;
  uint *puVar18;
  uint uVar19;
  uint uVar20;
  uint uVar21;
  ulong uVar22;
  ulong uVar23;
  long lVar24;
  uint uVar25;
  ulong uVar26;
  long lVar27;
  uint *puVar28;
  uint uVar29;
  uint uVar30;
  long lVar31;
  ColorRGB *pCVar32;
  uint *puVar33;
  ulong uVar34;
  ulong uVar35;
  long lVar36;
  ulong uVar37;
  uint uVar38;
  long lVar39;
  ulong uVar40;
  size_t __nmemb;
  double dVar41;
  double dVar42;
  undefined1 auVar43 [16];
  undefined1 auVar44 [16];
  undefined1 auVar45 [16];
  undefined1 auVar46 [16];
  undefined1 auVar47 [16];
  undefined1 auVar48 [16];
  int iVar49;
  int iVar50;
  int iVar51;
  undefined8 uVar52;
  ulong local_15f0;
  uint *local_15e0;
  undefined4 uStack_15a8;
  undefined4 uStack_15a4;
  ulong local_1580;
  ulong uStack_1578;
  undefined4 local_1568 [2];
  Mat *local_1560;
  undefined8 uStack_1558;
  uint local_1550 [2];
  Mat *apMStack_1548 [127];
  Mat aMStack_1150 [96];
  Mat aMStack_10f0 [16];
  long local_10e0;
  long *local_10a8;
  uint local_1090 [256];
  int local_c90 [256];
  int local_890 [256];
  int local_490 [256];
  long local_90;
  
  lVar8 = tpidr_el0;
  local_90 = *(long *)(lVar8 + 0x28);
  uVar12 = OpenCVUtil::checkAuthentication(param_1,param_3);
  if ((uVar12 & 1) != 0) {
    iVar11 = *(int *)(param_4 + 8);
    lVar2 = (long)iVar11;
    iVar49 = *(int *)(param_4 + 0xc);
    lVar3 = (long)iVar49;
    uVar19 = (uint)SQRT((double)((iVar11 * iVar49) / 8000));
    if ((int)uVar19 < 0xf) {
      uVar19 = 0xe;
    }
    uVar40 = (ulong)uVar19;
    uVar19 = uVar19 + 1;
    uVar12 = (ulong)uVar19;
    iVar5 = 0;
    if (uVar19 != 0) {
      iVar5 = iVar49 / (int)uVar19;
    }
    uVar6 = 0;
    if (uVar19 != 0) {
      uVar6 = iVar11 / (int)uVar19;
    }
    uVar19 = iVar5 + 1;
    uVar22 = (ulong)uVar19;
    uVar21 = uVar6 + 1;
    uVar26 = (ulong)uVar21;
    uVar38 = uVar21 * uVar19;
    lVar27 = *(long *)(param_4 + 0x10);
    uVar23 = -(ulong)(uVar38 >> 0x1f) & 0xfffffffc00000000 | (ulong)uVar38 << 2;
    __nmemb = (size_t)(int)uVar38;
    if ((int)uVar38 < 0) {
      uVar23 = 0xffffffffffffffff;
    }
    __s = operator_new__(uVar23);
    if (0 < (int)uVar38) {
      memset(__s,0xff,__nmemb << 2);
    }
    lVar39 = 0;
    auVar47 = NEON_fmov(0x3ff0000000000000,8);
    auVar43 = NEON_fmov(0x3fe0000000000000,8);
    uStack_1578 = _UNK_00105078;
    local_1580 = ram0x00105070;
    uVar52 = DAT_00104518;
    do {
      auVar44._0_8_ = (long)(int)uVar52;
      iVar11 = (int)((ulong)uVar52 >> 0x20);
      auVar44._8_8_ = (long)iVar11;
      auVar45 = NEON_scvtf(auVar44,8);
      dVar41 = pow(auVar45._8_8_ / 255.0,0.7);
      dVar42 = pow(auVar45._0_8_ / 255.0,0.7);
      uStack_15a8 = (undefined4)-(ulong)(uStack_1578 < 0x80);
      uStack_15a4 = (undefined4)(-(ulong)(uStack_1578 < 0x80) >> 0x20);
      dVar41 = cos(dVar41 * 3.141592653589793);
      dVar42 = cos(dVar42 * 3.141592653589793);
      auVar45._8_4_ = uStack_15a8;
      auVar45._0_8_ = -(ulong)(local_1580 < 0x80);
      uVar52 = CONCAT44(iVar11 + 2,(int)uVar52 + 2);
      auVar46._0_8_ = (auVar47._0_8_ - dVar42) * auVar43._0_8_ * 255.0;
      auVar46._8_8_ = (auVar47._8_8_ - dVar41) * auVar43._8_8_ * 255.0;
      auVar48._0_8_ = (long)auVar46._0_8_;
      auVar48._8_8_ = (long)auVar46._8_8_;
      auVar45._12_4_ = uStack_15a4;
      auVar46 = auVar46 ^ (auVar46 ^ auVar48) & ~auVar45;
      local_1580 = local_1580 + 2;
      uStack_1578 = uStack_1578 + 2;
      *(ulong *)((long)local_1090 + lVar39) =
           CONCAT44((int)(long)auVar46._8_8_,(int)(long)auVar46._0_8_);
      lVar39 = lVar39 + 8;
    } while (lVar39 != 0x400);
    if ((int)uVar6 < 0) {
      __ptr = calloc(__nmemb,0x1c);
    }
    else {
      lVar39 = uVar12 * (long)iVar5;
      local_15e0 = (uint *)(lVar27 + uVar40 * 4 + (uVar40 + uVar12 * uVar6) * lVar3 * 4 + lVar39 * 4
                           );
      local_15f0 = (ulong)uVar6;
      do {
        if (-1 < iVar5) {
          lVar24 = (long)iVar5;
          lVar36 = lVar39;
          puVar33 = local_15e0;
          do {
            memset(local_490,0,0x400);
            memset(local_890,0,0x400);
            memset(local_c90,0,0x400);
            iVar11 = 0;
            puVar28 = puVar33;
            uVar23 = uVar40;
            do {
              puVar16 = puVar28;
              uVar35 = uVar40;
              if ((long)(uVar23 + local_15f0 * uVar12) < lVar2) {
                do {
                  if ((long)(lVar36 + uVar35) < lVar3) {
                    uVar30 = *puVar16;
                    iVar11 = iVar11 + 1;
                    uVar37 = (ulong)(uVar30 >> 8) & 0xff;
                    uVar34 = (ulong)(uVar30 >> 0x10) & 0xff;
                    iVar49 = local_890[uVar37];
                    local_490[(ulong)uVar30 & 0xff] = local_490[(ulong)uVar30 & 0xff] + 1;
                    iVar50 = local_c90[uVar34];
                    local_890[uVar37] = iVar49 + 1;
                    local_c90[uVar34] = iVar50 + 1;
                  }
                  bVar10 = 0 < (long)uVar35;
                  puVar16 = puVar16 + -1;
                  uVar35 = uVar35 - 1;
                } while (bVar10);
              }
              puVar28 = puVar28 + -lVar3;
              bVar10 = 0 < (long)uVar23;
              uVar23 = uVar23 - 1;
            } while (bVar10);
            if (iVar11 == 0) {
              uVar30 = 0xffffffff;
            }
            else {
              lVar31 = 0xff;
              uVar20 = 0;
              uVar29 = 0;
              uVar25 = 0;
              do {
                uVar30 = (uint)lVar31;
                uVar1 = uVar30;
                if (local_490[lVar31] <= local_490[(int)uVar20]) {
                  uVar1 = uVar20;
                }
                uVar7 = uVar30;
                if (local_890[lVar31] <= local_890[(int)uVar29]) {
                  uVar7 = uVar29;
                }
                if (local_c90[lVar31] <= local_c90[(int)uVar25]) {
                  uVar30 = uVar25;
                }
                lVar31 = lVar31 + -1;
                uVar20 = uVar1;
                uVar29 = uVar7;
                uVar25 = uVar30;
              } while (lVar31 != -1);
              uVar30 = uVar7 << 8 | uVar1 << 0x10 | uVar30;
            }
            __s[local_15f0 * (long)(int)uVar19 + lVar24] = uVar30;
            puVar33 = (uint *)((long)puVar33 + (uVar40 * 4 ^ 0xfffffffffffffffc));
            lVar36 = lVar36 + ~uVar40;
            bVar10 = 0 < lVar24;
            lVar24 = lVar24 + -1;
          } while (bVar10);
        }
        local_15e0 = local_15e0 + ~uVar40 * lVar3;
        bVar10 = 0 < (long)local_15f0;
        local_15f0 = local_15f0 - 1;
      } while (bVar10);
      __ptr = calloc(__nmemb,0x1c);
      uVar30 = 0;
      uVar23 = 0;
      do {
        if (-1 < iVar5) {
          pCVar32 = __ptr + (ulong)uVar30 * 0x1c + 0xc;
          uVar35 = 0;
          do {
            uVar20 = __s[uVar30 + uVar35];
            *(uint *)(pCVar32 + -0xc) = (uint)uVar35;
            *(uint *)(pCVar32 + -8) = (uint)uVar23;
            if ((int)uVar20 < 0) {
              *(uint *)(pCVar32 + 8) = 0xffffffff;
            }
            else {
              uVar29 = uVar20 >> 0x10 & 0xff;
              uVar25 = uVar20 >> 8 & 0xff;
              uVar20 = uVar20 & 0xff;
              *(uint *)(pCVar32 + 4) = uVar20;
              *(uint *)(pCVar32 + 8) = 0;
              *(uint *)(pCVar32 + -4) = uVar29;
              *(uint *)pCVar32 = uVar25;
              if (uVar29 < 0x50) {
                CVar9 = (ColorRGB)0x0;
                if ((0x3b < uVar29) && (99 < uVar25)) {
LAB_00111ae4:
                  if ((0x77 < uVar20) || ((bool)CVar9)) {
LAB_00111af0:
                    uVar7 = uVar29 - uVar25;
                    uVar1 = -uVar7;
                    if (-1 < (int)uVar7) {
                      uVar1 = uVar7;
                    }
                    if (uVar1 < 0x3d) {
                      uVar29 = uVar29 - uVar20;
                      uVar1 = -uVar29;
                      if (-1 < (int)uVar29) {
                        uVar1 = uVar29;
                      }
                      if (uVar1 < 0x3d) {
                        uVar25 = uVar25 - uVar20;
                        uVar20 = -uVar25;
                        if (-1 < (int)uVar25) {
                          uVar20 = uVar25;
                        }
                        CVar9 = (ColorRGB)(uVar20 < 0x3d);
                        goto LAB_00111b28;
                      }
                    }
                  }
                  goto LAB_00111b24;
                }
              }
              else {
                if (0x4f < uVar25) {
                  CVar9 = (ColorRGB)(0x45 < uVar20);
                  if (99 < uVar25) goto LAB_00111ae4;
                  if (0x45 < uVar20) goto LAB_00111af0;
                }
LAB_00111b24:
                CVar9 = (ColorRGB)0x0;
              }
LAB_00111b28:
              pCVar32[0xc] = CVar9;
            }
            uVar35 = uVar35 + 1;
            pCVar32 = pCVar32 + 0x1c;
          } while (uVar22 != uVar35);
        }
        uVar23 = uVar23 + 1;
        uVar30 = uVar30 + uVar19;
      } while (uVar23 != uVar26);
    }
    PSBlockDigest::algorithm4(__s,uVar38,__ptr,uVar19,uVar21);
    free(__ptr);
    this = operator_new(0x10);
    *(undefined8 *)this = 0;
    *(undefined8 *)(this + 8) = 0;
    if ((int)uVar6 < 0) {
      free(this);
      cv::Mat::Mat(aMStack_10f0,uVar21,uVar19,0x18);
    }
    else {
      uVar38 = 0;
      uVar23 = 0;
      do {
        if (-1 < iVar5) {
          uVar35 = 0;
          do {
            if (__s[uVar38 + uVar35] < 0) {
              findSmallestRadius(this,__s,uVar19,uVar21,(int)uVar35,(int)uVar23);
              iVar11 = ColorizingRadius::getBackgroundColor(this);
              __s[uVar38 + uVar35] = iVar11;
            }
            uVar35 = uVar35 + 1;
          } while (uVar22 != uVar35);
        }
        uVar23 = uVar23 + 1;
        uVar38 = uVar38 + uVar19;
      } while (uVar23 != uVar26);
      free(this);
      cv::Mat::Mat(aMStack_10f0,uVar21,uVar19,0x18);
      if (-1 < (int)uVar6) {
        uVar21 = 0;
        uVar23 = 0;
        do {
          if (-1 < iVar5) {
            lVar39 = 0;
            do {
              uVar4 = *(undefined4 *)((long)__s + lVar39 + (ulong)uVar21 * 4);
              *(char *)(local_10e0 + uVar23 * *local_10a8 + lVar39) = (char)((uint)uVar4 >> 0x10);
              *(char *)(local_10e0 + uVar23 * *local_10a8 + lVar39 + 1) = (char)((uint)uVar4 >> 8);
              *(char *)(local_10e0 + uVar23 * *local_10a8 + lVar39 + 2) = (char)uVar4;
              lVar36 = local_10e0 + uVar23 * *local_10a8 + lVar39;
              lVar39 = lVar39 + 4;
              *(undefined1 *)(lVar36 + 3) = 0xff;
            } while (uVar22 << 2 != lVar39);
          }
          uVar23 = uVar23 + 1;
          uVar21 = uVar21 + uVar19;
        } while (uVar23 != uVar26);
      }
    }
    cv::Mat::Mat(aMStack_1150);
    apMStack_1548[0] = aMStack_10f0;
    local_1550[0] = 0x1010000;
    apMStack_1548[1] = (Mat *)0x0;
    local_1568[0] = 0x2010000;
    uStack_1558 = 0;
    local_1560 = aMStack_1150;
    cv::GaussianBlur(ZEXT816(0),ZEXT816(0),local_1550,local_1568,0x900000009,4,0);
    apMStack_1548[1] = (Mat *)0x0;
    local_1550[0] = 0x1010000;
    local_1568[0] = 0x2010000;
    uStack_1558 = 0;
    local_1560 = (Mat *)param_5;
    apMStack_1548[0] = aMStack_1150;
    cv::resize(ZEXT816(0),ZEXT816(0),local_1550,local_1568,
               CONCAT44(**(undefined4 **)(param_4 + 0x40),(*(undefined4 **)(param_4 + 0x40))[1]),2);
    cv::Mat::release();
    cv::Mat::release();
    lVar39 = *(long *)(param_5 + 0x10);
    free(__s);
    if ((int)uVar6 < 0) {
      uVar19 = 0;
    }
    else {
      uVar23 = 0;
      uVar19 = 0;
      lVar36 = (uVar40 + uVar40 * lVar3) * 4;
      puVar33 = (uint *)(lVar39 + lVar36);
      puVar28 = (uint *)(lVar27 + lVar36);
      do {
        if (-1 < iVar5) {
          lVar36 = 0;
          uVar35 = 0;
          puVar15 = puVar28;
          puVar16 = puVar33;
          uVar34 = uVar40;
          puVar14 = puVar33;
          puVar13 = puVar28;
          do {
            do {
              puVar17 = puVar15;
              puVar18 = puVar16;
              uVar37 = uVar40;
              if ((long)(uVar34 + uVar23 * uVar12) < lVar2) {
                do {
                  if ((long)(lVar36 + uVar37) < lVar3) {
                    uVar21 = *puVar18;
                    uVar38 = *puVar17;
                    uVar30 = (uVar38 & 0xff) - (uVar21 & 0xff);
                    uVar20 = (uVar38 >> 8 & 0xff) - (uVar21 >> 8 & 0xff);
                    uVar38 = (uVar38 >> 0x10 & 0xff) - (uVar21 >> 0x10 & 0xff);
                    uVar21 = -uVar30;
                    if (-1 < (int)uVar30) {
                      uVar21 = uVar30;
                    }
                    uVar30 = -uVar20;
                    if (-1 < (int)uVar20) {
                      uVar30 = uVar20;
                    }
                    uVar20 = -uVar38;
                    if (-1 < (int)uVar38) {
                      uVar20 = uVar38;
                    }
                    if ((int)uVar19 <= (int)uVar21) {
                      uVar19 = uVar21;
                    }
                    if (uVar19 <= uVar30) {
                      uVar19 = uVar30;
                    }
                    if (uVar19 <= uVar20) {
                      uVar19 = uVar20;
                    }
                  }
                  bVar10 = 0 < (long)uVar37;
                  puVar17 = puVar17 + -1;
                  puVar18 = puVar18 + -1;
                  uVar37 = uVar37 - 1;
                } while (bVar10);
              }
              bVar10 = 0 < (long)uVar34;
              puVar15 = puVar15 + -lVar3;
              puVar16 = puVar16 + -lVar3;
              uVar34 = uVar34 - 1;
            } while (bVar10);
            uVar35 = uVar35 + 1;
            puVar16 = puVar14 + uVar40 + 1;
            puVar15 = puVar13 + uVar40 + 1;
            lVar36 = lVar36 + uVar12;
            uVar34 = uVar40;
            puVar14 = puVar16;
            puVar13 = puVar15;
          } while (uVar35 != uVar22);
        }
        uVar23 = uVar23 + 1;
        puVar33 = puVar33 + uVar12 * lVar3;
        puVar28 = puVar28 + uVar12 * lVar3;
      } while (uVar23 != uVar26);
    }
    uVar21 = 1;
    do {
      uVar19 = local_1090[(int)uVar19];
      uVar38 = uVar21 + 1;
      bVar10 = uVar21 < 10;
      uVar21 = uVar38;
    } while ((int)uVar19 < 0xfa && bVar10);
    iVar50 = (int)_UNK_00105048;
    iVar51 = (int)((ulong)_UNK_00105048 >> 0x20);
    iVar11 = (int)_DAT_00105040;
    iVar49 = (int)((ulong)_DAT_00105040 >> 0x20);
    lVar36 = 0;
    do {
      lVar24 = lVar36 + 0x20;
      *(ulong *)((long)apMStack_1548 + lVar36) = CONCAT44(iVar51,iVar50);
      *(ulong *)((long)local_1550 + lVar36) = CONCAT44(iVar49,iVar11);
      *(ulong *)((long)apMStack_1548 + lVar36 + 0x10) = CONCAT44(iVar51 + 4,iVar50 + 4);
      *(ulong *)((long)apMStack_1548 + lVar36 + 8) = CONCAT44(iVar49 + 4,iVar11 + 4);
      iVar11 = iVar11 + 8;
      iVar49 = iVar49 + 8;
      iVar50 = iVar50 + 8;
      iVar51 = iVar51 + 8;
      lVar36 = lVar24;
    } while (lVar24 != 0x400);
    uVar19 = 1;
    do {
      lVar36 = 0;
      do {
        lVar24 = lVar36 + 8;
        uVar21 = local_1090[*(int *)((long)local_1550 + lVar36 + 4)];
        *(uint *)((long)local_1550 + lVar36) = local_1090[(int)*(uint *)((long)local_1550 + lVar36)]
        ;
        *(uint *)((long)local_1550 + lVar36 + 4) = uVar21;
        lVar36 = lVar24;
      } while (lVar24 != 0x400);
      uVar19 = uVar19 + 1;
    } while (uVar38 != uVar19);
    if (-1 < (int)uVar6) {
      uVar23 = 0;
      lVar36 = (uVar40 + uVar40 * lVar3) * 4;
      puVar33 = (uint *)(lVar39 + lVar36);
      puVar28 = (uint *)(lVar27 + lVar36);
      do {
        if (-1 < iVar5) {
          lVar27 = 0;
          uVar35 = 0;
          puVar15 = puVar28;
          puVar16 = puVar33;
          uVar34 = uVar40;
          puVar14 = puVar33;
          puVar13 = puVar28;
          do {
            do {
              puVar17 = puVar15;
              puVar18 = puVar16;
              uVar37 = uVar40;
              if ((long)(uVar34 + uVar23 * uVar12) < lVar2) {
                do {
                  if ((long)(lVar27 + uVar37) < lVar3) {
                    uVar19 = *puVar18;
                    uVar6 = *puVar17;
                    uVar21 = (uVar6 & 0xff) - (uVar19 & 0xff);
                    uVar38 = (uVar6 >> 8 & 0xff) - (uVar19 >> 8 & 0xff);
                    uVar6 = (uVar6 >> 0x10 & 0xff) - (uVar19 >> 0x10 & 0xff);
                    uVar19 = -uVar21;
                    if (-1 < (int)uVar21) {
                      uVar19 = uVar21;
                    }
                    uVar21 = -uVar38;
                    if (-1 < (int)uVar38) {
                      uVar21 = uVar38;
                    }
                    uVar38 = local_1550[uVar19];
                    uVar21 = local_1550[uVar21];
                    uVar19 = -uVar6;
                    if (-1 < (int)uVar6) {
                      uVar19 = uVar6;
                    }
                    uVar6 = local_1550[uVar19];
                    uVar19 = -uVar38;
                    if (-1 < (int)uVar38) {
                      uVar19 = uVar38;
                    }
                    uVar38 = -uVar21;
                    if (-1 < (int)uVar21) {
                      uVar38 = uVar21;
                    }
                    if (uVar19 <= uVar38) {
                      uVar19 = uVar38;
                    }
                    uVar21 = -uVar6;
                    if (-1 < (int)uVar6) {
                      uVar21 = uVar6;
                    }
                    if (uVar19 <= uVar21) {
                      uVar19 = uVar21;
                    }
                    if (0xfe < uVar19) {
                      uVar19 = 0xff;
                    }
                    *puVar18 = uVar19 << 0x18;
                  }
                  bVar10 = 0 < (long)uVar37;
                  puVar17 = puVar17 + -1;
                  puVar18 = puVar18 + -1;
                  uVar37 = uVar37 - 1;
                } while (bVar10);
              }
              bVar10 = 0 < (long)uVar34;
              puVar15 = puVar15 + -lVar3;
              puVar16 = puVar16 + -lVar3;
              uVar34 = uVar34 - 1;
            } while (bVar10);
            uVar35 = uVar35 + 1;
            puVar16 = puVar14 + uVar40 + 1;
            puVar15 = puVar13 + uVar40 + 1;
            lVar27 = lVar27 + uVar12;
            uVar34 = uVar40;
            puVar14 = puVar16;
            puVar13 = puVar15;
          } while (uVar35 != uVar22);
        }
        uVar23 = uVar23 + 1;
        puVar33 = puVar33 + uVar12 * lVar3;
        puVar28 = puVar28 + uVar12 * lVar3;
      } while (uVar23 != uVar26);
    }
    cv::Mat::~Mat(aMStack_1150);
    cv::Mat::~Mat(aMStack_10f0);
  }
  if (*(long *)(lVar8 + 0x28) == local_90) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

