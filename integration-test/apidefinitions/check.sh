#!/bin/bash

function check() {
  for f in /json/$1-swagger*.json ; do
    echo check to ${f}
    swagger-diff -i ${f} /json/${1}.json
    status=$?
    [ $status -eq 0 ] && echo "$f compatibility was successfull" || cstatus=1
  done;
}

cstatus=0
DataList="cloudbreak,freeipa,environment,datalake,redbeams,autoscale"
Field_Separator=$IFS

echo Cloudbreak swagger compatibility check
IFS=,
for val in $DataList; do
  check $val
done
IFS=$Field_Separator

exit $cstatus


