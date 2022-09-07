## What is this
This is a simple demonstration of uploading and downloading files via web (HTML, CSS and Javascript, here we use UIKit as the CSS theme and uploader element), and using Spring as the server.
We store the files in the database (here we simply use H2), using records of LOBs (Large Object), each having a limited volume of file's data. The first record is the master record containing the IDs of the data records.
  
## How to use
1. Build application with "mvn package -DskipTests"
2. Run application with "java -jar target/fileloader-0.0.1-SNAPSHOT.jar"
3. Connect to the server using chrome or firefox (Open http://localhost:63300/, make sure to enter the server's IP address instead of localhost in the link).
4. Drag and drop files to upload and click on the files to download (by clicking on the folders, the corresponding zip file will be downloaded).

sudo yum update -y
sudo yum install -y httpd
sudo service httpd start
sudo chkconfig httpd on
sudo iptables -nvL
sudo iptables -A INPUT -p tcp --dport 80 --syn -m conntrack --ctstate NEW -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 --syn -m conntrack --ctstate NEW -j ACCEPT

sudo -s
sudo wget dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm
sudo rpm -Uvh mysql80-community-release-el7-7.noarch.rpm
sudo yum install -y mysql-server
service mysqld start
chkconfig mysqld on
sudo mysql_secure_installation
vim /var/log/mysqld.log
"DBuser123!@#"
mysql -u root -p
grant all privileges on *.* to root@"%" identified by "DBuser123!@#"
grant all privileges on *.* to root@"localhost" identified by "DBuser123!@#"
flush privileges


sudo yum install -y git
sudo yum install -y java
git clone https://github.com/thw19990210/LabFileManager-amazon
cd LabFileManager-amazon
java -jar target/fileloader-0.0.1-SNAPSHOT.jar

/api/general/mysql/execute?passcode=dbuserdbuser&sql=
insert into account_info (id, user_name, password, token) values (0, "hanwtank", "123456", "hanwtank");

## About

https://aws.amazon.com/cn/premiumsupport/knowledge-center/ec2-instance-hosting-unresponsive-website/

use Maven to package application to jar
use Tomcat
use Spring Boot
use Vue.js

Enjoy!


