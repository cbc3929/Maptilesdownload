![](https://gitee.com/cc3929/Pic_go/raw/master/image/geotools/20211124160909.png)

**一切所需都在db.setting中配置然后执行```ThreadPoolExecutorDemo.download()```即可**

一款自动下载google tiles 的在线下载器，速度凶残，达到了每秒1000张左右，由于没加UA所有有可能**被封**。速度就是快

**必须挂载postgis库才能运行**，挂载postgis库的原因是减小复杂行政区单位的下载量（平均减少50%）19级左右就可以减少数百万张瓦片
**save time**

数据库仅仅是做计算使用，没有任何读取的sql，不方便的可以本地新建数据库或者建立临时数据库作为挂载使用， 但是必须挂载Postgis关于安装Postgis请自行google。

**别忘了自己配置Proxy的接口**

目前的缺陷：

* 无进度条显示下载进度
* 只支持shp格式的文件，未来考虑加入geojson或者直接从数据库指定空间数据
* 对于数据的投影格式支持还不完全，未考虑自相交的情况，请确保图形封闭，否则可能报错

## enjoy it！！！！！！

> product by cc