#! /usr/bin/env sh

# -----------------------------------------------------------------------------
#
# Start @PRODUCT_NAME@
#
# First, check port 8080. If the connection is accepted, then @PRODUCT_NAME@ (or 
# something) is already running. Complain and exit.
#
# Start @PRODUCT_NAME@. Check every 10 seconds to see if port 8080 will accept a 
# connection. If it does, announce success and exit. If no success after 
# 3 minutes, accounce failure and exit.
#
# -----------------------------------------------------------------------------
#

check_is_running() {
  echo X | telnet -c -e X localhost 8080 > /dev/null 2>&1
}

check_is_running
if [ $? == 0 ] 
then
  echo
  echo "@PRODUCT_NAME@ is already running on port 8080."
  echo
  exit
fi

echo
echo "Starting @PRODUCT_NAME@..."

BASE_DIR="$( cd "$(dirname "$0")" ; pwd )"

java -Dcatalina.home=$BASE_DIR \
     -Dsolr.solr.home=$BASE_DIR/home/solr \
     -Dvitro.home=$BASE_DIR/home \
     -Dorg.apache.jasper.compiler.disablejsr199=true \
     -jar $BASE_DIR/lib/jetty-runner-vitro.jar \
     --lib $BASE_DIR/lib/jsp \
     --path /@SOLR_NAME@ $BASE_DIR/lib/@SOLR_NAME@.war \
     --path /@MAIN_NAME@ $BASE_DIR/lib/@MAIN_NAME@.war \
     > $BASE_DIR/logs/jetty.output 2>&1 &

for time in 10 20 30 40 50 60 70 80 90 100 110 120 130 140 150 160 170 180
do
  check_is_running
  if [ $? == 0 ]
  then
    echo "Started @PRODUCT_NAME@ on port 8080."
    echo
    exit
  fi
  sleep 10
  echo "waiting for $time seconds"
done
echo
echo "@PRODUCT_NAME@ failed to start."
echo
