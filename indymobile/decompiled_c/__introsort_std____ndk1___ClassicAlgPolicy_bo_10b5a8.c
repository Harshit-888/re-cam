
/* void std::__ndk1::__introsort<std::__ndk1::_ClassicAlgPolicy, bool (*&)(std::__ndk1::pair<double,
   IOU::Vec2<double> > const&, std::__ndk1::pair<double, IOU::Vec2<double> > const&),
   std::__ndk1::pair<double, IOU::Vec2<double> >*, false>(std::__ndk1::pair<double,
   IOU::Vec2<double> >*, std::__ndk1::pair<double, IOU::Vec2<double> >*, bool
   (*&)(std::__ndk1::pair<double, IOU::Vec2<double> > const&, std::__ndk1::pair<double,
   IOU::Vec2<double> > const&), std::__ndk1::iterator_traits<std::__ndk1::pair<double,
   IOU::Vec2<double> >*>::difference_type, bool) */

void std::__ndk1::
     __introsort<std::__ndk1::_ClassicAlgPolicy,bool(*&)(std::__ndk1::pair<double,IOU::Vec2<double>>const&,std::__ndk1::pair<double,IOU::Vec2<double>>const&),std::__ndk1::pair<double,IOU::Vec2<double>>*,false>
               (undefined8 *param_1,undefined8 *param_2,undefined8 *param_3,long param_4,
               uint param_5)

