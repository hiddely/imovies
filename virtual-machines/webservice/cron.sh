#!/bin/sh

# Switch to vagrant user for crontab config
su vagrant

# daily backup at 00:00
# Update crontab
(crontab -l ; echo "0 0 * * * bash ~/scripts/backup.sh")| crontab -