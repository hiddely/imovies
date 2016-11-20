#!/usr/bin/env bash

# RUN ON LOCAL MACHINE

(cd wan_border_router && vagrant destroy -f)
(cd lan_ips_router && vagrant destroy -f)
(cd backup && vagrant destroy -f)
(cd database && vagrant destroy -f)
(cd webservice && vagrant destroy -f)

(cd wan_border_router && vagrant up)
(cd lan_ips_router && vagrant up)
(cd backup && vagrant up)
(cd database && vagrant up)
(cd webservice && vagrant up)