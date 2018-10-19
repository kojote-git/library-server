mvn clean package &&
sudo mv target/*.war /opt/tomcat/webapps/lise.war &&
sudo systemctl restart tomcat