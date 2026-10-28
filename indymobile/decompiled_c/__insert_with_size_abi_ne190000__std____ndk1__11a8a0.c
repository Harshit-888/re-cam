
/* std::__ndk1::__wrap_iter<cv::Point_<int>*> std::__ndk1::vector<cv::Point_<int>,
   std::__ndk1::allocator<cv::Point_<int> >
   >::__insert_with_size[abi:ne190000]<std::__ndk1::__wrap_iter<cv::Point_<int>*>,
   std::__ndk1::__wrap_iter<cv::Point_<int>*> >(std::__ndk1::__wrap_iter<cv::Point_<int> const*>,
   std::__ndk1::__wrap_iter<cv::Point_<int>*>, std::__ndk1::__wrap_iter<cv::Point_<int>*>, long) */

undefined8 * __thiscall
std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>::
__insert_with_size_abi_ne190000_<std::__ndk1::__wrap_iter<cv::Point_<int>*>,std::__ndk1::__wrap_iter<cv::Point_<int>*>>
          (vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>> *this,undefined8 *param_2
          ,undefined8 *param_3,long param_4,long param_5)

{
  undefined8 *__dest;
  undefined8 *puVar1;
  void *pvVar2;
  size_t sVar3;
  ulong uVar4;
  undefined8 *puVar5;
  undefined8 *puVar6;
  ulong uVar7;
  ulong uVar8;
  undefined8 *puVar9;
  ulong uVar10;
  undefined8 *puVar11;
  undefined8 *puVar12;
  void *__dest_00;
  void *__src;
  undefined8 *puVar13;
  long lVar14;
  undefined8 uVar15;
  undefined8 uVar16;
  undefined8 uVar17;
  
  if (param_5 < 1) {
    return param_2;
  }
  __dest = *(undefined8 **)(this + 8);
  if (param_5 <= *(long *)(this + 0x10) - (long)__dest >> 3) {
    lVar14 = (long)__dest - (long)param_2 >> 3;
    if (lVar14 < param_5) {
      puVar13 = (undefined8 *)((long)param_3 + ((long)__dest - (long)param_2));
      sVar3 = param_4 - (long)puVar13;
      if (sVar3 != 0) {
        memmove(__dest,puVar13,sVar3);
      }
      *(undefined8 **)(this + 8) = (undefined8 *)((long)__dest + sVar3);
      puVar5 = (undefined8 *)((long)__dest + sVar3);
      if (lVar14 < 1) {
        return param_2;
      }
    }
    else {
      puVar13 = param_3 + param_5;
      puVar5 = __dest;
    }
    puVar12 = puVar5 + -param_5;
    puVar6 = puVar5;
    if (puVar12 < __dest) {
      puVar6 = __dest;
      if (__dest <= puVar12 + 1) {
        puVar6 = puVar12 + 1;
      }
      uVar8 = (long)puVar6 + param_5 * 8 + ~(ulong)puVar5;
      puVar9 = puVar5;
      if ((0x37 < uVar8) && (0x1f < (ulong)(param_5 * 8))) {
        puVar9 = puVar5 + 2;
        uVar8 = (uVar8 >> 3) + 1;
        uVar4 = uVar8 & 0x3ffffffffffffffc;
        puVar6 = puVar5 + uVar4;
        puVar11 = puVar12 + 2;
        uVar7 = uVar4;
        do {
          puVar1 = puVar11 + -1;
          uVar15 = puVar11[-2];
          uVar17 = puVar11[1];
          uVar16 = *puVar11;
          uVar7 = uVar7 - 4;
          puVar11 = puVar11 + 4;
          puVar9[-1] = *puVar1;
          puVar9[-2] = uVar15;
          puVar9[1] = uVar17;
          *puVar9 = uVar16;
          puVar9 = puVar9 + 4;
        } while (uVar7 != 0);
        puVar9 = puVar6;
        puVar12 = puVar12 + uVar4;
        if (uVar8 == uVar4) goto LAB_0011ab10;
      }
      do {
        puVar11 = puVar12 + 1;
        puVar6 = puVar9 + 1;
        *puVar9 = *puVar12;
        puVar9 = puVar6;
        puVar12 = puVar11;
      } while (puVar11 < __dest);
    }
LAB_0011ab10:
    *(undefined8 **)(this + 8) = puVar6;
    if (puVar5 != param_2 + param_5) {
      sVar3 = (long)puVar5 - (long)(param_2 + param_5);
      memmove((void *)((long)puVar5 - sVar3),param_2,sVar3);
    }
    if ((long)puVar13 - (long)param_3 == 0) {
      return param_2;
    }
    memmove(param_2,param_3,(long)puVar13 - (long)param_3);
    return param_2;
  }
  lVar14 = *(long *)this;
  uVar8 = param_5 + ((long)__dest - lVar14 >> 3);
  if (uVar8 >> 0x3d != 0) {
    FUN_00117b34(this);
LAB_0011ab64:
                    /* WARNING: Subroutine does not return */
    FUN_0010b4cc();
  }
  uVar4 = *(long *)(this + 0x10) - lVar14;
  uVar7 = (long)uVar4 >> 2;
  if (uVar7 <= uVar8) {
    uVar7 = uVar8;
  }
  if (0x7ffffffffffffff7 < uVar4) {
    uVar7 = 0x1fffffffffffffff;
  }
  if (uVar7 == 0) {
    pvVar2 = (void *)0x0;
  }
  else {
    if (uVar7 >> 0x3d != 0) goto LAB_0011ab64;
    pvVar2 = operator_new(uVar7 << 3);
  }
  uVar8 = param_5 - 1U & 0x1fffffffffffffff;
  puVar13 = (undefined8 *)((long)pvVar2 + ((long)param_2 - lVar14 >> 3) * 8);
  puVar5 = puVar13 + param_5;
  puVar6 = puVar13;
  if ((6 < uVar8) && (0x1f < (ulong)((long)pvVar2 + (((long)param_2 - lVar14) - (long)param_3)))) {
    uVar8 = uVar8 + 1;
    puVar6 = param_3 + 2;
    puVar12 = puVar13 + 2;
    uVar10 = uVar8 & 0x3ffffffffffffffc;
    uVar4 = uVar10;
    do {
      puVar9 = puVar6 + -1;
      uVar15 = puVar6[-2];
      uVar17 = puVar6[1];
      uVar16 = *puVar6;
      uVar4 = uVar4 - 4;
      puVar6 = puVar6 + 4;
      puVar12[-1] = *puVar9;
      puVar12[-2] = uVar15;
      puVar12[1] = uVar17;
      *puVar12 = uVar16;
      puVar12 = puVar12 + 4;
    } while (uVar4 != 0);
    puVar6 = puVar13 + uVar10;
    param_3 = param_3 + uVar10;
    if (uVar8 == uVar10) goto LAB_0011aa50;
  }
  do {
    puVar12 = puVar6 + 1;
    *puVar6 = *param_3;
    puVar6 = puVar12;
    param_3 = param_3 + 1;
  } while (puVar12 != puVar5);
LAB_0011aa50:
  memcpy(puVar5,param_2,(long)__dest - (long)param_2);
  __src = *(void **)this;
  *(undefined8 **)(this + 8) = param_2;
  __dest_00 = (void *)((long)puVar13 - ((long)param_2 - (long)__src));
  memcpy(__dest_00,__src,(long)param_2 - (long)__src);
  *(void **)this = __dest_00;
  *(long *)(this + 8) = (long)puVar5 + ((long)__dest - (long)param_2);
  *(void **)(this + 0x10) = (void *)((long)pvVar2 + uVar7 * 8);
  if (__src != (void *)0x0) {
    operator_delete(__src);
  }
  return puVar13;
}

