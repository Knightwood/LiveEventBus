# LiveEventBus
LiveEventBus是一款Android消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX,未来还会支持跨进程。
对livedata和Observer进行包装，并对Observer进行代理，解决粘性问题。

 目前版本 dev-1.0

# 为什么要用LiveEventBus

生命周期感知

    消息随时订阅，自动取消订阅
    告别消息总线造成的内存泄漏
    告别生命周期造成的崩溃

更多特性支持

    免配置直接使用
    支持Sticky粘性消息
    支持AndroidX
    支持延迟发送
    观察者的多种接收模式（全生命周期/激活状态可接受消息）
    观察者可决定何时不接受任何消息
    
# 在工程中引用

```
1. 在根build.gradle文件中添加:
 
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
2. 添加依赖
  dependencies {
		implementation 'com.github.Knightwood:LiveEventBus:dev-1.0'
	}
```
# 快速开始

订阅消息

  以生命周期感知模式订阅消息
```
  DataBus.with<Bean>("ChannelName").observeSticky(this, dataObserver!!)
  
   inner class DataObserver : OstensibleObserver<Bean>() {
        override fun onChanged(t: Bean?) {
            //接收到消息
        }

    }
```
发送消息
```
   DataBus.with<Bean>("ChannelName").post(Bean())
```
更多用法，目前请看源码。未来再补充。
未完待续。。。。。
