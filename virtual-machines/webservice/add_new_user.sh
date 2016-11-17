#!/bin/bash

sudo adduser imovies-admin <<!
password
password
!

# remove before recreating to get rid of provisioning error
sudo rm -rf /home/imovies-admin/.ssh
sudo mkdir /home/imovies-admin/.ssh

chmod o+w /home/imovies-admin/.ssh


# add backup user
sudo adduser imovies-backup <<!
password
password
!

sudo rm -rf /home/imovies-backup/.ssh
sudo mkdir /home/imovies-backup/.ssh

sudo rm -rf /home/imovies-backup/scripts
sudo mkdir /home/imovies-backup/scripts

chown vagrant:vagrant /home/imovies-backup/scripts
chmod o+w /home/imovies-backup/.ssh