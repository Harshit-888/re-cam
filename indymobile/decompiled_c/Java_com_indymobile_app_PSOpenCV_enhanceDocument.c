
/* WARNING: Globals starting with '_' overlap smaller symbols at the same address */

uint Java_com_indymobile_app_PSOpenCV_enhanceDocument
               (_JNIEnv *param_1,undefined8 param_2,_jobject *param_3,long param_4,
               undefined4 *param_5,int param_6,int param_7)

{
  bool bVar1;
  uint uVar2;
  void *pvVar3;
  long lVar4;
  undefined4 uVar5;
  byte bVar6;
  uint uVar7;
  uint uVar8;
  long lVar9;
  undefined1 auVar10 [16];
  bool bVar11;
  uint uVar12;
  undefined1 auVar13 [16];
  uint uVar14;
  ColorRGB CVar15;
  bool bVar16;
  bool bVar17;
  bool bVar18;
  uint uVar19;
  ulong uVar20;
  uint *puVar21;
  void *__ptr;
  void *__ptr_00;
  int *__ptr_01;
  void *__ptr_02;
  ColorRGB *__ptr_03;
  ColorizingRadius *this;
  uint *puVar22;
  uint *puVar23;
  uint uVar24;
  int iVar25;
  uint uVar26;
  ulong uVar27;
  ulong uVar28;
  ulong uVar29;
  ulong uVar30;
  long lVar31;
  long lVar32;
  byte *pbVar33;
  uint uVar34;
  ulong uVar35;
  uint *puVar36;
  uint uVar37;
  int iVar38;
  uint uVar39;
  int iVar40;
  uint uVar41;
  uint *puVar42;
  long lVar43;
  uint uVar44;
  ColorRGB *pCVar45;
  uint uVar46;
  ulong uVar47;
  int iVar48;
  ulong uVar49;
  int iVar50;
  ulong uVar51;
  int iVar52;
  uint uVar53;
  long lVar54;
  uint *puVar55;
  size_t __nmemb;
  double dVar56;
  double dVar57;
  double dVar58;
  double dVar59;
  double dVar60;
  double dVar61;
  undefined1 auVar62 [16];
  double dVar66;
  double dVar67;
  undefined1 auVar63 [16];
  undefined1 auVar64 [16];
  undefined1 auVar65 [16];
  float fVar68;
  float fVar69;
  undefined8 uVar70;
  undefined8 uVar71;
  uint *local_ed0;
  uint *local_ec0;
  ulong local_eb0;
  uint *local_e90;
  undefined4 uStack_e88;
  undefined4 uStack_e84;
  ulong local_e50;
  ulong uStack_e48;
  undefined4 local_de0 [2];
  undefined4 *local_dd8;
  undefined8 uStack_dd0;
  undefined4 local_dc8 [2];
  Mat *local_dc0;
  undefined8 uStack_db8;
  undefined4 local_db0 [2];
  Mat *local_da8;
  byte *local_da0;
  Mat aMStack_d50 [96];
  Mat aMStack_cf0 [16];
  long local_ce0;
  long *local_ca8;
  int local_c90 [256];
  int local_890 [256];
  int local_490 [256];
  long local_90;
  
  lVar9 = tpidr_el0;
  local_90 = *(long *)(lVar9 + 0x28);
  uVar20 = OpenCVUtil::checkAuthentication(param_1,param_3);
  if ((uVar20 & 1) == 0) {
    uVar24 = 0;
    goto LAB_00111510;
  }
  iVar52 = *(int *)(param_4 + 8);
  iVar50 = *(int *)(param_4 + 0xc);
  lVar4 = (long)iVar50;
  local_ed0 = *(uint **)(param_4 + 0x10);
  uVar46 = iVar52 * iVar50;
  uVar20 = (ulong)uVar46;
  uVar24 = (uint)SQRT((double)((int)uVar46 / 8000));
  if ((int)uVar24 < 0xf) {
    uVar24 = 0xe;
  }
  uVar27 = (ulong)uVar24;
  uVar24 = uVar24 + 1;
  uVar28 = (ulong)uVar24;
  iVar48 = 0;
  if (uVar24 != 0) {
    iVar48 = iVar50 / (int)uVar24;
  }
  uVar41 = 0;
  if (uVar24 != 0) {
    uVar41 = iVar52 / (int)uVar24;
  }
  uVar44 = iVar48 + 1;
  uVar29 = (ulong)uVar44;
  uVar26 = uVar41 + 1;
  uVar35 = (ulong)uVar26;
  uVar24 = uVar26 * uVar44;
  uVar30 = -(ulong)(uVar24 >> 0x1f) & 0xfffffffc00000000 | (ulong)uVar24 << 2;
  __nmemb = (size_t)(int)uVar24;
  if ((int)uVar24 < 0) {
    uVar30 = 0xffffffffffffffff;
  }
  puVar21 = operator_new__(uVar30);
  if (0 < (int)uVar24) {
    memset(puVar21,0xff,__nmemb << 2);
  }
  uVar70 = DAT_00104518;
  uStack_e48 = _UNK_00105078;
  local_e50 = ram0x00105070;
  __ptr = operator_new__(0x400);
  auVar62 = NEON_fmov(0x3ff0000000000000,8);
  lVar54 = 0;
  dVar66 = auVar62._8_8_;
  dVar56 = auVar62._0_8_;
  auVar62 = NEON_fmov(0x3fe0000000000000,8);
  dVar67 = auVar62._8_8_;
  dVar57 = auVar62._0_8_;
  do {
    auVar63._0_8_ = (long)(int)uVar70;
    iVar50 = (int)((ulong)uVar70 >> 0x20);
    auVar63._8_8_ = (long)iVar50;
    auVar62 = NEON_scvtf(auVar63,8);
    dVar58 = pow(auVar62._8_8_ / 255.0,0.625);
    dVar59 = pow(auVar62._0_8_ / 255.0,0.625);
    uStack_e88 = (undefined4)-(ulong)(uStack_e48 < 0x80);
    uStack_e84 = (undefined4)(-(ulong)(uStack_e48 < 0x80) >> 0x20);
    dVar58 = cos(dVar58 * 3.141592653589793);
    dVar59 = cos(dVar59 * 3.141592653589793);
    auVar10._8_4_ = uStack_e88;
    auVar10._0_8_ = -(ulong)(local_e50 < 0x80);
    uVar70 = CONCAT44(iVar50 + 2,(int)uVar70 + 2);
    auVar64._0_8_ = (dVar56 - dVar59) * dVar57 * 255.0;
    auVar64._8_8_ = (dVar66 - dVar58) * dVar67 * 255.0;
    auVar62._8_4_ = (int)(long)auVar64._8_8_;
    auVar62._0_8_ = (long)auVar64._0_8_;
    auVar62._12_4_ = (int)((ulong)(long)auVar64._8_8_ >> 0x20);
    auVar10._12_4_ = uStack_e84;
    auVar64 = auVar64 ^ (auVar64 ^ auVar62) & ~auVar10;
    local_e50 = local_e50 + 2;
    uStack_e48 = uStack_e48 + 2;
    *(ulong *)((long)__ptr + lVar54) = CONCAT44((int)(long)auVar64._8_8_,(int)(long)auVar64._0_8_);
    uVar71 = DAT_00104518;
    lVar54 = lVar54 + 8;
  } while (lVar54 != 0x400);
  __ptr_00 = operator_new__(0x400);
  dVar58 = DAT_00104550;
  lVar54 = 0;
  do {
    auVar65._0_8_ = (long)(int)uVar71;
    iVar50 = (int)((ulong)uVar71 >> 0x20);
    auVar65._8_8_ = (long)iVar50;
    auVar62 = NEON_scvtf(auVar65,8);
    dVar59 = pow(auVar62._8_8_ / 255.0,dVar58);
    dVar60 = pow(auVar62._0_8_ / 255.0,dVar58);
    dVar59 = cos(dVar59 * 3.141592653589793);
    dVar60 = cos(dVar60 * 3.141592653589793);
    uVar71 = CONCAT44(iVar50 + 2,(int)uVar71 + 2);
    *(ulong *)((long)__ptr_00 + lVar54) =
         CONCAT44((int)(long)(double)(long)((dVar66 - dVar59) * dVar67 * 255.0),
                  (int)(long)(double)(long)((dVar56 - dVar60) * dVar57 * 255.0));
    lVar54 = lVar54 + 8;
  } while (lVar54 != 0x400);
  uStack_e48 = _UNK_00105048;
  local_e50 = _DAT_00105040;
  __ptr_01 = operator_new__(0x1f44);
  lVar54 = -8000;
  do {
    auVar13._8_8_ = uStack_e48;
    auVar13._0_8_ = local_e50;
    auVar62 = NEON_scvtf(auVar13,4);
    dVar58 = pow((double)(auVar62._4_4_ / 2000.0),0.7);
    dVar59 = pow((double)(auVar62._0_4_ / 2000.0),0.7);
    dVar60 = pow((double)(auVar62._12_4_ / 2000.0),0.7);
    dVar61 = pow((double)(auVar62._8_4_ / 2000.0),0.7);
    dVar58 = cos(dVar58 * 3.141592653589793);
    dVar59 = cos(dVar59 * 3.141592653589793);
    dVar60 = cos(dVar60 * 3.141592653589793);
    dVar61 = cos(dVar61 * 3.141592653589793);
    lVar31 = lVar54 + 0x10;
    *(int *)((long)__ptr_01 + lVar54 + 0x1f48) =
         (int)(long)(double)(long)((dVar56 - dVar61) * dVar57 * 2000.0);
    *(int *)((long)__ptr_01 + lVar54 + 0x1f4c) =
         (int)(long)(double)(long)((dVar66 - dVar60) * dVar67 * 2000.0);
    *(int *)((long)__ptr_01 + lVar54 + 8000) =
         (int)(long)(double)(long)((dVar56 - dVar59) * dVar57 * 2000.0);
    *(int *)((long)__ptr_01 + lVar54 + 0x1f44) =
         (int)(long)(double)(long)((dVar66 - dVar58) * dVar67 * 2000.0);
    uStack_e48 = CONCAT44((int)(uStack_e48 >> 0x20) + 4,(int)uStack_e48 + 4);
    local_e50 = CONCAT44((int)(local_e50 >> 0x20) + 4,(int)local_e50 + 4);
    lVar54 = lVar31;
  } while (lVar31 != 0);
  __ptr_01[2000] = 2000;
  __ptr_02 = operator_new__(0x10000);
  lVar31 = 0;
  lVar54 = 1;
  iVar50 = (int)((ulong)((long)(*__ptr_01 * 0xff) * -0x10624dd3) >> 0x20);
  uVar39 = (iVar50 >> 7) - (iVar50 >> 0x1f);
  uVar39 = uVar39 & (int)uVar39 >> 0x1f;
  if ((int)uVar39 < -0xfe) {
    uVar39 = 0xffffff01;
  }
  do {
    uVar19 = (int)lVar31 * 2000;
    *(char *)((long)__ptr_02 + lVar31 * 0x100) = (char)uVar39 + -1;
    lVar32 = 0;
    do {
      uVar37 = (int)lVar32 + 1;
      lVar43 = lVar32 + 2;
      uVar34 = 0;
      if (uVar37 != 0) {
        uVar34 = uVar19 / uVar37;
      }
      uVar37 = 0;
      if ((uint)lVar43 != 0) {
        uVar37 = uVar19 / (uint)lVar43;
      }
      if (1999 < uVar34) {
        uVar34 = 2000;
      }
      if (1999 < uVar37) {
        uVar37 = 2000;
      }
      iVar50 = __ptr_01[uVar37];
      iVar40 = (int)((ulong)((long)(__ptr_01[uVar34] * 0xff) * -0x10624dd3) >> 0x20);
      uVar37 = (iVar40 >> 7) - (iVar40 >> 0x1f);
      uVar37 = uVar37 & (int)uVar37 >> 0x1f;
      if ((int)uVar37 < -0xfe) {
        uVar37 = 0xffffff01;
      }
      *(char *)((long)__ptr_02 + lVar32 + lVar54) = (char)uVar37 + -1;
      iVar50 = (int)((ulong)((long)(iVar50 * 0xff) * -0x10624dd3) >> 0x20);
      uVar37 = (iVar50 >> 7) - (iVar50 >> 0x1f);
      uVar37 = uVar37 & (int)uVar37 >> 0x1f;
      if ((int)uVar37 < -0xfe) {
        uVar37 = 0xffffff01;
      }
      *(char *)((long)__ptr_02 + lVar32 + lVar54 + 1) = (char)uVar37 + -1;
      lVar32 = lVar43;
    } while (lVar43 != 0xfe);
    lVar54 = lVar54 + 0x100;
    uVar19 = uVar19 / 0xff;
    if (1999 < uVar19) {
      uVar19 = 2000;
    }
    iVar50 = (int)((ulong)((long)(__ptr_01[uVar19] * 0xff) * -0x10624dd3) >> 0x20);
    uVar19 = (iVar50 >> 7) - (iVar50 >> 0x1f);
    lVar32 = lVar31 * 0x100;
    lVar31 = lVar31 + 1;
    uVar19 = uVar19 & (int)uVar19 >> 0x1f;
    if ((int)uVar19 < -0xfe) {
      uVar19 = 0xffffff01;
    }
    *(char *)((long)__ptr_02 + lVar32 + 0xff) = (char)uVar19 + -1;
  } while (lVar31 != 0x100);
  pvVar3 = __ptr_00;
  if (param_6 != 1) {
    pvVar3 = __ptr;
  }
  if ((int)uVar41 < 0) {
    __ptr_03 = calloc(__nmemb,0x1c);
  }
  else {
    lVar54 = uVar28 * (long)iVar48;
    local_e90 = local_ed0 + uVar27 + lVar54 + (uVar27 + uVar28 * uVar41) * lVar4;
    local_eb0 = (ulong)uVar41;
    do {
      if (-1 < iVar48) {
        lVar32 = (long)iVar48;
        puVar22 = local_e90;
        lVar31 = lVar54;
        do {
          memset(local_490,0,0x400);
          memset(local_890,0,0x400);
          memset(local_c90,0,0x400);
          iVar50 = 0;
          puVar36 = puVar22;
          uVar30 = uVar27;
          do {
            puVar42 = puVar36;
            uVar49 = uVar27;
            if ((long)(uVar30 + local_eb0 * uVar28) < (long)iVar52) {
              do {
                if ((long)(lVar31 + uVar49) < lVar4) {
                  uVar39 = *puVar42;
                  iVar50 = iVar50 + 1;
                  uVar51 = (ulong)(uVar39 >> 8) & 0xff;
                  uVar47 = (ulong)(uVar39 >> 0x10) & 0xff;
                  iVar40 = local_890[uVar51];
                  local_490[(ulong)uVar39 & 0xff] = local_490[(ulong)uVar39 & 0xff] + 1;
                  iVar38 = local_c90[uVar47];
                  local_890[uVar51] = iVar40 + 1;
                  local_c90[uVar47] = iVar38 + 1;
                }
                bVar18 = 0 < (long)uVar49;
                puVar42 = puVar42 + -1;
                uVar49 = uVar49 - 1;
              } while (bVar18);
            }
            puVar36 = puVar36 + -lVar4;
            bVar18 = 0 < (long)uVar30;
            uVar30 = uVar30 - 1;
          } while (bVar18);
          if (iVar50 == 0) {
            uVar39 = 0xffffffff;
          }
          else {
            lVar43 = 0xff;
            uVar19 = 0;
            uVar37 = 0;
            uVar34 = 0;
            do {
              uVar39 = (uint)lVar43;
              uVar7 = uVar39;
              if (local_490[lVar43] <= local_490[(int)uVar19]) {
                uVar7 = uVar19;
              }
              uVar53 = uVar39;
              if (local_890[lVar43] <= local_890[(int)uVar37]) {
                uVar53 = uVar37;
              }
              if (local_c90[lVar43] <= local_c90[(int)uVar34]) {
                uVar39 = uVar34;
              }
              lVar43 = lVar43 + -1;
              uVar19 = uVar7;
              uVar37 = uVar53;
              uVar34 = uVar39;
            } while (lVar43 != -1);
            uVar39 = uVar53 << 8 | uVar7 << 0x10 | uVar39;
          }
          puVar21[local_eb0 * (long)(int)uVar44 + lVar32] = uVar39;
          puVar22 = (uint *)((long)puVar22 + (uVar27 * 4 ^ 0xfffffffffffffffc));
          lVar31 = lVar31 + ~uVar27;
          bVar18 = 0 < lVar32;
          lVar32 = lVar32 + -1;
        } while (bVar18);
      }
      local_e90 = local_e90 + ~uVar27 * lVar4;
      bVar18 = 0 < (long)local_eb0;
      local_eb0 = local_eb0 - 1;
    } while (bVar18);
    __ptr_03 = calloc(__nmemb,0x1c);
    uVar39 = 0;
    uVar30 = 0;
    do {
      if (-1 < iVar48) {
        pCVar45 = __ptr_03 + (ulong)uVar39 * 0x1c + 0xc;
        uVar49 = 0;
        do {
          uVar19 = puVar21[uVar39 + uVar49];
          *(uint *)(pCVar45 + -0xc) = (uint)uVar49;
          *(uint *)(pCVar45 + -8) = (uint)uVar30;
          if ((int)uVar19 < 0) {
            *(uint *)(pCVar45 + 8) = 0xffffffff;
          }
          else {
            uVar37 = uVar19 >> 0x10 & 0xff;
            uVar34 = uVar19 >> 8 & 0xff;
            uVar19 = uVar19 & 0xff;
            *(uint *)(pCVar45 + 4) = uVar19;
            *(uint *)(pCVar45 + 8) = 0;
            *(uint *)(pCVar45 + -4) = uVar37;
            *(uint *)pCVar45 = uVar34;
            if (uVar37 < 0x50) {
              CVar15 = (ColorRGB)0x0;
              if ((uVar37 < 0x3c) || (uVar34 < 100)) {
LAB_00110ab8:
                pCVar45[0xc] = CVar15;
                goto LAB_00110a04;
              }
LAB_00110a7c:
              if ((0x77 < uVar19) || ((bool)CVar15)) {
LAB_00110a88:
                uVar53 = uVar37 - uVar34;
                uVar7 = -uVar53;
                if (-1 < (int)uVar53) {
                  uVar7 = uVar53;
                }
                if (uVar7 < 0x3d) {
                  uVar37 = uVar37 - uVar19;
                  uVar7 = -uVar37;
                  if (-1 < (int)uVar37) {
                    uVar7 = uVar37;
                  }
                  if (uVar7 < 0x3d) {
                    uVar34 = uVar34 - uVar19;
                    uVar19 = -uVar34;
                    if (-1 < (int)uVar34) {
                      uVar19 = uVar34;
                    }
                    CVar15 = (ColorRGB)(uVar19 < 0x3d);
                    goto LAB_00110ab8;
                  }
                }
              }
            }
            else if (0x4f < uVar34) {
              CVar15 = (ColorRGB)(0x45 < uVar19);
              if (99 < uVar34) goto LAB_00110a7c;
              if (0x45 < uVar19) goto LAB_00110a88;
            }
            pCVar45[0xc] = (ColorRGB)0x0;
          }
LAB_00110a04:
          uVar49 = uVar49 + 1;
          pCVar45 = pCVar45 + 0x1c;
        } while (uVar29 != uVar49);
      }
      uVar30 = uVar30 + 1;
      uVar39 = uVar39 + uVar44;
    } while (uVar30 != uVar35);
  }
  PSBlockDigest::algorithm4((int *)puVar21,uVar24,__ptr_03,uVar44,uVar26);
  free(__ptr_03);
  if ((int)uVar24 < 1) {
    iVar50 = 0;
    iVar38 = 0;
    iVar40 = 0;
    iVar25 = 0;
  }
  else {
    iVar25 = 0;
    iVar40 = 0;
    iVar38 = 0;
    iVar50 = 0;
    puVar22 = puVar21;
    do {
      uVar24 = *puVar22;
      if (-1 < (int)uVar24) {
        iVar50 = iVar50 + (uVar24 & 0xff);
        iVar25 = iVar25 + 1;
        iVar40 = iVar40 + (uVar24 >> 0x10 & 0xff);
        iVar38 = iVar38 + (uVar24 >> 8 & 0xff);
      }
      __nmemb = __nmemb - 1;
      puVar22 = puVar22 + 1;
    } while (__nmemb != 0);
  }
  uVar39 = 0;
  if (iVar25 != 0) {
    uVar39 = iVar40 / iVar25;
  }
  uVar19 = 0;
  if (iVar25 != 0) {
    uVar19 = iVar38 / iVar25;
  }
  uVar37 = 0;
  if (iVar25 != 0) {
    uVar37 = iVar50 / iVar25;
  }
  bVar11 = (int)uVar19 < 100;
  bVar16 = uVar37 == 0x77;
  bVar1 = (((0x3b < (int)uVar39 && uVar19 != 99) && ((int)uVar39 < 0x3c || 0x62 < (int)uVar19)) &&
          (int)(uVar37 - 0x77) < 0) !=
          (((0x3b < (int)uVar39 && uVar19 != 99) && ((int)uVar39 < 0x3c || 0x62 < (int)uVar19)) &&
          SBORROW4(uVar37,0x77));
  bVar17 = uVar37 == 0x45;
  bVar18 = (((0x4f < (int)uVar39 && uVar19 != 0x4f) && ((int)uVar39 < 0x50 || 0x4e < (int)uVar19))
           && (int)(uVar37 - 0x45) < 0) !=
           (((0x4f < (int)uVar39 && uVar19 != 0x4f) && ((int)uVar39 < 0x50 || 0x4e < (int)uVar19))
           && SBORROW4(uVar37,0x45));
  uVar24 = 0xff;
  if (((((int)uVar39 < 0x50 || (int)uVar19 < 0x50) || bVar17) || bVar18) &&
      ((((int)uVar39 < 0x3c || bVar11) || bVar16) || bVar1)) {
    uVar24 = uVar37;
  }
  uVar37 = 0xff;
  if (((((int)uVar39 < 0x50 || (int)uVar19 < 0x50) || bVar17) || bVar18) &&
      ((((int)uVar39 < 0x3c || bVar11) || bVar16) || bVar1)) {
    uVar37 = uVar19;
  }
  uVar34 = 0xff;
  if (((((int)uVar39 < 0x50 || (int)uVar19 < 0x50) || bVar17) || bVar18) &&
      ((((int)uVar39 < 0x3c || bVar11) || bVar16) || bVar1)) {
    uVar34 = uVar39;
  }
  this = operator_new(0x10);
  *(undefined8 *)this = 0;
  *(undefined8 *)(this + 8) = 0;
  if ((int)uVar41 < 0) {
    free(this);
    cv::Mat::Mat(aMStack_cf0,uVar26,uVar44,0x18);
  }
  else {
    uVar39 = 0;
    uVar30 = 0;
    do {
      if (-1 < iVar48) {
        uVar49 = 0;
        do {
          if ((int)puVar21[uVar39 + uVar49] < 0) {
            findSmallestRadius(this,(int *)puVar21,uVar44,uVar26,(int)uVar49,(int)uVar30);
            uVar19 = ColorizingRadius::getBackgroundColor(this);
            puVar21[uVar39 + uVar49] = uVar19;
          }
          uVar49 = uVar49 + 1;
        } while (uVar29 != uVar49);
      }
      uVar39 = uVar39 + uVar44;
      uVar30 = uVar30 + 1;
    } while (uVar30 != uVar35);
    free(this);
    cv::Mat::Mat(aMStack_cf0,uVar26,uVar44,0x18);
    if (-1 < (int)uVar41) {
      uVar26 = 0;
      uVar30 = 0;
      do {
        if (-1 < iVar48) {
          lVar54 = 0;
          do {
            uVar5 = *(undefined4 *)((long)puVar21 + lVar54 + (ulong)uVar26 * 4);
            *(char *)(local_ce0 + uVar30 * *local_ca8 + lVar54) = (char)((uint)uVar5 >> 0x10);
            *(char *)(local_ce0 + uVar30 * *local_ca8 + lVar54 + 1) = (char)((uint)uVar5 >> 8);
            *(char *)(local_ce0 + uVar30 * *local_ca8 + lVar54 + 2) = (char)uVar5;
            lVar31 = local_ce0 + uVar30 * *local_ca8 + lVar54;
            lVar54 = lVar54 + 4;
            *(undefined1 *)(lVar31 + 3) = 0xff;
          } while (uVar29 << 2 != lVar54);
        }
        uVar30 = uVar30 + 1;
        uVar26 = uVar26 + uVar44;
      } while (uVar30 != uVar35);
    }
  }
  cv::Mat::Mat(aMStack_d50);
  local_da8 = aMStack_cf0;
  local_db0[0] = 0x1010000;
  local_da0 = (byte *)0x0;
  local_dc8[0] = 0x2010000;
  uStack_db8 = 0;
  local_dc0 = aMStack_d50;
  cv::GaussianBlur(ZEXT816(0),0,local_db0,local_dc8,0x900000009,4,0);
  local_dc8[0] = 0x2010000;
  local_da0 = (byte *)0x0;
  local_db0[0] = 0x1010000;
  uStack_db8 = 0;
  local_dc0 = (Mat *)param_5;
  local_da8 = aMStack_d50;
  cv::resize(ZEXT816(0),0,local_db0,local_dc8,
             CONCAT44(**(undefined4 **)(param_4 + 0x40),(*(undefined4 **)(param_4 + 0x40))[1]),2);
  cv::Mat::release();
  cv::Mat::release();
  local_ec0 = *(uint **)(param_5 + 4);
  free(puVar21);
  cv::Mat::Mat((Mat *)local_db0);
  if (param_6 == 1) {
    uStack_db8 = 0;
    local_dc8[0] = 0x1010000;
    local_de0[0] = 0x2010000;
    uStack_dd0 = 0;
    local_dd8 = local_db0;
    local_dc0 = (Mat *)param_4;
    cv::cvtColor((cv *)local_dc8,local_de0,6,0,0);
    local_dc8[0] = 0x1010000;
    uStack_db8 = 0;
    local_de0[0] = 0x2010000;
    uStack_dd0 = 0;
    local_dd8 = local_db0;
    local_dc0 = (Mat *)local_db0;
    cv::medianBlur((_InputArray *)local_dc8,(_OutputArray *)local_de0,3);
    uStack_db8 = 0;
    local_dc8[0] = 0x1010000;
    local_de0[0] = 0x2010000;
    uStack_dd0 = 0;
    local_dd8 = local_db0;
    local_dc0 = (Mat *)local_db0;
    cv::Laplacian((_InputArray *)local_dc8,(_OutputArray *)local_de0,0,3,1.0,0.0,4);
    pbVar33 = local_da0;
    if (uVar34 != 0xff) goto LAB_00111164;
LAB_00110eb4:
    if ((uVar37 != 0xff) || (uVar24 != 0xff)) goto LAB_00111164;
    if (0 < (int)uVar46) {
      do {
        uVar46 = *local_ec0;
        uVar41 = *local_ed0;
        uVar28 = (ulong)(uVar46 >> 8) & 0xff;
        uVar27 = (ulong)(uVar46 >> 0x10) & 0xff;
        uVar44 = (uVar41 & 0xff) - (int)((ulong)uVar46 & 0xff);
        uVar26 = (uVar41 >> 8 & 0xff) - (int)uVar28;
        uVar41 = (uVar41 >> 0x10 & 0xff) - (int)uVar27;
        if (param_6 == 1) {
          if (*pbVar33 == 0) {
            if ((int)uVar44 < 0) {
              iVar52 = -*(int *)((long)__ptr_00 + (ulong)-uVar44 * 4);
              if (-1 < (int)uVar26) goto LAB_00110fe0;
LAB_00111140:
              iVar50 = -*(int *)((long)__ptr_00 + (ulong)-uVar26 * 4);
              if (-1 < (int)uVar41) goto LAB_00110fe8;
LAB_00111150:
              iVar48 = -*(int *)((long)__ptr_00 + (ulong)-uVar41 * 4);
            }
            else {
              iVar52 = *(int *)((long)__ptr_00 + (ulong)uVar44 * 4);
              if ((int)uVar26 < 0) goto LAB_00111140;
LAB_00110fe0:
              iVar50 = *(int *)((long)__ptr_00 + (ulong)uVar26 * 4);
              if ((int)uVar41 < 0) goto LAB_00111150;
LAB_00110fe8:
              iVar48 = *(int *)((long)__ptr_00 + (ulong)uVar41 * 4);
            }
          }
          else {
            fVar69 = (float)*pbVar33 / 50.0;
            fVar68 = 1.0;
            if (fVar69 <= 1.0) {
              fVar68 = fVar69;
            }
            if ((int)uVar44 < 0) {
              iVar52 = -*(int *)((long)__ptr_00 + (ulong)-uVar44 * 4);
              if (-1 < (int)uVar26) goto LAB_00110f84;
LAB_00111064:
              iVar50 = -*(int *)((long)__ptr_00 + (ulong)-uVar26 * 4);
              if (-1 < (int)uVar41) goto LAB_00110f8c;
LAB_00111074:
              iVar48 = -*(int *)((long)__ptr_00 + (ulong)-uVar41 * 4);
              if (-1 < (int)uVar44) goto LAB_00110f94;
LAB_00111084:
              iVar40 = -*(int *)((long)__ptr + (ulong)-uVar44 * 4);
              if (-1 < (int)uVar26) goto LAB_00110f9c;
LAB_00111094:
              iVar38 = -*(int *)((long)__ptr + (ulong)-uVar26 * 4);
              if (-1 < (int)uVar41) goto LAB_00110fa4;
LAB_001110a4:
              iVar25 = -*(int *)((long)__ptr + (ulong)-uVar41 * 4);
            }
            else {
              iVar52 = *(int *)((long)__ptr_00 + (ulong)uVar44 * 4);
              if ((int)uVar26 < 0) goto LAB_00111064;
LAB_00110f84:
              iVar50 = *(int *)((long)__ptr_00 + (ulong)uVar26 * 4);
              if ((int)uVar41 < 0) goto LAB_00111074;
LAB_00110f8c:
              iVar48 = *(int *)((long)__ptr_00 + (ulong)uVar41 * 4);
              if ((int)uVar44 < 0) goto LAB_00111084;
LAB_00110f94:
              iVar40 = *(int *)((long)__ptr + (ulong)uVar44 * 4);
              if ((int)uVar26 < 0) goto LAB_00111094;
LAB_00110f9c:
              iVar38 = *(int *)((long)__ptr + (ulong)uVar26 * 4);
              if ((int)uVar41 < 0) goto LAB_001110a4;
LAB_00110fa4:
              iVar25 = *(int *)((long)__ptr + (ulong)uVar41 * 4);
            }
            fVar69 = (float)iVar52 - (float)(iVar52 - iVar40) * fVar68;
            if (iVar40 - iVar52 != 0 && iVar52 <= iVar40) {
              fVar69 = (float)iVar52 + (float)(iVar40 - iVar52) * fVar68;
            }
            iVar52 = (int)fVar69;
            fVar69 = (float)iVar50 - (float)(iVar50 - iVar38) * fVar68;
            if (iVar38 - iVar50 != 0 && iVar50 <= iVar38) {
              fVar69 = (float)iVar50 + (float)(iVar38 - iVar50) * fVar68;
            }
            iVar50 = (int)fVar69;
            if (iVar25 - iVar48 == 0 || iVar25 < iVar48) {
              iVar48 = (int)((float)iVar48 - (float)(iVar48 - iVar25) * fVar68);
              goto joined_r0x00110fc4;
            }
            iVar48 = (int)((float)iVar48 + (float)(iVar25 - iVar48) * fVar68);
          }
joined_r0x0011115c:
          if (iVar52 < 0) goto LAB_00111024;
LAB_00110fc8:
          uVar46 = 0xff;
          if (-1 < iVar50) goto LAB_00111038;
LAB_00110ee0:
          bVar6 = *(byte *)((long)__ptr_02 + uVar28 + (ulong)(uint)-iVar50 * 0x100);
          uVar44 = (uint)bVar6;
          uVar41 = (uint)bVar6;
          if (iVar48 < 0) goto LAB_00111040;
LAB_00110ef4:
          uVar44 = uVar41;
          uVar41 = 0xff;
        }
        else {
          if ((int)uVar44 < 0) {
            iVar52 = -*(int *)((long)__ptr + (ulong)-uVar44 * 4);
            if (-1 < (int)uVar26) goto LAB_00110fb8;
LAB_00111004:
            iVar50 = -*(int *)((long)__ptr + (ulong)-uVar26 * 4);
          }
          else {
            iVar52 = *(int *)((long)__ptr + (ulong)uVar44 * 4);
            if ((int)uVar26 < 0) goto LAB_00111004;
LAB_00110fb8:
            iVar50 = *(int *)((long)__ptr + (ulong)uVar26 * 4);
          }
          if ((int)uVar41 < 0) {
            iVar48 = -*(int *)((long)__ptr + (ulong)-uVar41 * 4);
            goto joined_r0x0011115c;
          }
          iVar48 = *(int *)((long)__ptr + (ulong)uVar41 * 4);
joined_r0x00110fc4:
          if (-1 < iVar52) goto LAB_00110fc8;
LAB_00111024:
          uVar46 = (uint)*(byte *)((long)__ptr_02 +
                                  ((ulong)uVar46 & 0xff) + (ulong)(uint)-iVar52 * 0x100);
          if (iVar50 < 0) goto LAB_00110ee0;
LAB_00111038:
          uVar44 = 0xff;
          uVar41 = uVar44;
          if (-1 < iVar48) goto LAB_00110ef4;
LAB_00111040:
          uVar41 = (uint)*(byte *)((long)__ptr_02 + uVar27 + (ulong)(uint)-iVar48 * 0x100);
        }
        uVar20 = uVar20 - 1;
        pbVar33 = pbVar33 + 1;
        *local_ec0 = uVar44 << 8 | uVar41 << 0x10 | uVar46 | 0xff000000;
        local_ed0 = local_ed0 + 1;
        local_ec0 = local_ec0 + 1;
      } while (uVar20 != 0);
    }
  }
  else {
    pbVar33 = (byte *)0x0;
    if (uVar34 == 0xff) goto LAB_00110eb4;
LAB_00111164:
    if (-1 < (int)uVar41) {
      local_eb0 = 0;
      lVar54 = uVar27 + uVar27 * lVar4;
      local_ec0 = local_ec0 + lVar54;
      local_ed0 = local_ed0 + lVar54;
      do {
        if (-1 < iVar48) {
          lVar54 = 0;
          uVar20 = 0;
          puVar22 = local_ed0;
          puVar21 = local_ec0;
          uVar30 = uVar27;
          puVar36 = local_ec0;
          puVar42 = local_ed0;
          do {
            do {
              puVar23 = puVar22;
              puVar55 = puVar21;
              uVar49 = uVar27;
              if ((long)(uVar30 + local_eb0 * uVar28) < (long)iVar52) {
                do {
                  if ((long)(lVar54 + uVar49) < lVar4) {
                    uVar41 = *puVar55;
                    uVar44 = *puVar23;
                    uVar46 = uVar41 & 0xff;
                    uVar26 = (uVar44 & 0xff) - uVar46;
                    if ((int)uVar26 < 0) {
                      uVar39 = -*(int *)((long)pvVar3 + (ulong)-uVar26 * 4);
                    }
                    else {
                      uVar39 = *(uint *)((long)pvVar3 + (ulong)uVar26 * 4);
                    }
                    uVar7 = uVar41 >> 8 & 0xff;
                    uVar19 = (uVar44 >> 8 & 0xff) - uVar7;
                    if ((int)uVar19 < 0) {
                      uVar53 = -*(int *)((long)pvVar3 + (ulong)-uVar19 * 4);
                    }
                    else {
                      uVar53 = *(uint *)((long)pvVar3 + (ulong)uVar19 * 4);
                    }
                    uVar8 = uVar41 >> 0x10 & 0xff;
                    uVar41 = (uVar44 >> 0x10 & 0xff) - uVar8;
                    if ((int)uVar41 < 0) {
                      uVar44 = -*(int *)((long)pvVar3 + (ulong)-uVar41 * 4);
                    }
                    else {
                      uVar44 = *(uint *)((long)pvVar3 + (ulong)uVar41 * 4);
                    }
                    uVar2 = uVar44;
                    uVar12 = uVar39;
                    uVar14 = uVar53;
                    if (param_6 != 2) {
                      uVar2 = uVar41;
                      uVar12 = uVar26;
                      uVar14 = uVar19;
                    }
                    iVar50 = 2000;
                    uVar41 = uVar34;
                    if (uVar39 != 0) {
                      uVar41 = 0;
                      if ((uVar46 ^ 0xff) != 0) {
                        uVar41 = (uVar12 * 2000) / (uVar46 ^ 0xff);
                      }
                      iVar40 = 0;
                      if (uVar46 != 0) {
                        iVar40 = (int)(uVar12 * -2000) / (int)uVar46;
                      }
                      if (1999 < (int)uVar41) {
                        uVar41 = 2000;
                      }
                      if (1999 < iVar40) {
                        iVar40 = 2000;
                      }
                      bVar18 = (uVar12 & 0x80000000) != 0;
                      uVar46 = 0xff - uVar34;
                      uVar47 = (ulong)uVar41;
                      if (bVar18) {
                        uVar46 = uVar34;
                        uVar47 = (long)iVar40;
                      }
                      iVar40 = iVar50;
                      if (bVar18) {
                        iVar40 = -2000;
                      }
                      iVar38 = 0;
                      if (iVar40 != 0) {
                        iVar38 = (int)(__ptr_01[uVar47] * uVar46) / iVar40;
                      }
                      uVar41 = iVar38 + uVar34;
                    }
                    if (0xfe < (int)uVar41) {
                      uVar41 = 0xff;
                    }
                    uVar46 = uVar37;
                    if (uVar53 != 0) {
                      uVar46 = 0;
                      if ((uVar7 ^ 0xff) != 0) {
                        uVar46 = (uVar14 * 2000) / (uVar7 ^ 0xff);
                      }
                      iVar40 = 0;
                      if (uVar7 != 0) {
                        iVar40 = (int)(uVar14 * -2000) / (int)uVar7;
                      }
                      if (1999 < (int)uVar46) {
                        uVar46 = 2000;
                      }
                      if (1999 < iVar40) {
                        iVar40 = 2000;
                      }
                      bVar18 = (uVar14 & 0x80000000) != 0;
                      uVar26 = 0xff - uVar37;
                      uVar47 = (ulong)uVar46;
                      if (bVar18) {
                        uVar26 = uVar37;
                        uVar47 = (long)iVar40;
                      }
                      iVar40 = iVar50;
                      if (bVar18) {
                        iVar40 = -2000;
                      }
                      iVar38 = 0;
                      if (iVar40 != 0) {
                        iVar38 = (int)(__ptr_01[uVar47] * uVar26) / iVar40;
                      }
                      uVar46 = iVar38 + uVar37;
                    }
                    if (0xfe < (int)uVar46) {
                      uVar46 = 0xff;
                    }
                    uVar26 = uVar24;
                    if (uVar44 != 0) {
                      uVar44 = 0;
                      if ((uVar8 ^ 0xff) != 0) {
                        uVar44 = (uVar2 * 2000) / (uVar8 ^ 0xff);
                      }
                      iVar40 = 0;
                      if (uVar8 != 0) {
                        iVar40 = (int)(uVar2 * -2000) / (int)uVar8;
                      }
                      if (1999 < (int)uVar44) {
                        uVar44 = 2000;
                      }
                      if (1999 < iVar40) {
                        iVar40 = 2000;
                      }
                      bVar18 = (uVar2 & 0x80000000) != 0;
                      uVar26 = 0xff - uVar24;
                      uVar47 = (ulong)uVar44;
                      if (bVar18) {
                        uVar26 = uVar24;
                        uVar47 = (long)iVar40;
                      }
                      if (bVar18) {
                        iVar50 = -2000;
                      }
                      iVar40 = 0;
                      if (iVar50 != 0) {
                        iVar40 = (int)(__ptr_01[uVar47] * uVar26) / iVar50;
                      }
                      uVar26 = iVar40 + uVar24;
                    }
                    if (0xfe < (int)uVar26) {
                      uVar26 = 0xff;
                    }
                    *puVar55 = uVar41 & ((int)uVar41 >> 0x1f ^ 0xffffffffU) |
                               (uVar46 & ((int)uVar46 >> 0x1f ^ 0xffffffffU)) << 8 |
                               (uVar26 & ((int)uVar26 >> 0x1f ^ 0xffffffffU)) << 0x10 | 0xff000000;
                  }
                  bVar18 = 0 < (long)uVar49;
                  puVar23 = puVar23 + -1;
                  puVar55 = puVar55 + -1;
                  uVar49 = uVar49 - 1;
                } while (bVar18);
              }
              bVar18 = 0 < (long)uVar30;
              puVar22 = puVar22 + -lVar4;
              puVar21 = puVar21 + -lVar4;
              uVar30 = uVar30 - 1;
            } while (bVar18);
            uVar20 = uVar20 + 1;
            puVar21 = puVar36 + uVar27 + 1;
            puVar22 = puVar42 + uVar27 + 1;
            lVar54 = lVar54 + uVar28;
            uVar30 = uVar27;
            puVar36 = puVar21;
            puVar42 = puVar22;
          } while (uVar20 != uVar29);
        }
        local_eb0 = local_eb0 + 1;
        local_ec0 = local_ec0 + uVar28 * lVar4;
        local_ed0 = local_ed0 + uVar28 * lVar4;
      } while (local_eb0 != uVar35);
    }
  }
  cv::Mat::release();
  if (param_7 == 2) {
    local_dc8[0] = 0x1010000;
    uStack_db8 = 0;
    local_de0[0] = 0x2010000;
    uStack_dd0 = 0;
    local_dd8 = param_5;
    local_dc0 = (Mat *)param_5;
    cv::cvtColor((cv *)local_dc8,local_de0,6,0,0);
  }
  uVar24 = uVar37 << 8 | uVar34 << 0x10 | uVar24;
  free(__ptr);
  free(__ptr_00);
  free(__ptr_01);
  free(__ptr_02);
  cv::Mat::~Mat((Mat *)local_db0);
  cv::Mat::~Mat(aMStack_d50);
  cv::Mat::~Mat(aMStack_cf0);
LAB_00111510:
  if (*(long *)(lVar9 + 0x28) != local_90) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return uVar24;
}

