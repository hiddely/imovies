#!/bin/sh

#
echo "=== BACKING UP LOGS ===";

BACKUP_DIR=/tmp/backup

rm -rf $BACKUP_DIR
mkdir $BACKUP_DIR
mkdir $BACKUP_DIR/original
cd $BACKUP_DIR/original

# all files to be backed up
#cp /var/log/syslog $BACKUP_DIR/original
echo "HELLO BACKUP" > $BACKUP_DIR/original/log.txt # test log

zip $BACKUP_DIR/original.zip $BACKUP_DIR/original

echo "=== ENCRYPTING ===";

KEY="$(openssl rand -base64 32)";

openssl aes-256-cbc -a -salt -k $KEY -in $BACKUP_DIR/original.zip -out $BACKUP_DIR/digest.zip.enc

# encrypt our encryption key with our public key and store
echo $KEY > $BACKUP_DIR/key.pem
#openssl rsautl -encrypt -inkey /home/vagrant/.ssh/bak_rsa.pub.pem -pubin -in $BACKUP_DIR/key.pem -out $BACKUP_DIR/key.bin.enc
openssl rsautl -encrypt -inkey /Users/hidde/IdeaProjects/iMovies/virtual-machines/webservice/keys/bak_rsa.pub.pem -pubin -in $BACKUP_DIR/key.pem -out $BACKUP_DIR/key.bin.enc
rm -f $BACKUP_DIR/key.pem

echo "=== ENCRYPTED ===";
echo "=== UPLOADING TO SERVER ===";



echo "=== END UPLOAD ===";

echo "=== END BACKUP ===";
