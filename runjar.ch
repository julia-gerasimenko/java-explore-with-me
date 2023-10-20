#!/bin/bash
cd ewm-service
rm -fr target
mvn clean package

cd ..

cd  stats-service


rm -fr target

mvn clean package

