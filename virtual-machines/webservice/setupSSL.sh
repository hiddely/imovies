#!/bin/sh

sudo -i

# generate private key
openssl genrsa -out /etc/ssl/private/apache.key 4096

# generate certificate
openssl req -new -x509 -key /etc/ssl/private/apache.key -days 365 -sha256 -out /etc/ssl/certs/apache.crt

# activate ssl-module
a2emod ssl
service apache2 force-reload

# configure ssl-website
sslconf = "<VirtualHost *:443>
    SSLEngine on
    SSLCertificateFile /etc/ssl/certs/apache.crt
    SSLCertificateKeyFile /etc/ssl/private/apache.key
    
    # Pfad zu den Webinhalten
    DocumentRoot /var/www/
</VirtualHost>"

echo $sslconf > /etc/apache2/sites-available/ssl.conf

# activate virtual host
a2ensite ssl.conf
service apache2 force-reload

exit
