
/* IOU::Quad::beInSomeWise(IOU::WiseType) */

void __thiscall IOU::Quad::beInSomeWise(Quad *this,int param_2)

{
  long lVar1;
  undefined8 uVar2;
  undefined8 *local_50;
  undefined8 *local_48;
  undefined8 local_40;
  long local_38;
  
  lVar1 = tpidr_el0;
  local_38 = *(long *)(lVar1 + 0x28);
  if (param_2 != 0) {
    local_50 = (undefined8 *)0x0;
    local_48 = (undefined8 *)0x0;
    local_40 = 0;
    getVertList(this,(vector *)&local_50);
    beInSomeWiseEx(&local_50,param_2);
    uVar2 = *local_50;
    local_48 = local_50;
    *(undefined8 *)(this + 8) = local_50[1];
    *(undefined8 *)this = uVar2;
    uVar2 = local_50[2];
    *(undefined8 *)(this + 0x18) = local_50[3];
    *(undefined8 *)(this + 0x10) = uVar2;
    uVar2 = local_50[4];
    *(undefined8 *)(this + 0x28) = local_50[5];
    *(undefined8 *)(this + 0x20) = uVar2;
    uVar2 = local_50[6];
    *(undefined8 *)(this + 0x38) = local_50[7];
    *(undefined8 *)(this + 0x30) = uVar2;
    operator_delete(local_50);
  }
  if (*(long *)(lVar1 + 0x28) == local_38) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

