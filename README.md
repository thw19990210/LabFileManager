## What is this
This project aims to serve for the experimental data management of Camera team.
We use database system to manage engineers' experimental files and data. At the same time, file can be searched more efficiently in the database, for example, images can be quickly screened out according to the parameters of image.
We built this program on AWS EC2. We can edit inbound rules to control internal and external network access.

## How to use

### web service
```
sudo yum update -y
sudo yum install -y httpd
sudo service httpd start
sudo chkconfig httpd on
sudo iptables -nvL
sudo iptables -A INPUT -p tcp --dport 80 --syn -m conntrack --ctstate NEW -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 --syn -m conntrack --ctstate NEW -j ACCEPT
```

### MySQL installation
```
sudo -s
sudo wget dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm
sudo rpm -Uvh mysql80-community-release-el7-7.noarch.rpm
sudo yum install -y mysql-server
service mysqld start
chkconfig mysqld on
```
#### 查看临时密码
```
vim /var/log/mysqld.log
```
#### 更改密码
```
sudo mysql_secure_installation
change password to "DBuser123!@#"
```
#### 一定要写分号:
```
mysql -u root -p

grant all privileges on *.* to root@"%" identified by "DBuser123!@#";
grant all privileges on *.* to root@"localhost" identified by "DBuser123!@#";
flush privileges;
```


### Java
#### 新开窗口
```
sudo yum install -y git
sudo yum install -y java
git clone ssh://git.amazon.com/pkg/Lab126CameraDatabase
cd Lab126CameraDatabase
java -jar target/fileloader-0.0.1-SNAPSHOT.jar
```

### MySQL query
```
create schema amazon_lab126;

use amazon_lab126;

create table account_info
(
    id            int          not null
    primary key,
    user_name     text         not null,
    password      text         not null,
    token         text         not null,
    access        text         null,
    name          varchar(32)  null,
    prof_pic_path varchar(32)  null,
    PDP_access    varchar(128) null,
    location      text         null,
    constraint account_info_id_uindex
        unique (id)
);

create table files
(
    id                int         not null
    primary key,
    project           varchar(32) null,
    sensor            varchar(32) null,
    color_temperature double      null,
    illuminance       double      null,
    ISO               double      null,
    ET                int         null,
    hardware_version  varchar(32) null,
    software_version  varchar(32) null,
    scene             varchar(32) null,
    file_type         varchar(16) null,
    file_name         text        null,
    file_path         text        not null,
    constraint files_id_uindex
        unique (id)
);

create table PDP
(
    id           int         not null
    primary key,
    project      varchar(24) null,
    item         varchar(64) null,
    priority     varchar(10) null,
    EVT3         varchar(64) null,
    EVT3_status  varchar(8)  null,
    DVT          varchar(64) null,
    DVT_status   varchar(8)  null,
    PVT          varchar(64) null,
    PVT_status   varchar(8)  null,
    MP           varchar(64) null,
    MP_status    varchar(8)  null,
    _EVT3        varchar(64) null,
    _EVT3_status varchar(8)  null,
    _DVT         varchar(64) null,
    _DVT_status  varchar(8)  null,
    _PVT         varchar(64) null,
    _PVT_status  varchar(8)  null,
    _MP          varchar(64) null,
    _MP_status   varchar(8)  null,
    jira_link    text        null
);

insert into account_info (id, name, token, user_name, password, access, PDP_access) values 
(1,'Hanwei', 'hanwtank', 'hanwtank', '123456', '/Test', 'Test'),
(2,'Xueke', 'kecaox', 'kecaox', '123456', '/Test', 'Test'),
(3,'Jamilah', 'jamilahz', 'jamilahz', '123456', '/Test', 'Test'),
(4,'Dah Way', 'wayfoon', 'wayfoon', '123456', '/Test', 'Test'),
(5,'Neville', 'huzlh', 'huzlh', '123456', '/Test', 'Test'),
(6,'Wenting', 'wentil', 'wentil', '123456', '/Test', 'Test'),
(7,'Lin', 'linlinl', 'linlinl', '123456', '/Test', 'Test'),
(8,'Bambie', 'bambieo', 'bambieo', '123456', '/Test', 'Test'),
(9,'Dick', 'dckpng', 'dckpng', '123456', '/Test', 'Test'),
(10,'Renkun', 'yanrk', 'yanrk', '123456', '/Test', 'Test'),
(11,'Shida', 'shidyu', 'shidyu', '123456', '/Test', 'Test'),
(12,'Star', 'starzeng', 'starzeng', '123456', '/Test', 'Test'),
(13,'Damon', 'dmnzhang', 'dmnzhang', '123456', '/Test', 'Test'),
(14,'Bruce', 'brucezh', 'brucezh', '123456', '/Test', 'Test');
```

access和PDP_access都是用逗号分隔，不要打空格。例如 access: '/Test,/Trona/SFR'  PDP_access: 'Test,Trona'
```
insert into files (id, file_path) values (0, "initialized");
update account_info set location = 'SZX21 - LAB126 CO(Shenzhen,CN) (UTC +08:00)';
select access from account_info where token = 'hanwtank';
delete from PDP where project = 'test';
```
#### 填写project名字来替代{project}
第一次建立请写set @ID = 0;
```
set @ID = (select id from PDP order by -id limit 1);
set @project = 'Test';
INSERT INTO
PDP (id,project,item,priority,EVT3_status,DVT_status,PVT_status,MP_status,_EVT3_status,_DVT_status,_PVT_status,_MP_status,EVT3,DVT,PVT,MP,_EVT3,_DVT,_PVT,_MP)
VALUES
(@ID+1,@project,'Project Milestone and Core team','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+2,@project,'FPY goal setting and summary','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+3,@project,'Supply Chain Map','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+4,@project,'NUDD and migration plan','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+5,@project,'Process Flow','High','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+6,@project,'CTQ, CTP, Control Plan, QCP','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+7,@project,'Critical part list','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+8,@project,'IQC','High','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+9,@project,'DFMEA, PFMEA','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+10,@project,'Traceability process flow','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+11,@project,'Machine/LIne buy off (Machine/Fixture list included)','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+12,@project,'Reliability test plan','High','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+13,@project,'Camera characterization','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+14,@project,'Camera tuning','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+15,@project,'Waiver list','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+16,@project,'limit sample','Mid','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+17,@project,'Yield Bridge Report','High','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+18,@project,'BOM','Low','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+19,@project,'SMIL','High','white','white','white','white','white','white','white','white','','','','','','','',''),
(@ID+20,@project,'2D, 3D Drawing, Test Plan, including components and devices','High','white','white','white','white','white','white','white','white','','','','','','','','');
```
#### 复制文件夹PDP到/res/storage/{project}/
```
cp -r PDP res/storage/{project}
```
#### 网页api方式执行mysql
>/api/general/mysql/execute?passcode=dbuserdbuser&sql=xxx  //执行mysql语句

>/api/general/mysql/forgetPSW?passcode=dbuserdbuser&login=xxx //忘记密码查询

## About

### 服务器连接问题
https://aws.amazon.com/cn/premiumsupport/knowledge-center/ec2-instance-hosting-unresponsive-website/

### 工具箱
use Java
use MySQL
use HTML, Javascript, CSS
use Maven to package application to jar
use Tomcat
use Spring Boot
use Vue.js
use Apache
use Hibernate

Enjoy!