{
  long lVar1;
  long lVar2;
  undefined8 *puVar3;
  ulong uVar4;
  undefined8 *puVar5;
  ulong uVar6;
  undefined8 *puVar7;
  long lVar8;
  undefined8 *puVar9;
  long lVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  undefined1 auVar14 [16];
  undefined8 local_80;
  undefined8 uStack_78;
  undefined8 local_70;
  long local_68;
  
  lVar1 = tpidr_el0;
  local_68 = *(long *)(lVar1 + 0x28);
LAB_0010b5f0:
  puVar9 = param_2 + -3;
  do {
    param_4 = -param_4;
    puVar5 = param_1;
    do {
      param_1 = puVar5;
      param_4 = param_4 + 1;
      uVar4 = (long)param_2 - (long)param_1;
      uVar6 = ((long)uVar4 >> 3) * -0x5555555555555555;
      if ((long)uVar6 < 3) {
        if (uVar6 < 2) goto LAB_0010baf8;
        if (uVar6 == 2) {
          uVar4 = (*(code *)*param_3)(puVar9,param_1);
          if ((uVar4 & 1) != 0) {
            uVar12 = *param_1;
            *param_1 = param_2[-3];
            param_2[-3] = uVar12;
            uStack_78 = param_1[2];
            local_80 = param_1[1];
            uVar12 = param_2[-2];
            param_1[2] = param_2[-1];
            param_1[1] = uVar12;
            param_2[-1] = uStack_78;
            param_2[-2] = local_80;
          }
          goto LAB_0010baf8;
        }
      }
      else {
        if (uVar6 == 3) {
          if (*(long *)(lVar1 + 0x28) == local_68) {
            FUN_0010bb30(param_1,param_1 + 3,puVar9,param_3);
            return;
          }
          goto LAB_0010bb2c;
        }
        if (uVar6 == 4) {
          FUN_0010bb30(param_1,param_1 + 3,param_1 + 6,param_3);
          uVar4 = (*(code *)*param_3)(puVar9,param_1 + 6);
          if ((uVar4 & 1) != 0) {
            uVar12 = param_1[6];
            param_1[6] = param_2[-3];
            param_2[-3] = uVar12;
            uVar12 = param_2[-2];
            uVar13 = param_1[8];
            uVar11 = param_1[7];
            param_1[8] = param_2[-1];
            param_1[7] = uVar12;
            param_2[-1] = uVar13;
            param_2[-2] = uVar11;
            uVar4 = (*(code *)*param_3)(param_1 + 6,param_1 + 3);
            if ((uVar4 & 1) != 0) {
              uVar13 = param_1[3];
              param_1[3] = param_1[6];
              uVar11 = param_1[5];
              uVar12 = param_1[4];
              param_1[6] = uVar13;
              param_1[5] = param_1[8];
              param_1[4] = param_1[7];
              param_1[8] = uVar11;
              param_1[7] = uVar12;
              uVar4 = (*(code *)*param_3)(param_1 + 3,param_1);
              if ((uVar4 & 1) != 0) {
                uStack_78 = param_1[2];
                local_80 = param_1[1];
                uVar12 = *param_1;
                *param_1 = param_1[3];
                param_1[2] = param_1[5];
                param_1[1] = param_1[4];
                param_1[3] = uVar12;
                param_1[5] = uStack_78;
                param_1[4] = local_80;
              }
            }
          }
          goto LAB_0010baf8;
        }
        if (uVar6 == 5) {
          if (*(long *)(lVar1 + 0x28) == local_68) {
            FUN_0010c260(param_1,param_1 + 3,param_1 + 6,param_1 + 9,puVar9,param_3);
            return;
          }
          goto LAB_0010bb2c;
        }
      }
      if ((long)uVar4 < 0x240) {
        if ((param_5 & 1) == 0) {
          if ((param_1 != param_2) && (param_1 + 3 != param_2)) {
            puVar9 = param_1 + -3;
            puVar5 = param_1 + 3;
            do {
              puVar3 = puVar5;
              uVar4 = (*(code *)*param_3)(puVar3,param_1);
              if ((uVar4 & 1) != 0) {
                local_70 = puVar3[2];
                uStack_78 = puVar3[1];
                local_80 = *puVar3;
                puVar5 = puVar9;
                do {
                  puVar7 = puVar5;
                  puVar7[6] = puVar7[3];
                  puVar7[8] = puVar7[5];
                  puVar7[7] = puVar7[4];
                  uVar4 = (*(code *)*param_3)(&local_80,puVar7);
                  puVar5 = puVar7 + -3;
                } while ((uVar4 & 1) != 0);
                puVar7[3] = local_80;
                puVar7[5] = local_70;
                puVar7[4] = uStack_78;
              }
              puVar9 = puVar9 + 3;
              puVar5 = puVar3 + 3;
              param_1 = puVar3;
            } while (puVar3 + 3 != param_2);
          }
          goto LAB_0010baf8;
        }
        if ((param_1 == param_2) || (param_1 + 3 == param_2)) goto LAB_0010baf8;
        lVar8 = 0;
        puVar9 = param_1 + 3;
        puVar5 = param_1;
        goto LAB_0010b9b0;
      }
      if (param_4 == 1) {
        if (param_1 == param_2) goto LAB_0010baf8;
        if (*(long *)(lVar1 + 0x28) == local_68) {
          FUN_0010c444(param_1,param_2,param_2,param_3);
          return;
        }
        goto LAB_0010bb2c;
      }
      uVar6 = uVar6 >> 1;
      puVar5 = param_1 + uVar6 * 3;
      if (uVar4 < 0xc01) {
        FUN_0010bb30(puVar5,param_1,puVar9,param_3);
      }
      else {
        FUN_0010bb30(param_1,puVar5,puVar9,param_3);
        puVar3 = param_1 + 3;
        FUN_0010bb30(puVar3,param_1 + uVar6 * 3 + -3,param_2 + -6,param_3);
        FUN_0010bb30(param_1 + 6,puVar3 + uVar6 * 3,param_2 + -9,param_3);
        FUN_0010bb30(param_1 + uVar6 * 3 + -3,puVar5,puVar3 + uVar6 * 3,param_3);
        uVar12 = *param_1;
        uStack_78 = param_1[2];
        local_80 = param_1[1];
        *param_1 = *puVar5;
        *puVar5 = uVar12;
        uVar12 = puVar5[1];
        param_1[2] = puVar5[2];
        param_1[1] = uVar12;
        puVar5[2] = uStack_78;
        puVar5[1] = local_80;
      }
      if (((param_5 & 1) == 0) &&
         (uVar4 = (*(code *)*param_3)(param_1 + -3,param_1), (uVar4 & 1) == 0)) {
        param_1 = (undefined8 *)FUN_0010bca8(param_1,param_2,param_3);
        goto LAB_0010b7bc;
      }
      auVar14 = FUN_0010be24(param_1,param_2,param_3);
      puVar3 = auVar14._0_8_;
      if ((auVar14._8_8_ & 1) == 0) break;
      uVar4 = FUN_0010bfa0(param_1,puVar3,param_3);
      uVar6 = FUN_0010bfa0(puVar3 + 3,param_2,param_3);
      if ((uVar6 & 1) != 0) {
        param_4 = -param_4;
        param_2 = puVar3;
        if ((uVar4 & 1) == 0) goto LAB_0010b5f0;
        goto LAB_0010baf8;
      }
      puVar5 = puVar3 + 3;
    } while ((uVar4 & 1) != 0);
    __introsort<std::__ndk1::_ClassicAlgPolicy,bool(*&)(std::__ndk1::pair<double,IOU::Vec2<double>>const&,std::__ndk1::pair<double,IOU::Vec2<double>>const&),std::__ndk1::pair<double,IOU::Vec2<double>>*,false>
              (param_1,puVar3,param_3,-param_4,param_5 & 1);
    param_1 = puVar3 + 3;
LAB_0010b7bc:
    param_5 = 0;
    param_4 = -param_4;
  } while( true );
LAB_0010b9b0:
  puVar3 = puVar9;
  uVar4 = (*(code *)*param_3)(puVar3,puVar5);
  if ((uVar4 & 1) != 0) {
    local_70 = puVar3[2];
    uStack_78 = puVar3[1];
    local_80 = *puVar3;
    lVar2 = lVar8;
    do {
      lVar10 = lVar2;
      puVar9 = (undefined8 *)((long)param_1 + lVar10);
      puVar9[3] = *puVar9;
      puVar9[5] = puVar9[2];
      puVar9[4] = puVar9[1];
      puVar5 = param_1;
      if (lVar10 == 0) goto LAB_0010b990;
      uVar4 = (*(code *)*param_3)(&local_80,lVar10 + -0x18 + (long)param_1);
      lVar2 = lVar10 + -0x18;
    } while ((uVar4 & 1) != 0);
    puVar5 = (undefined8 *)((long)param_1 + lVar10);
LAB_0010b990:
    *puVar5 = local_80;
    puVar9[2] = local_70;
    puVar9[1] = uStack_78;
  }
  lVar8 = lVar8 + 0x18;
  puVar9 = puVar3 + 3;
  puVar5 = puVar3;
  if (puVar3 + 3 == param_2) {
LAB_0010baf8:
    if (*(long *)(lVar1 + 0x28) == local_68) {
      return;
    }
LAB_0010bb2c:
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  goto LAB_0010b9b0;
}

