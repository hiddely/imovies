#!/bin/sh

#
echo "=== BACKING UP LOGS ===";

BACKUP_DIR=/tmp/backup

BACKUP_PKEY=/home/imovies-backup/.ssh/bak_rsa.pub.pem
#BACKUP_PKEY=/Users/hidde/IdeaProjects/iMovies/virtual-machines/webservice/keys/bak_rsa.pub.pem # local, to test

rm -rf $BACKUP_DIR
mkdir $BACKUP_DIR
mkdir $BACKUP_DIR/original
cd $BACKUP_DIR/original

# all files to be backed up
#cp /var/log/syslog $BACKUP_DIR/original
echo "HELLO BACKUP" > $BACKUP_DIR/original/log.txt # test log

zip -r $BACKUP_DIR/original.zip $BACKUP_DIR/original

echo "=== ENCRYPTING ===";

KEY="$(openssl rand -base64 32)";

openssl aes-256-cbc -a -salt -k $KEY -in $BACKUP_DIR/original.zip -out $BACKUP_DIR/digest.zip.enc

# encrypt our encryption key with our public key and store
echo $KEY > $BACKUP_DIR/key.pem
openssl rsautl -encrypt -inkey $BACKUP_PKEY -pubin -in $BACKUP_DIR/key.pem -out $BACKUP_DIR/key.bin.enc
rm -f $BACKUP_DIR/key.pem

echo "=== ENCRYPTED ===";
echo "=== UPLOADING TO SERVER ===";

scp -P 22 $BACKUP_DIR/key.bin.enc vagrant@192.168.1.6:~/
scp -P 22 $BACKUP_DIR/digest.zip.enc vagrant@192.168.1.6:~/

# Local, to test
#cp $BACKUP_DIR/key.bin.enc ~/Desktop/key.bin.enc
#cp $BACKUP_DIR/digest.zip.enc ~/Desktop/digest.zip.enc

echo "=== END UPLOAD ===";

echo "=== END BACKUP ===";
