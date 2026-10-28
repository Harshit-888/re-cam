
/* IOU::Quad::area() const */

void __thiscall IOU::Quad::area(Quad *this)

{
  long lVar1;
  undefined8 uVar2;
  void *local_50;
  void *local_48;
  undefined8 local_40;
  long local_38;
  
  lVar1 = tpidr_el0;
  local_38 = *(long *)(lVar1 + 0x28);
  local_50 = (void *)0x0;
  local_48 = (void *)0x0;
  local_40 = 0;
  getVertList(this,(vector *)&local_50);
  uVar2 = areaEx((vector *)&local_50);
  if (local_50 != (void *)0x0) {
    local_48 = local_50;
    operator_delete(local_50);
  }
  if (*(long *)(lVar1 + 0x28) == local_38) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar2);
}

