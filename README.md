FManager
========

A manager platform based Floodlight REST API

========

Author: Li JianFeng @SCUEC<br>

大家嚎啊＼（⌒∇⌒）／<br>
我又回来更新这个玩意了o(*￣3￣)o<br>

经过半个月的披(xin)星(bu)戴(zai)月(yan)的重构工作，终于把之前名为FLTopoView的惨不忍睹的管理平台重构了*★,°*:.☆\(￣▽￣)/$:*.°★* 。<br>
为啥前一个版本的名字叫得如此土鳖呢Σ( ° △ °|||)︴<br>
因为我的初衷只是想写一个显示Floodlight拓扑图的工具而已，FL自带的显示工具实在是太鬼畜了w(ﾟДﾟ)w<br>
写着写着又想写个获取拓扑信息的中间件，给算法程序用-________-''<br>
后来索性写了个算法容器o(￣ヘ￣o* )<br>
再后来听了纪晓峰老师的意见，加了生成虚拟拓扑的模块~\(≧▽≦)/~<br>
最后为了实现大赛题目的要求又加了个流量工程的模块⊙﹏⊙<br>
深感用代码一行一行写界面真是难受啊。。。写出来的界面真是丑爆了，简直把我的审美暴露无遗T^T<br>
名字也是人不人鬼不鬼的FLTopoView。。。黑历史啊黑历史(╯﹏╰）<br>
之前的版本纯粹是为了达到功能目的不择手段的产物，包括一个类里从头到尾就一个方法啊静态变量随意用啊代码冗余啊谭浩强式变量命名法啊等等等等，就如年少时看过郭敬明的小说一样，让我觉得现在脸上无光o(︶︿︶)o<br>
于是半个月以前开始从零开始重构平台的代码，当初软件工程不好好听课，现在真是现世报( >﹏<。)～……<br>
从基础的通信模块，拓扑提取，可视化，各功能模块，一个一个完成，期间还抽空去了趟天津，GF那几天也在复习期末考，所以就出现了不远千里过去的我拿着她的笔记本在coding，她在复习保险学考试的催人泪下的场景╰(￣▽￣)╮<br>
真是嘴上说要当学霸，身体却很诚实呢(╯▽╰)<br>
还好终于在六月底把所有代码改完，发现的BUG绝大部分修复了，没修复的也在代码里标记了FIXME。。。<(￣▽￣)><br>
5000行代码，算不上是开源，给大家过过目吧，能有一些启发也是好的(＾▽＾)ｺ<br>
