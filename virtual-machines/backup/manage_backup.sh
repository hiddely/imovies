#!/usr/bin/env bash

# copy new files from imovies-backup folder
cp -a /home/imovies-backup/backups/. /home/vagrant/backups/
# remove files
rm -r /home/imovies-backup/backups/*


# find files older than 30 days
find /home/vagrant/backups/ -mindepth 1 -mtime +30 -delete