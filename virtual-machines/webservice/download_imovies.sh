#!/bin/bash
cd /home/imovies-admin

eval "$(ssh-agent -s)"
ssh-add /home/imovies-admin/.ssh/id_rsa

echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> /home/imovies-admin/.ssh/config

chown imovies-admin:imovies-admin /home/imovies-admin/.ssh/
chown imovies-admin:imovies-admin /home/imovies-admin/.ssh/id_rsa
chown imovies-admin:imovies-admin /home/imovies-admin/.ssh/id_rsa.pub

chmod 700 /home/imovies-admin/.ssh/
chmod 600 /home/imovies-admin/.ssh/id_rsa

echo "##### DOWNLOAD FROM GITHUB #####"
git init
su imovies-admin << EOF
git clone git@github.com:hlycklama/imovies.git
EOF
cd imovies
find . -type f -exec chmod 600 {} +
find . -type d -exec chmod 700 {} +
chmod 700 ./mvnw

cp ./src/main/resources/application.properties.example ./src/main/resources/application.properties
chown imovies-admin:imovies-admin /home/imovies-admin/imovies/src/main/resources/application.properties

# run imovies in daemon
#daemon --name="imovies-webservice" --output=webservice.txt sh /home/imovies-admin/imovies/run_imovies.sh
(cd /home/imovies-admin/imovies && (./run_imovies.sh & ))
