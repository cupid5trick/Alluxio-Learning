# `Kubernetes`基本概念



# 集群架构



# 工作负载

工作负载是以一组`Pod`运行在`Kubernetes`集群上的应用。一个应用可以有很多个组件，以一组Pod在集群上运行。而Pod是一组正在运行的运行容器集合。

工作负载有自己的生命周期。例如一旦某个节点上发生了致命故障，那么这个节点上所有运行的Pod都会处于失败状态，这些od必须重启，即使这个节点之后可能会恢复正常。

`Kubernetes`提供了几种内置的工作负载资源来方便地管理多个pod。

- `Pod`
- `Deployment`/`ReplicateSet`: `Deployment`很适合无状态应用的场景，`Deployment`的所有`Pod`之间都是可以互换的，可以在需要时替换。
- `StatefulSet`: `StatefulSet`适合有状态应用。如果工作负载持久化记录数据，可以运行一个`StatefulSet`，给每个`pod`分配一个`PersistentVolume`。应用代码可以把数据冗余到多个`Pod`，提升应用弹性。
- `DaemonSet`: `DaemonSet`定义了提供节点本地设施的`Pod`集合，这种工作负载可能对集群运行十分重要。适合作为网络辅助工具或附件组件。
- `Job`/`CronJob`: 定义了运行完成后结束的任务。`Job`代表只运行一次的任务，`CronJob`代表根据时间表重复的任务。

# 服务、负载均衡与网络

在Kubernetes中，服务是一种抽象，它定义了一组`Pod`的逻辑集合和访问`Pod`的策略(有时这种模式被称为微服务)。服务的目标`Pod`集合通常由选择器决定。也可以不使用选择器来定义服务端点，[Services *without* selectors](https://kubernetes.io/docs/concepts/services-networking/service/#services-without-selectors)介绍了不用选择器定义服务端点的场景(需要手动创建`EndPoint`资源)。

## 服务类型

- `Headless Service`(`ClusterIP=None`)

有时不需要负载均衡和服务IP，可以通过显式指定`ClusterIP=None`来创建无头服务。可以使用无头服务来和其他服务发现机制相配合，不需要局限于`Kubernetes`的实现。

`Kubernetes`不会给无头服务分配`ClusterIP`，`kube-proxy`不会处理无头服务，也不会有负载均衡和代理服务。`DNS`如何自动配置根据无头服务是否定义了选择器而不同。

**有选择器的无头服务**

对于有选择器的无头服务，相应的`EndPoint`会被自动创建，`DNS`也会返回一条A记录，指向提供服务的`Pod`IP地址。

**没有选择器的无头服务**

没有选择器的无头服务不会自动创建`EndPoint`。而`DNS`对所有非`ExternalName`类型的服务会查找同名`EndPoint`的记录，对`ExternalName`类型的服务查找`CNAME`记录。

- `ClusterIP`: 通过集群内部IP暴露服务，也只能从集群内部访问服务。这是默认的服务类型。

- `NodePort`: 通过集群每个节点的IP和端口暴露服务，同时自动创建一个`ClusterIP`服务。由`NodePort`服务路由到`ClusterIP`服务，可以从集群外通过`<node-ip>:<node-port>`访问服务，但是端口范围被限制在`--service-node-port-range`，默认值是`30000-32767`。
- `ExternalIP`: 适用于有一个IP能够路由到集群的情况。当对这个外部IP的请求到达集群时，将被路由到某一个服务断点。
- `ExternalName`: 把服务映射到一个可解析的DNS名称，因此是把外部服务接入`Kubernetes`的一种场景。例如`prod`名称空间中的`my-service`服务，当集群内访问服务时，集群的`DNS`服务查找`my-service.prod.svc.cluster.local`的`CNAME`别名记录，解析为`my.database.example.com`，于是能够访问外部服务。
- `LoadBalancer`: 在支持外部负载均衡组件的云环境中，设置`LoadBalancer`型服务会给服务提供一个负载均衡器。

## `DNS`

`DNS`是`Kubernetes`中重要的服务发现机制，除此以外还有[环境变量](https://kubernetes.io/docs/concepts/services-networking/service/#environment-variables)的服务发现模式。

支持集群的DNS服务器（例如`CoreDNS`）会通过Kubernetes API监视新服务，给每个服务都创建一组DNS记录。如果集群内开启了DNS，那么所有Pod都可以通过DNS名解析对应的服务。

如果`my-ns`名称空间内有一个`my-service`服务，控制平面节点和DNS服务会会共同创建一条`my-service.my-ns`的记录，同处于`my-ns`名称空间的`Pod`可以通过`my-service.my-ns`或`my-service`来访问服务。而其他名称空间的`Pod`必须使用全限定名称(`my-service.my-ns`)。

`Kubernetes`同样支持带名称端口的DNS服务。例如名称为`http`的`TCP`端口会有一条`SRV`记录`_http_._tcp.my-service.my-ns`。通过DNS的SRV查询可以获取`my-service`服务`http`端口相应的端口号和IP地址。

DNS服务是`Kubernetes`集群中访问`ExternalName`服务的唯一方式。

# 配置数据

`ConfigMap`用来以键值对的方式存储非机密数据，典型场景就是存储配置文件、环境变量等。`Secrets`用来存储数量较小的密码、令牌、秘钥等敏感数据。两者都可以作为数据卷去挂载，也可以作为环境变量去使用。

- `ConfigMap`：https://kubernetes.io/docs/concepts/configuration/configmap/#using-configmaps
- `Secrets`: https://kubernetes.io/docs/concepts/configuration/secret/#using-secrets

# 存储

存储的主要内容是数据卷([Volumes](https://kubernetes.io/docs/concepts/storage/volumes/))，最主要的两种就是`emptyDir`和`hostPath`，`ConfigMap`和`Secrets`也能当做数据卷使用因此也在数据卷类型之列。除此以外`PersistentVolumeClaim`也能作为数据卷去挂载。

卷是`Pod`的资源，但是同一个卷可以在不同容器中挂载到不同路径。

- `emptyDir`: 

  `emptyDir`卷是在`Pod`被分配到节点上时被创建的，`Pod`在该节点运行期间`emptyDir`会一直存在。`Pod`从节点上被删除时，`emptyDir`的数据被清空，因此适合作暂存。

  可以通过`emptyDir.medium="Memory"`把`tmpfs`挂载到容器，这时`emptyDir`的大小将计入容器内存的消耗，受到容器内存限制。

- `hostPath`:

  `hostPath`把主机上任意目录挂载到容器中，具有很强的灵活性。但是在多节点的情况下需要确保各节点上路径一致，每个节点`hostPath`路径下数据不一致也会带来严重的问题。

- `ConfigMap`

- `Secrets`

- `PersistentVolumeClaim`: https://kubernetes.io/docs/concepts/storage/volumes/#persistentvolumeclaim

