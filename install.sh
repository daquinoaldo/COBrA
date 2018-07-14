#!/usr/bin/env bash

bash compile.sh

cd DAPP
rm -rf jar

mvn clean
mvn install

cd author-server
mvn clean
mvn install

cd ../gui
mvn clean
mvn install