#!/bin/sh

chmod 700 /home/imovies-backup/scripts/backup.sh
chown imovies-backup:imovies-backup /home/imovies-backup/scripts/backup.sh

# Switch to vagrant user for crontab config
su imovies-backup << EOF
# daily backup at 00:00
# Update crontab
(crontab -l ; echo "0 0 * * * bash /home/imovies-backup/scripts/backup.sh")| crontab -
EOF