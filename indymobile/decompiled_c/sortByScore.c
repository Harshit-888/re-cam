
/* sortByScore(cv::Size_<int>, std::__ndk1::vector<contour_info,
   std::__ndk1::allocator<contour_info> >&) */

void sortByScore(undefined8 param_1,long *param_2)

{
  long lVar1;
  long lVar2;
  float *__dest;
  undefined8 uVar3;
  long lVar4;
  ulong uVar5;
  long lVar6;
  ulong uVar7;
  float fVar8;
  void *__src;
  float fVar9;
  float fVar10;
  float fVar11;
  float fVar12;
  float fVar13;
  void *local_80;
  void *local_78;
  undefined8 local_70;
  long local_68;
  
  lVar1 = tpidr_el0;
  local_68 = *(long *)(lVar1 + 0x28);
  lVar4 = param_2[1];
  lVar2 = *param_2;
  if (lVar4 == lVar2) {
    uVar5 = 0;
    lVar2 = lVar4;
  }
  else {
    lVar6 = 0;
    uVar7 = 0;
    do {
      local_80 = (void *)0x0;
      local_78 = (void *)0x0;
      lVar2 = lVar2 + lVar6;
      local_70 = 0;
      __src = *(void **)(lVar2 + 0x18);
      uVar5 = *(long *)(lVar2 + 0x20) - (long)__src;
      if ((long)uVar5 < 0) {
        if (*(long *)(lVar1 + 0x28) == local_68) {
          uVar3 = FUN_00118674(&local_80);
          if (local_80 != (void *)0x0) {
            local_78 = local_80;
            operator_delete(local_80);
          }
          if (*(long *)(lVar1 + 0x28) == local_68) {
                    /* WARNING: Subroutine does not return */
            FUN_0011cac8(uVar3);
          }
        }
        goto LAB_00115880;
      }
      __dest = operator_new(uVar5);
      memcpy(__dest,__src,uVar5);
      fVar12 = ABS(*__dest - __dest[1]);
      fVar13 = ABS(__dest[2] - __dest[3]);
      fVar8 = ABS(*__dest - __dest[3]);
      fVar9 = ABS(__dest[1] - __dest[2]);
      fVar10 = fVar13;
      if (fVar12 <= fVar13) {
        fVar10 = fVar12;
      }
      fVar11 = fVar9;
      if (fVar8 <= fVar9) {
        fVar11 = fVar8;
      }
      fVar12 = fVar12 + fVar13;
      if (fVar11 <= fVar10) {
        fVar12 = fVar9 + fVar8;
      }
      *(int *)(lVar2 + 0x38) = (int)(((120.0 - fVar12) * 100.0) / 120.0);
      operator_delete(__dest);
      lVar2 = *param_2;
      lVar4 = param_2[1];
      uVar7 = uVar7 + 1;
      lVar6 = lVar6 + 0x40;
      uVar5 = lVar4 - lVar2 >> 6;
    } while (uVar7 < uVar5);
  }
  lVar6 = 0;
  if (lVar4 != lVar2) {
    lVar6 = LZCOUNT(uVar5) * -2 + 0x7e;
  }
  std::__ndk1::__introsort<std::__ndk1::_ClassicAlgPolicy,contour_score_sorter&,contour_info*,false>
            (lVar2,lVar4,&local_80,lVar6,1);
  if (*(long *)(lVar1 + 0x28) == local_68) {
    return;
  }
LAB_00115880:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

