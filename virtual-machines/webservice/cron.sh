#!/bin/sh

# Switch to vagrant user for crontab config
su imovies-backup

chmod 700 /home/imovies-backup/scripts/backup.sh

# daily backup at 00:00
# Update crontab
(crontab -l ; echo "0 0 * * * bash /home/imovies-backup/scripts/backup.sh")| crontab -