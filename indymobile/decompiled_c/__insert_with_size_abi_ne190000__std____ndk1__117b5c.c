
/* std::__ndk1::__wrap_iter<cv::Vec<float, 2>*> std::__ndk1::vector<cv::Vec<float, 2>,
   std::__ndk1::allocator<cv::Vec<float, 2> >
   >::__insert_with_size[abi:ne190000]<std::__ndk1::__wrap_iter<cv::Vec<float, 2>*>,
   std::__ndk1::__wrap_iter<cv::Vec<float, 2>*> >(std::__ndk1::__wrap_iter<cv::Vec<float, 2>
   const*>, std::__ndk1::__wrap_iter<cv::Vec<float, 2>*>, std::__ndk1::__wrap_iter<cv::Vec<float,
   2>*>, long) */

undefined8 * __thiscall
std::__ndk1::vector<cv::Vec<float,2>,std::__ndk1::allocator<cv::Vec<float,2>>>::
__insert_with_size_abi_ne190000_<std::__ndk1::__wrap_iter<cv::Vec<float,2>*>,std::__ndk1::__wrap_iter<cv::Vec<float,2>*>>
          (vector<cv::Vec<float,2>,std::__ndk1::allocator<cv::Vec<float,2>>> *this,
          undefined8 *param_2,undefined8 *param_3,undefined8 *param_4,long param_5)

