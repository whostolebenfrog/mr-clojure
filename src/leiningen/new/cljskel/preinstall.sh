/bin/echo "preinstall script started [$1]"

APP_NAME={{lower-name}}
prefixDir=/usr/local/$APP_NAME
identifier=$APP_NAME.jar

isJettyRunning=`pgrep java -lf | grep $identifier | cut -d" " -f1 | /usr/bin/wc -l`
if [ $isJettyRunning -eq 0 ]
then
  /bin/echo "{{upper-name}} is not running"
else
  sleepCounter=0
  sleepIncrement=2
  waitTimeOut=600

  /bin/echo "Timeout is $waitTimeOut seconds"
  /bin/echo "{{upper-name}} is running, stopping service"
  /sbin/service $APP_NAME stop &
  myPid=$!

  until [ `pgrep java -lf | grep $identifier | cut -d" " -f1 | /usr/bin/wc -l` -eq 0 ]
  do
    if [ $sleepCounter -ge $waitTimeOut ]
    then
      /usr/bin/pkill -KILL -f '$identifier'
      /bin/echo "Killed {{upper-name}}"
      break
    fi
    sleep $sleepIncrement
    sleepCounter=$(($sleepCounter + $sleepIncrement))
  done

  wait $myPid

  /bin/echo "{{upper-name}} down"
fi

rm -rf $prefixDir

if [ "$1" -le 1 ]
then
  mkdir -p $prefixDir
  /usr/sbin/useradd -r -s /sbin/nologin -d $prefixDir -m -c "{{upper-name}} user for the {{upper-name}} service" $APP_NAME 2> /dev/null || :
fi

/usr/bin/getent passwd $APP_NAME

/bin/echo "preinstall script finished"
exit 0
