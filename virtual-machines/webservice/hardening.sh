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

## Allow HTTP
iptables -A INPUT -p tcp -m tcp --dport 80 -m state --state NEW,ESTABLISHED -j ACCEPT

## Allow HTTPS
iptables -A INPUT -p tcp -m tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT

## Allow MySQL (From LAN)
iptables -A INPUT -s localhost -p tcp -m tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT

## Allow connections from port (8080)
iptables -A INPUT -p tcp -m tcp --dport 8080 -m state --state NEW,ESTABLISHED -j ACCEPT

## Allow connections from port (8443)
iptables -A INPUT -p tcp -m tcp --dport 8443 -m state --state NEW,ESTABLISHED -j ACCEPT

## Prevent HTTP DoS Attack
#> -m limit: This uses the limit iptables extension
#> --limit 25/minute: This limits only maximum of 25 connection per minute. Change this value based on your specific requirement
#> --limit-burst 100: This value indicates that the limit/minute will be enforced only after the total number of connection have reached the limit-burst level.
#iptables -A INPUT -p tcp --dport 80 -m limit --limit 25/minute --limit-burst 100 -j ACCEPT

exit
