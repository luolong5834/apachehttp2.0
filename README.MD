## HTTP2.0下的H2C的使用

#### 简介

- HTTP2.0的原理请参照：https://blog.csdn.net/zhuyiquan/article/details/69257126?locationNum=4&fps=1
  
- 特别注意：http2.0什么时候用，哪些工具支持
  
#### 环境要求

- jdk1.7+,apache-compent-5.0-beta+,tomcat8.5+
   
#### 外部配置

- 由于spring boot2.0明确提出不支持h2c,所以得吧项目部署到外部tomcat8.5+
   
- 更改tomcat8.5 server.xml的配置，如下

        <Connector port="8441" protocol="HTTP/1.1" connectionTimeout="20000" >
           <UpgradeProtocol className="org.apache.coyote.http2.Http2Protocol" />
        </Connector>

- UpgradeProtocol代表就是http2de h2c
    
- 项目war丢到tomcat/webapps下启动tomcat
    
- 可以是使用curl测试配置的tomcat server是否支持h2c,curl windows版本下载链接：https://curl.haxx.se/
- curl命令:curl --verbose --http2  host:post,其他options参照curl官方文档
    
#### 代码

- 项目中TestH2c提供了测试http2.0 h2c方式的demo
   
- 注意：httpclient最新组织名更改了
     Maven group id changed to 'org.apache.httpcomponents.client5'
   
#### 其他事项

- 后续增加http2.0 h2方式，有其他意见，联系我，谢谢。
    