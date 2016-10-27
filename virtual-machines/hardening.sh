#!/bin/sh

sudo su

### MYSQL ###
echo 'mysql: localhost' >> /etc/hosts.allow
echo 'mysql: ALL' >> /etc/hosts.deny

# set default policy of incoming connections
iptables -P INPUT DROP

### SSH ###
# accept all ssh input connections
iptables -A INPUT -p tcp --dport 22 -j ACCEPT


### HTTP / HTTPS ###
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT

iptables -A INPUT -p udp --dport 80 -j ACCEPT
iptables -A INPUT -p udp --dport 443 -j ACCEPT


exit