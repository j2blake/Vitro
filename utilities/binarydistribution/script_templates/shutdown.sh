#! /usr/bin/env sh

#
# -----------------------------------------------------------------------------
# 
# Stop @PRODUCT_NAME@.
#
# Look for a running process using the Jetty Runner jar. If not running, 
# complain and exit.
#
# Kill the process. Check every two seconds to see whether it is gone. If it's
# still running after 20 seconds, complain and exit.
#
# -----------------------------------------------------------------------------
# 

check_is_running() {
  pgrep -f jetty-runner-vitro.jar > /dev/null
}

check_is_running
if [ $? != 0 ]
then
  echo
  echo "@PRODUCT_NAME@ is not running."
  echo
  exit
fi

echo
echo "Stopping @PRODUCT_NAME@."
pkill -f jetty-runner-vitro.jar

for time in 2 4 6 8 10 12 14 16 18 20
do
  check_is_running
  if [ $? != 0 ]
  then
    echo "Stopped."
    echo
    exit
  fi
  sleep 2
done
echo
echo "@PRODUCT_NAME@ did not stop."
echo
