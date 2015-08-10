@echo off
::
:: -----------------------------------------------------------------------------
::
:: Stop @PRODUCT_NAME@
::
:: Check port 8080. If the connection is rejected, then @PRODUCT_NAME@ is not running.
:: Say so, and exit.
::
:: Send the stop command, and wait up to 30 seconds for it to acknowledge. If it
:: doesn't, complain and exit.
::
:: -----------------------------------------------------------------------------
::

call:checkIsRunning

if not errorlevel 0 (
  echo -
  echo - @PRODUCT_NAME@ is not running.
  echo -
  goto:eof
)

echo -
echo - Stopping @PRODUCT_NAME@.

pushd %~dp0
java -jar lib/start.jar -DSTOP.PORT=8181 -DSTOP.KEY=abc123 -DSTOP.WAIT=30 --stop >NUL 2>NUL
popd

if errorlevel -4 (
  echo -
  echo - FAILED TO STOP @PRODUCT_NAME@.
  echo -
  goto:eof
)

echo -
echo - Stopped.
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