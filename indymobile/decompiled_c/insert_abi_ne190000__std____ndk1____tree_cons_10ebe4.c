
/* WARNING: Heritage AFTER dead removal. Example location: x0 : 0x0010ec40 */
/* WARNING: Restarted to delay deadcode elimination for space: register */
/* void std::__ndk1::set<int, std::__ndk1::less<int>, std::__ndk1::allocator<int>
   >::insert[abi:ne190000]<std::__ndk1::__tree_const_iterator<int, std::__ndk1::__tree_node<int,
   void*>*, long> >(std::__ndk1::__tree_const_iterator<int, std::__ndk1::__tree_node<int, void*>*,
   long>, std::__ndk1::__tree_const_iterator<int, std::__ndk1::__tree_node<int, void*>*, long>) */

void __thiscall
std::__ndk1::set<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>>::
insert_abi_ne190000_<std::__ndk1::__tree_const_iterator<int,std::__ndk1::__tree_node<int,void*>*,long>>
          (set<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>> *this,long *param_2,
          long *param_3)

{
  undefined4 uVar1;
  long lVar2;
  bool bVar3;
  __tree<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>> *p_Var4;
  undefined8 *puVar5;
  long *plVar6;
  undefined1 auStack_58 [8];
  undefined8 local_50;
  long local_48;
  
  lVar2 = tpidr_el0;
  local_48 = *(long *)(lVar2 + 0x28);
  p_Var4 = (__tree<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>> *)this;
  do {
    while( true ) {
      if (param_2 == param_3) {
        if (*(long *)(lVar2 + 0x28) != local_48) {
                    /* WARNING: Subroutine does not return */
          __stack_chk_fail();
        }
        return;
      }
      p_Var4 = (__tree<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>> *)
               __tree<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>>::__find_equal<int>
                         (p_Var4,this + 8,&local_50,auStack_58,(long)param_2 + 0x1c);
      if (*(long *)p_Var4 != 0) break;
      puVar5 = operator_new(0x20);
      uVar1 = *(undefined4 *)((long)param_2 + 0x1c);
      *puVar5 = 0;
      puVar5[1] = 0;
      puVar5[2] = local_50;
      *(undefined8 **)p_Var4 = puVar5;
      plVar6 = *(long **)this;
      *(undefined4 *)((long)puVar5 + 0x1c) = uVar1;
      if (*plVar6 != 0) {
        *(long *)this = *plVar6;
        puVar5 = *(undefined8 **)p_Var4;
      }
      p_Var4 = *(__tree<int,std::__ndk1::less<int>,std::__ndk1::allocator<int>> **)(this + 8);
      FUN_0010ea50(p_Var4,puVar5);
      *(long *)(this + 0x10) = *(long *)(this + 0x10) + 1;
      plVar6 = (long *)param_2[1];
      if ((long *)param_2[1] == (long *)0x0) goto LAB_0010ecc4;
LAB_0010ecb4:
      do {
        param_2 = plVar6;
        plVar6 = (long *)*param_2;
      } while ((long *)*param_2 != (long *)0x0);
    }
    plVar6 = (long *)param_2[1];
    if ((long *)param_2[1] != (long *)0x0) goto LAB_0010ecb4;
LAB_0010ecc4:
    do {
      plVar6 = (long *)param_2[2];
      bVar3 = (long *)*plVar6 != param_2;
      param_2 = plVar6;
    } while (bVar3);
  } while( true );
}

