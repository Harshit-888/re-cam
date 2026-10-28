
/* void std::__ndk1::__introsort<std::__ndk1::_ClassicAlgPolicy, contour_score_sorter&,
   contour_info*, false>(contour_info*, contour_info*, contour_score_sorter&,
   std::__ndk1::iterator_traits<contour_info*>::difference_type, bool) */

void std::__ndk1::
     __introsort<std::__ndk1::_ClassicAlgPolicy,contour_score_sorter&,contour_info*,false>
               (long param_1,long param_2,undefined8 param_3,long param_4,uint param_5)

{
  bool bVar1;
  long lVar2;
  long lVar3;
  ulong uVar4;
  long lVar5;
  long lVar6;
  ulong uVar7;
  undefined1 auVar8 [16];
  long local_80;
  long local_78;
  long local_70;
  long local_68;
  
  lVar3 = tpidr_el0;
  local_68 = *(long *)(lVar3 + 0x28);
  local_78 = param_2;
  do {
    param_4 = 1 - param_4;
    local_70 = param_1;
LAB_00118df0:
    lVar2 = local_70;
    uVar7 = param_2 - local_70 >> 6;
    if (uVar7 - 2 == 0 || (long)uVar7 < 2) {
      if (uVar7 < 2) goto LAB_001190a8;
      if (uVar7 == 2) {
        local_78 = param_2 + -0x40;
        if (*(int *)(local_70 + 0x38) < *(int *)(param_2 + -8)) {
          FUN_001190dc(&local_70,&local_78);
        }
        goto LAB_001190a8;
      }
    }
    else {
      if (uVar7 == 3) {
        local_78 = param_2 + -0x40;
        FUN_0011953c(local_70,local_70 + 0x40,local_78,param_3);
        goto LAB_001190a8;
      }
      if (uVar7 == 4) {
        local_78 = param_2 + -0x40;
        FUN_00119e3c(local_70,local_70 + 0x40,local_70 + 0x80,local_78,param_3);
        goto LAB_001190a8;
      }
      if (uVar7 == 5) {
        local_78 = param_2 + -0x40;
        FUN_00119f0c(local_70,local_70 + 0x40,local_70 + 0x80,local_70 + 0xc0,local_78,param_3);
        goto LAB_001190a8;
      }
    }
    if ((long)uVar7 < 0x18) {
      if ((param_5 & 1) == 0) {
        FUN_001193d0(local_70,param_2,param_3);
      }
      else {
        FUN_0011921c();
      }
LAB_001190a8:
      if (*(long *)(lVar3 + 0x28) == local_68) {
        return;
      }
                    /* WARNING: Subroutine does not return */
      __stack_chk_fail();
    }
    if (param_4 == 1) {
      if (local_70 != param_2) {
        uVar4 = uVar7 - 2 >> 1;
        lVar6 = local_70 + uVar4 * 0x40;
        lVar5 = uVar4 + 1;
        do {
          FUN_0011a000(lVar2,param_3,uVar7,lVar6);
          lVar5 = lVar5 + -1;
          lVar6 = lVar6 + -0x40;
        } while (lVar5 != 0);
        do {
          FUN_0011a1f4(lVar2,param_2,param_3,uVar7);
          param_2 = param_2 + -0x40;
          bVar1 = 2 < uVar7;
          uVar7 = uVar7 - 1;
        } while (bVar1);
      }
      goto LAB_001190a8;
    }
    lVar2 = local_70 + (uVar7 >> 1) * 0x40;
    if (uVar7 < 0x81) {
      FUN_0011953c(lVar2,local_70,param_2 + -0x40,param_3);
      param_2 = local_78;
    }
    else {
      FUN_0011953c(local_70,lVar2,param_2 + -0x40,param_3);
      lVar2 = local_78;
      lVar5 = (uVar7 >> 1) * 0x40;
      FUN_0011953c(local_70 + 0x40,local_70 + lVar5 + -0x40,local_78 + -0x80,param_3);
      FUN_0011953c(local_70 + 0x80,local_70 + lVar5 + 0x40,lVar2 + -0xc0,param_3);
      FUN_0011953c(local_70 + lVar5 + -0x40,local_70 + lVar5,local_70 + lVar5 + 0x40,param_3);
      local_80 = local_70 + lVar5;
      FUN_00119620(&local_70,&local_80);
      param_2 = local_78;
    }
    local_78 = param_2;
    if (((param_5 & 1) == 0) && (*(int *)(local_70 + -8) <= *(int *)(local_70 + 0x38))) {
      param_1 = FUN_00119760(local_70,param_2,param_3);
      goto LAB_00118f98;
    }
    auVar8 = FUN_00119978(local_70,param_2,param_3);
    param_1 = auVar8._0_8_;
    if ((auVar8._8_8_ & 1) != 0) {
      uVar7 = FUN_00119b9c(local_70,param_1,param_3);
      uVar4 = FUN_00119b9c(param_1 + 0x40,param_2,param_3);
      if ((uVar4 & 1) == 0) {
        if ((uVar7 & 1) == 0) goto LAB_00118f68;
        param_4 = param_4 + 1;
        local_70 = param_1 + 0x40;
      }
      else {
        if ((uVar7 & 1) != 0) goto LAB_001190a8;
        param_4 = param_4 + 1;
        param_2 = param_1;
        local_78 = param_1;
      }
      goto LAB_00118df0;
    }
LAB_00118f68:
    __introsort<std::__ndk1::_ClassicAlgPolicy,contour_score_sorter&,contour_info*,false>
              (local_70,param_1,param_3,-param_4,param_5 & 1);
    param_1 = param_1 + 0x40;
LAB_00118f98:
    param_5 = 0;
    param_4 = -param_4;
  } while( true );
}

