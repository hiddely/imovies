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

iptables -A INPUT -i eth0 -p udp -m multiport --sport 53,67 -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -i eth0 -p tcp -m multiport --sport 53,80,443,8080 -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -i eth0 -p udp -m multiport --sport 53,80,443,8080 -m state --state ESTABLISHED,RELATED -j ACCEPT


exit