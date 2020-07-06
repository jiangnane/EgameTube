# EgameTube
A simple, clear Android network lib.

public class FastTube extends java.lang.Object

快速获取网络数据类

使用此类进行数据获取的基本步骤如下：

1.使用getInstance()方法获取唯一实例;

2.调用init(TubeConfig)方法进行初始化操作;

3.调用getString(String, StringTubeListener), getString(String, TubeOptions, StringTubeListener), getJSON(String, JSONTubeListener), getJSON(String, TubeOptions, JSONTubeListener)等方法获取数据;

4.在需要增加主机列表时调用addHosts(String, LinkedList)方法增加一组主机列表;

5.建议2步骤在Application的初始化中调用;

6.在不再使用时调用release()释放资源，由于全局唯一实例，此方法不必须

作者:
  Hein
