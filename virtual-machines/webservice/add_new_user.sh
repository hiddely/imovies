#!/bin/bash

sudo adduser imovies-admin <<!
password
password
!

sudo rm -rf /home/imovies-admin/.ssh
sudo mkdir /home/imovies-admin/.ssh

chmod o+w /home/imovies-admin/.ssh