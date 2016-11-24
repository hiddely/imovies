#!/bin/bash

sudo adduser imovies-admin <<!
8Q264d80&YZEgHqpS*oe
8Q264d80&YZEgHqpS*oe
!

# remove before recreating to get rid of provisioning error
sudo rm -rf /home/imovies-admin/.ssh
sudo mkdir /home/imovies-admin/.ssh

chmod o+w /home/imovies-admin/.ssh


# add backup user
sudo adduser imovies-backup <<!
xU103&44k1M7Lgydb$&$
xU103&44k1M7Lgydb$&$
!

# Give user access to adm group to view log files
sudo usermod -a -G adm imovies-backup

sudo rm -rf /home/imovies-backup/.ssh
sudo mkdir /home/imovies-backup/.ssh

sudo rm -rf /home/imovies-backup/scripts
sudo mkdir /home/imovies-backup/scripts

chown vagrant:vagrant /home/imovies-backup/scripts
chmod o+w /home/imovies-backup/.ssh