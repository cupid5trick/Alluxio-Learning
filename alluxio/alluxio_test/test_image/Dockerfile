
FROM alpine:3.14

MAINTAINER guo
# alpine 镜像的包管理工具是apk，添加阿里云镜像
RUN echo -e "http://mirrors.aliyun.com/alpine/v3.14/community" > /etc/apk/repositories
RUN echo -e "http://mirrors.aliyun.com/alpine/v3.14/main" >> /etc/apk/repositories
# ssh 环境
RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash openssh tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    sed -i "s/#PermitRootLogin.*/PermitRootLogin yes/g" /etc/ssh/sshd_config && \
    ssh-keygen -t dsa -P "" -f /etc/ssh/ssh_host_dsa_key && \
    ssh-keygen -t rsa -P "" -f /etc/ssh/ssh_host_rsa_key && \
    ssh-keygen -t ecdsa -P "" -f /etc/ssh/ssh_host_ecdsa_key && \
    ssh-keygen -t ed25519 -P "" -f /etc/ssh/ssh_host_ed25519_key && \
    echo "root:admin" | chpasswd && \
    echo "root   ALL=(ALL)       ALL" >> /etc/sudoers && \
    mkdir /var/run/sshd
# Oracle jdk8 环境
RUN apk add --no-cache wget && \
    wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-2.33-r0.apk && \
    apk add glibc-2.33-r0.apk && \
    rm glibc-2.33-r0.apk
ADD jdk-8u271-linux-x64.tar.gz /opt
RUN mv /opt/jdk1.8.0_271 /opt/java
ENV JAVA_HOME=/opt/java
ENV CLASSPATH=$JAVA_HOME/bin
ENV PATH=.:$JAVA_HOME/bin:$PATH
WORKDIR /opt

# 添加工作负载


# 放在最后
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
