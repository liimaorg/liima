#!/bin/bash
cd `dirname $BASH_SOURCE`
TEST=`ping $@ 2>&1`
SUCCESS=`echo "$TEST" | grep '0% packet loss' | wc -l`
if [ "$SUCCESS" == "1" ] ; then
	echo "SUCCESS: $TEST";
	exit 0;
else
	echo "FAILURE: $TEST" >&2;
	exit 1;
fi