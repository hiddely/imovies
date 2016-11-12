#!/bin/sh

## This code is taken from
## http://www.bonusbits.com/wiki/HowTo:Configure_iptables_to_Allow_Access_to_Common_Services_on_Linux#Allow_HTTP_.28TCP_Port_80.29
## and modified according to our purposes

sudo su
# Delete All Existing Rules
iptables --flush

# Set Default Chain Policies
iptables -P INPUT DROP
iptables -P OUTPUT ACCEPT
iptables -P FORWARD ACCEPT

## Allow Loopback
iptables -A INPUT -i lo -j ACCEPT

## Allow Established and Related Connections
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

## Allow SSH
iptables -A INPUT -p tcp -m tcp --dport 22 -m state --state NEW,ESTABLISHED -j ACCEPT

## Allow MySQL (From LAN)
iptables -A INPUT -s 192.168.1.4 -p tcp -m tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT


exit
