
/* IOU::Quad::interPts(IOU::Line const&, std::__ndk1::vector<IOU::Vec2<double>,
   std::__ndk1::allocator<IOU::Vec2<double> > >&) const */

void __thiscall IOU::Quad::interPts(Quad *this,Line *param_1,vector *param_2)

{
  long lVar1;
  ulong uVar2;
  void *local_50;
  undefined8 uStack_48;
  undefined8 local_40;
  long local_38;
  
  lVar1 = tpidr_el0;
  local_38 = *(long *)(lVar1 + 0x28);
  local_50 = (void *)0x0;
  uStack_48 = 0;
  local_40 = 0;
  getVertList(this,(vector *)&local_50);
  uVar2 = interPtsEx((vector *)&local_50,param_1,param_2);
  if (local_50 != (void *)0x0) {
    uVar2 = uVar2 & 0xffffffff;
    operator_delete(local_50);
  }
  if (*(long *)(lVar1 + 0x28) == local_38) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar2);
}

