#!/bin/bash

su imovies-admin << EOF
    ssh -i /home/imovies-admin/.ssh/for_db -f -N -L 3306:127.0.0.1:3306 vagrant@192.168.1.5
EOF