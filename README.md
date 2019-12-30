# QASystem
A Java project for Question and Answer System

deployment issue

maven install -> QASystem.war

copy QASystem.war -> tomcat/webapps

sh tomcat/bin/start.sh

vim tomcat/webapps/QASystem/WEB-INF/classes/config.ini

[mysql]
user=******

password=******

host=******

database=******

charset=******

port=******


[model]

pwd=<your workingDirectory where .h5 models lie>

invoke the test program:

curl http://localhost:8080/QASystem/Knowledge/main

curl http://localhost:8080/QASystem/Knowledge/phatic/你们公司有些什么业务

curl http://localhost:8080/QASystem/Knowledge/qatype/你们公司业务有哪些

curl http://localhost:8080/QASystem/Knowledge/similarity/你们公司有些什么业务/你们公司业务有哪些

curl http://localhost:8080/QASystem/Knowledge/update/00000000000000000000000000000000/你们公司有些什么业务/海南航空等

curl http://localhost:8080/QASystem/Knowledge/update/00000000000000000000000000000000/你们公司有什么业务/信诚人寿等

curl http://localhost:8080/QASystem/Knowledge/search/00000000000000000000000000000000/你们公司有些啥业务

and see the logging information:

tail -100f tomcat/logs/catalina.out 