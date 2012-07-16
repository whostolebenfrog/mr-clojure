/bin/echo "preinstall script started [$1]"

prefixDir=/usr/local/jetty
identifier={{upper-name}}.jar

isJettyRunning=`pgrep java -lf | grep $identifier | cut -d" " -f1 | /usr/bin/wc -l`
if [ $isJettyRunning -eq 0 ]
then
  /bin/echo "Jetty is not running"
else
  sleepCounter=0
  sleepIncrement=2
  waitTimeOut=600

  /bin/echo "Timeout is $waitTimeOut seconds"
  /bin/echo "Jetty is running, stopping service"
  /sbin/service jetty stop &
  myPid=$!

  until [ `pgrep java -lf | grep $identifier | cut -d" " -f1 | /usr/bin/wc -l` -eq 0 ]
  do
    if [ $sleepCounter -ge $waitTimeOut ]
    then
      /usr/bin/pkill -KILL -f '$identifier'
      /bin/echo "Killed Jetty"
      break
    fi
    sleep $sleepIncrement
    sleepCounter=$(($sleepCounter + $sleepIncrement))
  done

  wait $myPid

  /bin/echo "Jetty down"
fi

rm -rf $prefixDir

if [ "$1" -le 1 ]
then
  mkdir -p $prefixDir
  /usr/sbin/useradd -r -s /sbin/nologin -d $prefixDir -m -c "Jetty user for the Jetty service" jetty 2> /dev/null || :
fi

/usr/bin/getent passwd jetty

/bin/echo "preinstall script finished"
exit 0
