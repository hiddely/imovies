#!/bin/sh

#
echo "=== BACKING UP ===";

BACKUP_DIR=/tmp/backup
DATESTRING=$(date +'%Y.%m.%d')

BACKUP_PKEY=/home/imovies-backup/.ssh/bak_rsa.pub.pem
#BACKUP_PKEY=/Users/hidde/IdeaProjects/iMovies/virtual-machines/webservice/keys/bak_rsa.pub.pem # local, to test

rm -rf $BACKUP_DIR
mkdir $BACKUP_DIR
mkdir $BACKUP_DIR/original
cd $BACKUP_DIR/original
mkdir identities

# all files to be backed up
cp /var/log/syslog $BACKUP_DIR/original
cp /var/log/auth.log $BACKUP_DIR/original
cp /var/log/kern.log $BACKUP_DIR/original
cp /var/log/faillog $BACKUP_DIR/original
cp /var/log/lastlog $BACKUP_DIR/original
cp /var/log/spring.log $BACKUP_DIR/original
cp /home/imovies-admin/imovies/src/main/resources/crypto/certificates/* $BACKUP_DIR/original/identities
echo "=== DUMPING DATABASE ===";
mysqldump -P 3306 -h 127.0.0.1 -u webservice -pwebservice imovies > $BACKUP_DIR/original/database.sql
echo "=== END DATABASE DUMP ===";

#echo "HELLO BACKUP" > $BACKUP_DIR/original/log.txt # test log

zip -r $BACKUP_DIR/original.zip $BACKUP_DIR/original

echo "=== ENCRYPTING ===";

KEY="$(openssl rand -base64 32)";

openssl aes-256-cbc -a -salt -k $KEY -in $BACKUP_DIR/original.zip -out $BACKUP_DIR/digest.$DATESTRING.zip.enc

# encrypt our encryption key with our public key and store
echo $KEY > $BACKUP_DIR/key.pem
openssl rsautl -encrypt -inkey $BACKUP_PKEY -pubin -in $BACKUP_DIR/key.pem -out $BACKUP_DIR/key.$DATESTRING.bin.enc
rm -f $BACKUP_DIR/key.pem

echo "=== ENCRYPTED ===";
echo "=== UPLOADING TO SERVER ===";

scp -P 8022 $BACKUP_DIR/key.$DATESTRING.bin.enc vagrant@172.16.0.2:~/backups/
scp -P 8022 $BACKUP_DIR/digest.$DATESTRING.zip.enc vagrant@172.16.0.2:~/backups/

# Local, to test
#cp $BACKUP_DIR/key.bin.enc ~/Desktop/key.bin.enc
#cp $BACKUP_DIR/digest.zip.enc ~/Desktop/digest.zip.enc

echo "=== END UPLOAD ===";

echo "=== Finished BACKUP ===";
