
/* IOU::Quad::location(IOU::Vec2<double> const&) const */

void __thiscall IOU::Quad::location(Quad *this,Vec2 *param_1)

{
  long lVar1;
  ulong uVar2;
  void *local_40;
  undefined8 uStack_38;
  undefined8 local_30;
  long local_28;
  
  lVar1 = tpidr_el0;
  local_28 = *(long *)(lVar1 + 0x28);
  local_40 = (void *)0x0;
  uStack_38 = 0;
  local_30 = 0;
  getVertList(this,(vector *)&local_40);
  uVar2 = locationEx((vector *)&local_40,param_1);
  if (local_40 != (void *)0x0) {
    uVar2 = uVar2 & 0xffffffff;
    operator_delete(local_40);
  }
  if (*(long *)(lVar1 + 0x28) == local_28) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar2);
}

