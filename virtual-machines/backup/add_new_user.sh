#!/bin/bash

sudo adduser imovies-backup <<!
password
password
!

# remove before recreating to get rid of provisioning error
sudo rm -rf /home/imovies-backup/.ssh
sudo mkdir /home/imovies-backup/.ssh
chmod 700 /home/imovies-backup/.ssh
chown imovies-backup:imovies-backup /home/imovies-backup/.ssh

sudo rm -rf /home/imovies-backup/backups
sudo mkdir /home/imovies-backup/backups
chmod o+w /home/imovies-backup/backups