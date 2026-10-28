
/* IOU::Quad::whichWise() const */

void __thiscall IOU::Quad::whichWise(Quad *this)

{
  long lVar1;
  ulong uVar2;
  void *local_40;
  void *local_38;
  undefined8 local_30;
  long local_28;
  
  lVar1 = tpidr_el0;
  local_28 = *(long *)(lVar1 + 0x28);
  local_40 = (void *)0x0;
  local_38 = (void *)0x0;
  local_30 = 0;
  getVertList(this,(vector *)&local_40);
  uVar2 = whichWiseEx((vector *)&local_40);
  if (local_40 != (void *)0x0) {
    uVar2 = uVar2 & 0xffffffff;
    local_38 = local_40;
    operator_delete(local_40);
  }
  if (*(long *)(lVar1 + 0x28) == local_28) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar2);
}

