#!/bin/sh

if [ -z "$1" ]; then
    echo "Usage: ./ssh_setup.sh [user] [port]"
    break
elif [ -z "$2" ]; then
    echo "Usage: ./ssh_setup.sh [user] [port]"
    break
else
    user=$1
    port=$2

    echo "##### DEPLOY KEYS ON SERVER #####"
    for key in ssh_public_keys/*.pub; do
        cat $key | ssh -p "$port" "$user"@localhost "cat >> ~/.ssh/authorized_keys"
        scp -P "$port" ./sshd_config "$user"@localhost:/etc/ssh/sshd_config
    done
fi