{
  undefined4 *puVar1;
  long lVar2;
  undefined4 *puVar3;
  undefined8 *puVar4;
  ulong uVar5;
  undefined8 *puVar6;
  long lVar7;
  undefined8 *puVar8;
  undefined8 *puVar9;
  ulong uVar10;
  undefined8 *puVar11;
  undefined4 *puVar12;
  ulong uVar13;
  undefined8 *puVar14;
  ulong uVar15;
  undefined8 *puVar16;
  undefined8 *puVar17;
  undefined8 uVar18;
  undefined8 uVar19;
  undefined8 uVar20;
  undefined8 uVar21;
  undefined8 uVar22;
  undefined8 uVar23;
  undefined8 uVar24;
  
  if (param_5 < 1) {
    return param_2;
  }
  puVar8 = *(undefined8 **)(this + 8);
  if (param_5 <= *(long *)(this + 0x10) - (long)puVar8 >> 3) {
    lVar7 = (long)puVar8 - (long)param_2 >> 3;
    puVar17 = puVar8;
    if (lVar7 < param_5) {
      puVar6 = (undefined8 *)((long)param_3 + ((long)puVar8 - (long)param_2));
      if (puVar6 != param_4) {
        uVar13 = ((((long)param_2 + (long)param_4) - (long)param_3) - (long)puVar8) - 8;
        puVar9 = puVar8;
        puVar11 = puVar6;
        if ((0x77 < uVar13) &&
           (((undefined8 *)
             ((long)param_3 + (long)puVar8 + ((uVar13 & 0xfffffffffffffff8) - (long)param_2) + 8) <=
             puVar8 || ((undefined8 *)((long)puVar8 + (uVar13 & 0xfffffffffffffff8) + 8) <= puVar6))
           )) {
          uVar13 = (uVar13 >> 3) + 1;
          uVar5 = uVar13 & 0x3ffffffffffffff8;
          puVar17 = puVar8 + 4;
          puVar9 = (undefined8 *)((long)param_3 + ((long)(puVar8 + 4) - (long)param_2));
          uVar10 = uVar5;
          do {
            uVar18 = puVar9[-4];
            uVar20 = puVar9[-1];
            uVar19 = puVar9[-2];
            uVar10 = uVar10 - 8;
            uVar24 = puVar9[1];
            uVar23 = *puVar9;
            uVar22 = puVar9[3];
            uVar21 = puVar9[2];
            puVar17[-3] = puVar9[-3];
            puVar17[-4] = uVar18;
            puVar17[-1] = uVar20;
            puVar17[-2] = uVar19;
            puVar17[1] = uVar24;
            *puVar17 = uVar23;
            puVar17[3] = uVar22;
            puVar17[2] = uVar21;
            puVar17 = puVar17 + 8;
            puVar9 = puVar9 + 8;
          } while (uVar10 != 0);
          puVar17 = puVar8 + uVar5;
          puVar9 = puVar8 + uVar5;
          puVar11 = puVar6 + uVar5;
          if (uVar13 == uVar5) goto LAB_00117f24;
        }
        do {
          *(undefined4 *)puVar9 = *(undefined4 *)puVar11;
          puVar4 = puVar11 + 1;
          *(undefined4 *)((long)puVar9 + 4) = *(undefined4 *)((long)puVar11 + 4);
          puVar17 = puVar9 + 1;
          puVar9 = puVar9 + 1;
          puVar11 = puVar4;
        } while (puVar4 != param_4);
      }
LAB_00117f24:
      *(undefined8 **)(this + 8) = puVar17;
      if (lVar7 < 1) {
        return param_2;
      }
    }
    else {
      puVar6 = param_3 + param_5;
    }
    puVar11 = puVar17 + -param_5;
    puVar9 = puVar17;
    if (puVar11 < puVar8) {
      puVar4 = puVar11 + 1;
      lVar7 = param_5 * 8 + ~(ulong)puVar17;
      puVar14 = puVar8;
      if (puVar8 <= puVar4) {
        puVar14 = puVar4;
      }
      uVar13 = (long)puVar14 + lVar7;
      puVar14 = puVar11;
      if (0x97 < uVar13) {
        puVar16 = puVar8;
        if (puVar8 <= puVar4) {
          puVar16 = puVar4;
        }
        uVar10 = (long)puVar16 + lVar7 & 0xfffffffffffffff8;
        if (((undefined8 *)((long)puVar17 + param_5 * -8 + uVar10 + 8) <= puVar17) ||
           ((undefined8 *)((long)puVar17 + uVar10 + 8) <= puVar11)) {
          uVar13 = (uVar13 >> 3) + 1;
          uVar5 = uVar13 & 0x3ffffffffffffff8;
          puVar14 = puVar11 + uVar5;
          puVar9 = puVar17 + uVar5;
          puVar4 = puVar11 + 4;
          uVar10 = uVar5;
          puVar16 = puVar17 + 4;
          do {
            uVar18 = puVar4[-4];
            uVar20 = puVar4[-1];
            uVar19 = puVar4[-2];
            uVar10 = uVar10 - 8;
            uVar24 = puVar4[1];
            uVar23 = *puVar4;
            uVar22 = puVar4[3];
            uVar21 = puVar4[2];
            puVar16[-3] = puVar4[-3];
            puVar16[-4] = uVar18;
            puVar16[-1] = uVar20;
            puVar16[-2] = uVar19;
            puVar16[1] = uVar24;
            *puVar16 = uVar23;
            puVar16[3] = uVar22;
            puVar16[2] = uVar21;
            puVar4 = puVar4 + 8;
            puVar16 = puVar16 + 8;
          } while (uVar10 != 0);
          if (uVar13 == uVar5) goto LAB_00118010;
        }
      }
      do {
        *(undefined4 *)puVar9 = *(undefined4 *)puVar14;
        puVar3 = (undefined4 *)((long)puVar14 + 4);
        puVar14 = puVar14 + 1;
        *(undefined4 *)((long)puVar9 + 4) = *puVar3;
        puVar9 = puVar9 + 1;
      } while (puVar14 < puVar8);
    }
LAB_00118010:
    *(undefined8 **)(this + 8) = puVar9;
    if (puVar17 != param_2 + param_5) {
      uVar13 = (long)puVar17 + (param_5 * -8 - (long)param_2) + -8;
      if ((0x37 < uVar13) && (0x1f < (ulong)(param_5 * -8))) {
        uVar13 = (uVar13 >> 3) + 1;
        uVar5 = uVar13 & 0x3ffffffffffffffc;
        puVar9 = puVar11 + -2;
        puVar8 = puVar17 + -2;
        uVar10 = uVar5;
        do {
          puVar4 = puVar9 + -1;
          uVar20 = puVar9[-2];
          uVar19 = puVar9[1];
          uVar18 = *puVar9;
          uVar10 = uVar10 - 4;
          puVar9 = puVar9 + -4;
          puVar8[-1] = *puVar4;
          puVar8[-2] = uVar20;
          puVar8[1] = uVar19;
          *puVar8 = uVar18;
          puVar8 = puVar8 + -4;
        } while (uVar10 != 0);
        puVar11 = puVar11 + -uVar5;
        puVar17 = puVar17 + -uVar5;
        if (uVar13 == uVar5) goto LAB_0011805c;
      }
      do {
        puVar11 = puVar11 + -1;
        puVar17[-1] = *puVar11;
        puVar17 = puVar17 + -1;
      } while (puVar11 != param_2);
    }
LAB_0011805c:
    if ((long)puVar6 - (long)param_3 == 0) {
      return param_2;
    }
    uVar13 = ((long)puVar6 - (long)param_3) - 8;
    puVar8 = param_2;
    if ((0x17 < uVar13) && (0x1f < (ulong)((long)param_2 - (long)param_3))) {
      puVar8 = param_3 + 2;
      puVar17 = param_2 + 2;
      uVar13 = (uVar13 >> 3) + 1;
      uVar5 = uVar13 & 0x3ffffffffffffffc;
      uVar10 = uVar5;
      do {
        puVar9 = puVar8 + -1;
        uVar18 = puVar8[-2];
        uVar20 = puVar8[1];
        uVar19 = *puVar8;
        uVar10 = uVar10 - 4;
        puVar8 = puVar8 + 4;
        puVar17[-1] = *puVar9;
        puVar17[-2] = uVar18;
        puVar17[1] = uVar20;
        *puVar17 = uVar19;
        puVar17 = puVar17 + 4;
      } while (uVar10 != 0);
      param_3 = param_3 + uVar5;
      puVar8 = param_2 + uVar5;
      if (uVar13 == uVar5) {
        return param_2;
      }
    }
    do {
      puVar17 = param_3 + 1;
      *puVar8 = *param_3;
      param_3 = puVar17;
      puVar8 = puVar8 + 1;
    } while (puVar17 != puVar6);
    return param_2;
  }
  puVar17 = *(undefined8 **)this;
  uVar13 = param_5 + ((long)puVar8 - (long)puVar17 >> 3);
  if (uVar13 >> 0x3d != 0) {
    FUN_00117b48();
LAB_00118150:
                    /* WARNING: Subroutine does not return */
    FUN_0010b4cc();
  }
  uVar5 = *(long *)(this + 0x10) - (long)puVar17;
  uVar10 = (long)uVar5 >> 2;
  if (uVar10 <= uVar13) {
    uVar10 = uVar13;
  }
  if (0x7ffffffffffffff7 < uVar5) {
    uVar10 = 0x1fffffffffffffff;
  }
  if (uVar10 == 0) {
    puVar3 = (undefined4 *)0x0;
  }
  else {
    if (uVar10 >> 0x3d != 0) goto LAB_00118150;
    puVar3 = operator_new(uVar10 << 3);
  }
  lVar7 = (long)param_2 - (long)puVar17;
  uVar13 = param_5 - 1U & 0x1fffffffffffffff;
  puVar9 = (undefined8 *)(puVar3 + (lVar7 >> 3) * 2);
  puVar11 = puVar9 + param_5;
  puVar6 = puVar9;
  if ((uVar13 < 0xf) ||
     ((puVar9 < param_3 + param_5 && (param_3 < (undefined8 *)((long)puVar3 + lVar7 + param_5 * 8)))
     )) {
LAB_00117ce0:
    do {
      *(undefined4 *)puVar6 = *(undefined4 *)param_3;
      puVar12 = (undefined4 *)((long)param_3 + 4);
      param_3 = param_3 + 1;
      *(undefined4 *)((long)puVar6 + 4) = *puVar12;
      puVar6 = puVar6 + 1;
    } while (puVar6 != puVar11);
  }
  else {
    uVar13 = uVar13 + 1;
    puVar4 = param_3 + 4;
    uVar15 = uVar13 & 0x3ffffffffffffff8;
    puVar6 = puVar9 + uVar15;
    param_3 = param_3 + uVar15;
    uVar5 = uVar15;
    puVar14 = puVar9 + 4;
    do {
      uVar18 = puVar4[-4];
      uVar20 = puVar4[-1];
      uVar19 = puVar4[-2];
      uVar5 = uVar5 - 8;
      uVar24 = puVar4[1];
      uVar23 = *puVar4;
      uVar22 = puVar4[3];
      uVar21 = puVar4[2];
      puVar14[-3] = puVar4[-3];
      puVar14[-4] = uVar18;
      puVar14[-1] = uVar20;
      puVar14[-2] = uVar19;
      puVar14[1] = uVar24;
      *puVar14 = uVar23;
      puVar14[3] = uVar22;
      puVar14[2] = uVar21;
      puVar4 = puVar4 + 8;
      puVar14 = puVar14 + 8;
    } while (uVar5 != 0);
    if (uVar13 != uVar15) goto LAB_00117ce0;
  }
  lVar2 = (long)puVar8 - (long)param_2;
  if (lVar2 != 0) {
    uVar13 = lVar2 - 8;
    puVar6 = puVar11;
    puVar4 = param_2;
    if ((0x97 < uVar13) &&
       (((undefined8 *)((long)param_2 + (uVar13 & 0xfffffffffffffff8) + 8) <= puVar11 ||
        ((undefined8 *)((long)puVar3 + (uVar13 & 0xfffffffffffffff8) + lVar7 + param_5 * 8 + 8) <=
         param_2)))) {
      uVar13 = (uVar13 >> 3) + 1;
      uVar15 = uVar13 & 0x3ffffffffffffff8;
      puVar6 = (undefined8 *)(puVar3 + param_5 * 2 + (lVar7 >> 3) * 2 + 8);
      puVar4 = param_2 + 4;
      uVar5 = uVar15;
      do {
        uVar18 = puVar4[-4];
        uVar20 = puVar4[-1];
        uVar19 = puVar4[-2];
        uVar5 = uVar5 - 8;
        uVar24 = puVar4[1];
        uVar23 = *puVar4;
        uVar22 = puVar4[3];
        uVar21 = puVar4[2];
        puVar6[-3] = puVar4[-3];
        puVar6[-4] = uVar18;
        puVar6[-1] = uVar20;
        puVar6[-2] = uVar19;
        puVar6[1] = uVar24;
        *puVar6 = uVar23;
        puVar6[3] = uVar22;
        puVar6[2] = uVar21;
        puVar6 = puVar6 + 8;
        puVar4 = puVar4 + 8;
      } while (uVar5 != 0);
      puVar6 = puVar11 + uVar15;
      puVar4 = param_2 + uVar15;
      if (uVar13 == uVar15) goto LAB_00117dbc;
    }
    do {
      *(undefined4 *)puVar6 = *(undefined4 *)puVar4;
      puVar14 = puVar4 + 1;
      *(undefined4 *)((long)puVar6 + 4) = *(undefined4 *)((long)puVar4 + 4);
      puVar6 = puVar6 + 1;
      puVar4 = puVar14;
    } while (puVar14 != puVar8);
  }
LAB_00117dbc:
  if (puVar17 != param_2) {
    uVar13 = lVar7 - 8;
    puVar8 = puVar17;
    puVar12 = puVar3;
    if ((0x57 < uVar13) &&
       ((lVar7 = (uVar13 & 0xfffffffffffffff8) + 8, (undefined4 *)((long)puVar17 + lVar7) <= puVar3
        || ((undefined8 *)((long)puVar3 + lVar7) <= puVar17)))) {
      uVar13 = (uVar13 >> 3) + 1;
      uVar15 = uVar13 & 0x3ffffffffffffff8;
      puVar8 = puVar17 + uVar15;
      puVar12 = puVar3 + uVar15 * 2;
      puVar6 = (undefined8 *)(puVar3 + 8);
      puVar4 = puVar17 + 4;
      uVar5 = uVar15;
      do {
        uVar18 = puVar4[-4];
        uVar20 = puVar4[-1];
        uVar19 = puVar4[-2];
        uVar5 = uVar5 - 8;
        uVar24 = puVar4[1];
        uVar23 = *puVar4;
        uVar22 = puVar4[3];
        uVar21 = puVar4[2];
        puVar6[-3] = puVar4[-3];
        puVar6[-4] = uVar18;
        puVar6[-1] = uVar20;
        puVar6[-2] = uVar19;
        puVar6[1] = uVar24;
        *puVar6 = uVar23;
        puVar6[3] = uVar22;
        puVar6[2] = uVar21;
        puVar6 = puVar6 + 8;
        puVar4 = puVar4 + 8;
      } while (uVar5 != 0);
      if (uVar13 == uVar15) goto LAB_00117e6c;
    }
    do {
      *puVar12 = *(undefined4 *)puVar8;
      puVar1 = (undefined4 *)((long)puVar8 + 4);
      puVar8 = puVar8 + 1;
      puVar12[1] = *puVar1;
      puVar12 = puVar12 + 2;
    } while (puVar8 != param_2);
  }
LAB_00117e6c:
  *(undefined4 **)this = puVar3;
  *(long *)(this + 8) = (long)puVar11 + lVar2;
  *(undefined4 **)(this + 0x10) = puVar3 + uVar10 * 2;
  if (puVar17 != (undefined8 *)0x0) {
    operator_delete(puVar17);
  }
  return puVar9;
}

