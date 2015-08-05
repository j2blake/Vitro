@echo off
::
:: -----------------------------------------------------------------------------
::
:: Start @PRODUCT_NAME@
::
:: First, check port 8080. If the connection is accepted, then @PRODUCT_NAME@ (or 
:: something) is already running. Complain and exit.
::
:: Start @PRODUCT_NAME@. Check every 10 seconds to see if port 8080 will accept a 
:: connection. If it does, announce success and exit. If no success after 
:: 3 minutes, announce failure and exit.
::
:: -----------------------------------------------------------------------------
::

call:checkIsRunning

if errorlevel 0 (
  echo -
  echo - @PRODUCT_NAME@ is already running on port 8080.
  echo -
  goto:eof
)

echo -
echo - Starting @PRODUCT_NAME@...

pushd %~dp0
start "jetty" /B ^
    java -Dcatalina.home=. ^
    -Dsolr.solr.home=home/solr ^
    -Dvitro.home=home ^
    -Dorg.apache.jasper.compiler.disablejsr199=true ^
    -jar lib/jetty-runner-vitro.jar ^
    --lib lib/jsp ^
    --path /@SOLR_NAME@ lib/@SOLR_NAME@.war ^
    --path /@MAIN_NAME@ lib/@MAIN_NAME@.war ^
    --stop-key abc123 --stop-port 8181 ^
    > logs/jetty.output 2>&1
popd

for /L %%t in (10, 10, 180) do (
  call:checkIsRunning
  if errorlevel 0 (
    echo - Started @PRODUCT_NAME@ on port 8080.
    echo -
    goto:eof
  ) else (
    ping 1.1.1.1 -n 1 -w 10000 >NUL
    echo - waiting for %%t seconds
  )
)
echo - @PRODUCT_NAME@ failed to start.
echo -
goto:eof

:: ----------------------------------------------------------------------------
::
:: Check to see whether Jetty is running.
::
:: This looks like it's trying to stop Jetty, but actually it just checks to 
:: see whether the connection will be accepted -- this is not the stop port.
::
:checkIsRunning
pushd %~dp0
java -jar lib/start.jar -DSTOP.PORT=8080 -DSTOP.KEY=BOGUS --stop >NUL 2>NUL
popd
goto:eof