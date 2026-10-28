
/* void std::__ndk1::vector<cv::Point_<int>, std::__ndk1::allocator<cv::Point_<int> >
   >::__assign_with_size[abi:ne190000]<cv::Point_<int>*, cv::Point_<int>*>(cv::Point_<int>*,
   cv::Point_<int>*, long) */

void __thiscall
std::__ndk1::vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>>::
__assign_with_size_abi_ne190000_<cv::Point_<int>*,cv::Point_<int>*>
          (vector<cv::Point_<int>,std::__ndk1::allocator<cv::Point_<int>>> *this,Point_ *param_1,
          Point_ *param_2,long param_3)

{
  void *__src;
  long lVar1;
  contour_info *pcVar2;
  contour_info *pcVar3;
  contour_info *pcVar4;
  undefined8 *puVar5;
  long *plVar6;
  void *__src_00;
  Point_ *pPVar7;
  size_t sVar8;
  ulong uVar9;
  ulong uVar10;
  ulong uVar11;
  void *pvVar12;
  long lVar13;
  contour_info *pcVar14;
  undefined1 auVar15 [16];
  void *pvStack_f0;
  contour_info *pcStack_e8;
  contour_info *pcStack_e0;
  void *pvStack_d8;
  long *plStack_d0;
  long lStack_c8;
  undefined1 *puStack_c0;
  code *pcStack_b8;
  void *pvStack_b0;
  undefined1 *puStack_80;
  code *pcStack_78;
  Point_ *pPStack_70;
  
  uVar9 = *(ulong *)(this + 0x10);
  pvVar12 = *(void **)this;
  if ((ulong)param_3 <= (ulong)((long)(uVar9 - (long)pvVar12) >> 3)) {
    sVar8 = (long)*(void **)(this + 8) - (long)pvVar12;
    pPVar7 = param_1;
    if (((ulong)((long)sVar8 >> 3) < (ulong)param_3) &&
       (pPVar7 = param_1 + sVar8, *(void **)(this + 8) != pvVar12)) {
      memmove(pvVar12,param_1,sVar8);
      pvVar12 = *(void **)(this + 8);
    }
    sVar8 = (long)param_2 - (long)pPVar7;
    if (sVar8 != 0) {
      memmove(pvVar12,pPVar7,sVar8);
    }
LAB_00118794:
    *(size_t *)(this + 8) = (long)pvVar12 + sVar8;
    return;
  }
  pPVar7 = param_2;
  if (pvVar12 != (void *)0x0) {
    *(void **)(this + 8) = pvVar12;
    operator_delete(pvVar12);
    uVar9 = 0;
    *(undefined8 *)this = 0;
    *(undefined8 *)(this + 8) = 0;
    *(undefined8 *)(this + 0x10) = 0;
  }
  if ((ulong)param_3 >> 0x3d == 0) {
    uVar11 = (long)uVar9 >> 2;
    if ((ulong)((long)uVar9 >> 2) <= (ulong)param_3) {
      uVar11 = param_3;
    }
    if (0x7ffffffffffffff7 < uVar9) {
      uVar11 = 0x1fffffffffffffff;
    }
    if (uVar11 >> 0x3d == 0) {
      pvVar12 = operator_new(uVar11 * 8);
      sVar8 = (long)param_2 - (long)param_1;
      *(void **)this = pvVar12;
      *(void **)(this + 8) = pvVar12;
      *(void **)(this + 0x10) = (void *)((long)pvVar12 + uVar11 * 8);
      if (sVar8 != 0) {
        memcpy(pvVar12,param_1,sVar8);
      }
      goto LAB_00118794;
    }
  }
  auVar15 = FUN_00117b34(this);
  __src_00 = auVar15._8_8_;
  puVar5 = auVar15._0_8_;
  pcStack_78 = vector<float,std::__ndk1::allocator<float>>::
               __assign_with_size_abi_ne190000_<float*,float*>;
  uVar9 = puVar5[2];
  pvVar12 = (void *)*puVar5;
  pPStack_70 = param_1;
  if ((ulong)param_3 <= (ulong)((long)(uVar9 - (long)pvVar12) >> 2)) {
    sVar8 = (long)puVar5[1] - (long)pvVar12;
    __src = __src_00;
    puStack_80 = &stack0xffffffffffffffc0;
    if (((ulong)((long)sVar8 >> 2) < (ulong)param_3) &&
       (__src = (void *)((long)__src_00 + sVar8), puStack_80 = &stack0xffffffffffffffc0,
       (void *)puVar5[1] != pvVar12)) {
      puStack_80 = &stack0xffffffffffffffc0;
      memmove(pvVar12,__src_00,sVar8);
      pvVar12 = (void *)puVar5[1];
    }
    sVar8 = (long)pPVar7 - (long)__src;
    if (sVar8 != 0) {
      memmove(pvVar12,__src,sVar8);
    }
LAB_001188c4:
    puVar5[1] = (long)pvVar12 + sVar8;
    return;
  }
  puStack_80 = &stack0xffffffffffffffc0;
  if (pvVar12 != (void *)0x0) {
    puVar5[1] = pvVar12;
    puStack_80 = &stack0xffffffffffffffc0;
    operator_delete(pvVar12);
    uVar9 = 0;
    *puVar5 = 0;
    puVar5[1] = 0;
    puVar5[2] = 0;
  }
  if ((ulong)param_3 >> 0x3e == 0) {
    uVar11 = (long)uVar9 >> 1;
    if ((ulong)((long)uVar9 >> 1) <= (ulong)param_3) {
      uVar11 = param_3;
    }
    if (0x7ffffffffffffffb < uVar9) {
      uVar11 = 0x3fffffffffffffff;
    }
    if (uVar11 >> 0x3e == 0) {
      pvVar12 = operator_new(uVar11 * 4);
      sVar8 = (long)pPVar7 - (long)__src_00;
      *puVar5 = pvVar12;
      puVar5[1] = pvVar12;
      puVar5[2] = (void *)((long)pvVar12 + uVar11 * 4);
      if (sVar8 != 0) {
        memcpy(pvVar12,__src_00,sVar8);
      }
      goto LAB_001188c4;
    }
  }
  auVar15 = FUN_00118674(puVar5);
  plVar6 = auVar15._0_8_;
  pcStack_b8 = vector<contour_info,std::__ndk1::allocator<contour_info>>::
               __push_back_slow_path<contour_info_const&>;
  lVar1 = tpidr_el0;
  lStack_c8 = *(long *)(lVar1 + 0x28);
  lVar13 = plVar6[1] - *plVar6 >> 6;
  uVar9 = lVar13 + 1;
  pvStack_b0 = __src_00;
  if (uVar9 >> 0x3a == 0) {
    plStack_d0 = plVar6 + 2;
    uVar10 = *plStack_d0 - *plVar6;
    uVar11 = (long)uVar10 >> 5;
    if (uVar11 <= uVar9) {
      uVar11 = uVar9;
    }
    if (0x7fffffffffffffbf < uVar10) {
      uVar11 = 0x3ffffffffffffff;
    }
    if (uVar11 == 0) {
      pvStack_f0 = (void *)0x0;
      puStack_c0 = (undefined1 *)&puStack_80;
    }
    else {
      puStack_c0 = (undefined1 *)&puStack_80;
      if (uVar11 >> 0x3a != 0) goto LAB_00118a3c;
      puStack_c0 = (undefined1 *)&puStack_80;
      pvStack_f0 = operator_new(uVar11 << 6);
    }
    pcStack_e8 = (contour_info *)((long)pvStack_f0 + lVar13 * 0x40);
    pvStack_d8 = (void *)((long)pvStack_f0 + uVar11 * 0x40);
    pcStack_e0 = pcStack_e8;
    contour_info::contour_info(pcStack_e8,auVar15._8_8_);
    pcStack_e0 = pcStack_e0 + 0x40;
    FUN_00118b88(plVar6,&pvStack_f0);
    pcVar2 = pcStack_e8;
    while (pcVar3 = pcStack_e0, pcStack_e0 != pcVar2) {
      pvVar12 = *(void **)(pcStack_e0 + -0x28);
      pcVar14 = pcStack_e0 + -0x40;
      pcVar4 = pcVar14;
      if (pvVar12 != (void *)0x0) {
        *(void **)(pcStack_e0 + -0x20) = pvVar12;
        pcStack_e0 = pcVar14;
        operator_delete(pvVar12);
        pcVar4 = pcStack_e0;
      }
      pcStack_e0 = pcVar4;
      pvVar12 = *(void **)pcVar14;
      if (pvVar12 != (void *)0x0) {
        *(void **)(pcVar3 + -0x38) = pvVar12;
        operator_delete(pvVar12);
      }
    }
    if (pvStack_f0 != (void *)0x0) {
      operator_delete(pvStack_f0);
    }
    if (*(long *)(lVar1 + 0x28) == lStack_c8) {
      return;
    }
  }
  else {
    puStack_c0 = (undefined1 *)&puStack_80;
    if (*(long *)(lVar1 + 0x28) != lStack_c8) goto LAB_00118a74;
    puStack_c0 = (undefined1 *)&puStack_80;
    FUN_00118d20(plVar6);
LAB_00118a3c:
    if (*(long *)(lVar1 + 0x28) == lStack_c8) {
                    /* WARNING: Subroutine does not return */
      FUN_0010b4cc();
    }
  }
LAB_00118a74:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

