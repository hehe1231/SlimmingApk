提升应用的启动速度和splash页面的设计
1.启动分为两种方式:
1.1冷启动:从桌面直接启动,同时后台没有该进程的缓存,这时系统需要重新创建个一个新的进程并且分配各种资源.
1.2热启动:该app有该进程的缓存,这时启动的进程就是属于热启动.
热启动不需要重新分配进程,也不会走application,直接走的就是app的入口activity,这样速度快						   很多.

2.如何测量一个app的启动时间.
使用命令行来启动app,同时进行时间测量.单位:毫秒
adb shell am start -W [PackageName]/[PackageName.MainActivity]
运行结果:
ThisTime: 0------------启动当前activity所需时间
TotalTime: 0------------整个应用的启动时间,application_activity的启动时间
WaitTime: 44------------包括系统的影响时间---------一般来说最大
Complete

3.应用启动的流程
application从构造方法开始--->attachBaseContext()--->onCreate()
activity构造方法---->onCreate()---->设置显示界面,设置主题、背景等等属性--->onStart()--->onResume()--->显示里面的view(测量、布局、绘制,显示到界面上)

时间花在哪里了?

4.减少应用的启动时间的耗时
4.1不要在application的构造方法、attachbasecontext()、oncreate()立面做初始化耗时的操作.
4.2mainactivity,由于用户只关心最后的显示的那一帧,对于我们布局的层次要求减少,自定义控件的测量、布局、绘制的时间要减少.不要在onCreate、onStart、onResume当中做耗时操作.
4.3对于sharedpreference的初始化需要注意
因为他初始化的时候是要需要将数据全部读取出来放到内存当中.
4.3.1优化1:可以尽可能减少sp文件数量(io需要时间)
4.3.2优化2:像这样的初始化最好放到线程里面.
4.3.3优化3:大的数据最好缓存到数据库里面

app启动的耗时主要是在:application初始化+mainactivity的界面加载绘制时间.
由于mainactivity的业务和布局复杂度非常高,甚至该界面必须要有一些初始化的数据才能显示.
这样会导致mainactivity有可能加载半天都出不来,给用户app很卡顿的感觉,我们要给用户一种干净利落的体验,一点击app就立马弹出我们的界面.

一点击就有东西加载出来,不要停顿一些时间才加载出来.于是乎想到使用splashactivity----非常简单的一个欢迎页,欢迎页什么都不干只显示一个图片.
splashactivity启动显示完图片后,再跳转到mainactivity,起码启动时不会有一种卡顿的感觉.

解决:
将splashactivity和mainactivity合为一个.
一进来还是现实的mainactivity,splashactivity可以变成一个splashfragment,然后放一个fragmentlayout作为跟布局实现splashactivity界面.
splashfragment里面非常简单,就是实现一个图片,启动非常快.
当splashfragment显示完毕后再将它remove.同时在splash的2S的友好时间内进行网络数据缓存.
这个时候我们才看到mainactivity,就不必再去等待网络数据返回.

问题:splashview和contentview加载放到一起来做了,这可能影响应用的启动时间.
解决:可以使用viewstub延时加载mainactivity当中的view来达到减轻这个影响.

viewstub的设计就是为了防止mainactivity的启动加载资源太耗时了.延迟进行加载,不影响启动,用户体验友好.

5.如何设计延时加载delayload
第一时间想到的就是onCreate里面调用handler.postDelayd()方法
问题:这个延迟时间如何控制
不同的机器启动速度不一样,这个时间如何控制?无法控制
需要达到的效果:应用已经启动并加载完成,界面已经全部显示完毕了,我们再去做如网络加载等耗时操作.用这种传统的方式无法做到应用已经启动并加载完成,界面已经全部显示完毕.

Handler mHanlder = new Handler();
mHanlder.postDelayed(new Runnable() {
    @Override
    public void run() {
        //TODO:
    }
}, 2500);
用这种传统的方式无法做到应用已经启动并加载完成,界面已经全部显示完毕. 无法控制时间.
问题:什么时候应用已经启动并加载完成,界面已经全部显示出来了?
onResume所有任务执行之后才显示完毕.不行.

如果application和mainactivity中有耗时操作,可以放到子线程去操作,如果非得放到主线程去操作
要

思路:
0.利用fragment代替activity,将splashactivity换成splashfragment,将splash和main撮合成到一块.
1.首先第一步在splashactivity显示图片,形成不卡顿的现象,可以在splashactivity中请求mainactivy中所需的数据,在mainactivity中订阅splashactivity中的接口,获取到返回的数据.
2.在mainactivity中getWindow().getDecorView().post(new Runnable(){
public void run(){
//去做耗时操作或后续需要做的事情(如动画)
//这种的做法的意思是先将所有界面先给我加载出来,再去做后面的事情.
}
});