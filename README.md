# QASystem
A Java project for Question and Answer System

deployment issue

maven install -> QASystem.war
copy QASystem.war -> tomcat/webapps
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


sh tomcat/bin/start.sh
tail -100f tomcat/logs/catalina.out 