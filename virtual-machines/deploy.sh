#!/usr/bin/env bash

# RUN ON LOCAL MACHINE

(cd backup && vagrant destroy -f)
(cd database && vagrant destroy -f)
(cd webservice && vagrant destroy -f)

(cd backup && vagrant up)
(cd database && vagrant up)
(cd webservice && vagrant up)