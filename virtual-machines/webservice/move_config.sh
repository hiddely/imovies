
echo "Move files to final destination"
chown root:root /home/vagrant/networking/*
touch /etc/network/if-pre-up.d/iptables
mv -f /home/vagrant/networking/interfaces /etc/network/interfaces
mv -f /home/vagrant/networking/pre_up_iptables /etc/network/if-pre-up.d/iptables
mv -f /home/vagrant/networking/iptables /etc/network/iptables

# clean up if anything was left behind
rm -f /home/vagrant/networking/*