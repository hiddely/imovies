#!/bin/bash

echo "##### CREATE DATABASE #####"
echo 'create database imovies' | mysql -uroot -proot
mysql -uroot -proot imovies < /home/vagrant/imovies_users.dump

# create a read only user
mysql -uroot -proot -e "CREATE USER 'webservice'@'%' IDENTIFIED BY 'webservice';"
mysql -uroot -proot -e "GRANT USAGE ON *.* TO 'webservice'@'%';"
mysql -uroot -proot -e "GRANT SELECT ON imovies.* TO 'webservice'@'%';"
mysql -uroot -proot -e "GRANT LOCK TABLES ON imovies.* TO 'webservice'@'%';"

rm /home/vagrant/imovies_users.dump
