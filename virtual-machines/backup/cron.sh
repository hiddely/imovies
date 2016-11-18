#!/bin/sh


chmod 700 /home/vagrant/manage_backup.sh
rm -rf /home/vagrant/backups
mkdir /home/vagrant/backups

# daily backup at 00:00
# Update crontab
(crontab -l ; echo "0 0 * * * bash /home/vagrant/manage_backup.sh")| crontab -