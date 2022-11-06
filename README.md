ForkedProxy
==================

ForkedProxy is a fork of Integrated Proxy for 1.12.2 with a small rewrite and bug fixes

ForkedProxy an addon mod for [Integrated Dynamics](https://www.curseforge.com/minecraft/mc-mods/integrated-dynamics "Integrated Dynamics"). It allows you to redirect the part's target to another position.  

How to Use
==================
This mod has only one block (more planed), the access proxy here is a image of its gui.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/proxy_gui_1.png "")  
1.The top right text box shows the proxy's target.
2.The text box below sets the time in ticks for which proxy will check if a change in targets occurred(The proxy by default starts ticking at random, it can be disabled in the configs).  
3.The button in the middle allows you to change the position mode:  
        **Relative Mode**:The target pos is relative to the proxy block, for example: if you use 5 for the Y value, it means the target is 5 blocks on top of the proxy block.  
        **Absolute Mode**:The target pos is relative to the world's coordinate,  for example: if you use 5 for the Y value, it means the y coordinate of the target is actually at y=5.  
4.The X, Y, Z variable slot: you can put integer variable card in these slots, this will change the target position based on the pos mode.  
5.The Display slot: You can put variable cards of any type in here, it will show the value of this variable on the target, just like a display panel. Just like the display panel, you can right click a side of the proxy block with a wrench to rotate the displayed value. (example images down below)  

Compatibility
==================
Currently doesn't work with redstone writer and light panels (of course doesn't work with display panel), other parts all works fine with the access proxy, but if you have some problem, please post it on the github issues page.

Images
==================
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/read_block.png "")  
Read a block far away with access proxy.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/wireless_break.png "")  
Break a block far away with access proxy using Integrated Tunnels mod.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/drop_item.png "")  
Drop items to the target with Integrated Tunnels mod.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/wireless_particle.png "")  
Spawn some particles in the target position.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/entity.png "")  
Read entities in the target position.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/wireless_automation.png "")  
Access a inventory far away with Integrated Tunnels mod.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/dynamic_particle.gif "")  
An example of use variable card to dynamicly change the target.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/auto_crafting.png "")  
Autocrafting with Integrated Crafting mod.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/display_value_1.png "")  
Show a variable in the target pos.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/display_value_2.png "")  
Show a variable in the target pos.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/display_value_3.png "")  
Show a variable in the target pos and you can use wrench to rotate the displayed value.  
![](https://raw.githubusercontent.com/Srdjan-V/ForkedProxy/master/images/recipe.png "")  
The recipe of access proxy.  