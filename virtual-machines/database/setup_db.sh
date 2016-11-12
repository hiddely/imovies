#!/bin/bash

echo "##### CREATE DATABASE #####"
echo 'create database imovies' | mysql -uroot -proot
mysql -uroot -proot imovies < /home/vagrant/imovies_users.dump

rm /home/vagrant/imovies_users.dump