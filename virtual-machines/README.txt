# Vagrant virtual machine settings

Use https://atlas.hashicorp.com/kaorimatz/boxes/ubuntu-16.04-amd64 as base image.

## Setup
Install vagrant.
In the folder where you want your vm configuration run:
vagrant init kaorimatz/ubuntu-16.04-amd64
This creates a default Vagrantfile, replace it with the custom Vagranfile included here.
These are development settings. Port forwarding is set up from 8080 guest to 8080 host to easily test the Spring webpages.

External scripts can be called on startup by:
config.vm.provision "shell", path: "script.sh"
but more advanced configuration managment systems like Chef or Puppet are also possible. See https://www.vagrantup.com/docs/provisioning/index.html

Bring it up with:
vagrant up

Connect to it via ssh on localhost 127.0.0.1, default port is 2222, user vagrant, password vagrant, or simply:
vagrant ssh

Check out this repo with:
git clone https://github.com/hlycklama/imovies.git

cd imvovies

Set Spring configurations:
cp imovies/src/main/resources/application.properties.example imovies/src/main/resources/application.properties

Path it with user root and password root.
Connect to the DB: mysql -h 127.0.0.1 -u root -p root
Create the DB infsec with:
create database infsec;

Run the maven project as instructed in the readme with (make sure you are in the imovies directory):
mvn spring-boot:run

Have fun.
