#!/bin/bash

su imovies-admin << EOF
    (crontab -l; echo "@reboot ssh -i /home/imovies-admin/.ssh/for_db -f -N -L 3306:127.0.0.1:3306 vagrant@172.16.0.2") | crontab -
    (crontab -l; echo "@reboot (cd /home/imovies-admin/imovies && (./run_imovies.sh & ))") | crontab -
EOF