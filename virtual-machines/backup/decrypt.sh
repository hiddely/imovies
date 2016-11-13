#!/usr/bin/env bash

DATA="$1";
SYMFILE="$2";
PRIVATE_KEY="$3";
OUTPUT="$4";

FAILED=0;

if [ ! -e $DATA ]; then
    echo "Path '$DATA' to data file not found.";
    FAILED=1;
fi
if [ ! -e $SYMFILE ]; then
    echo "Path '$SYMFILE' to symmetric key file not found.";
    FAILED=1;
fi
if [ ! -e $PRIVATE_KEY ]; then
    echo "Path '$PRIVATE_KEY' to private key file not found.";
    FAILED=1;
fi
if [ ! -d $OUTPUT ]; then
    echo "Path '$OUTPUT' to output dir not found.";
    FAILED=1;
fi
if [ FAILED == 1 ]; then
    echo "Invalid command. Usage: ./decrypt.sh [DATA file] [KEY file] [PRIVATE KEY file] [OUTPUT dir]";
    exit 1;
fi

SYM_KEY="$(openssl rsautl -decrypt -inkey $PRIVATE_KEY -in $SYMFILE)";

echo "Decrypted symmetric key";

openssl aes-256-cbc -a -d -salt -k $SYM_KEY -in $DATA -out $OUTPUT/backup.zip

echo "Decrypting archive";

unzip $OUTPUT/backup.zip -d $OUTPUT


