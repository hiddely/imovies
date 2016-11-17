
echo "Move files to final destination"
mv /home/vagrant/interfaces /etc/network/interfaces
mv /home/vagrant/sysctl.conf /etc/sysctl.conf
touch /etc/network/if-pre-up.d/iptables
mv /home/vagrant/pre_up_iptables /etc/network/if-pre-up.d/iptables
mv /home/vagrant/iptables /etc/network/iptables