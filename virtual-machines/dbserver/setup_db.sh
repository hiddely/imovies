#!/bin/bash

echo "##### CREATE DATABASE #####"
echo 'create database imovies' | mysql -uroot -proot
mysql -uroot -proot imovies < /home/vagrant/imovies_users.dump
mysql -uroot -proot -e "CREATE USER 'webservice'@'%' IDENTIFIED BY 'webservice';"
mysql -uroot -proot -e "GRANT USAGE ON *.* TO 'webservice'@'%';"
mysql -uroot -proot -e "GRANT SELECT ON imovies.* TO 'webservice'@'%';"
# comment out bind address in /etc/mysql/mysql.conf.d/mysqld.cnf
# and restart mysql: sudo service mysql restart