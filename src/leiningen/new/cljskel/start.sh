#!/bin/sh

PIDS=$(pgrep java -lf | grep {{upper-name}} | cut -d" " -f1);

if [ -n "$PIDS" ]
then
  echo "Jetty is already running in process $PIDS";
  exit 1
fi

JETTY_HOME=/usr/local/jetty
JAR_NAME=$JETTY_HOME/{{upper-name}}.jar
LOG_FILE=$JETTY_HOME/log/jetty.log
ERR_FILE=$JETTY_HOME/log/jetty.err

IFS="$(echo -e "\n\r")"
for LINE in `cat /usr/local/deployment/{{upper-name}}1/config/post_install.properties`
do
  case $LINE in
    \#*) ;;
    *)
      LEFT=`echo $LINE | cut -d"=" -f1`
      RIGHT=`echo $LINE | cut -d"=" -f2- | sed -e 's/\\\:/:/g' | sed -e 's/\\\=/=/g' | sed -e 's/\\\ / /g' | sed -e 's/\\\!/!/g' | sed -e 's/\\\\\\\/\\\/g'`
      ULEFT=`echo $LEFT | awk '{print toupper($0)}' | sed -e 's/\./_/g'`
      export $ULEFT=$RIGHT
  esac
done

IFS="$(echo -e " ")"

SERVICE_PORT=${SERVICE_PORT:-"8080"}
STATUS_PATH=${SERVICE_STATUS_PATH:-"/1.x/status"}
SERVICE_JETTY_START_TIMEOUT_SECONDS=${SERVICE_JETTY_START_TIMEOUT_SECONDS:-"15"}

nohup java -Dconfig=/usr/local/deployment/{{upper-name}}1/config/post_install.properties $SERVICE_JVMARGS -jar $JAR_NAME > $LOG_FILE 2> $ERR_FILE < /dev/null &

statusUrl=http://localhost:$SERVICE_PORT$STATUS_PATH
waitTimeout=$SERVICE_JETTY_START_TIMEOUT_SECONDS
sleepCounter=0
sleepIncrement=2
  
echo "Giving Jetty $waitTimeout seconds to start successfully"
echo "Using $statusUrl to determine service status"

retVal=0

until [ `curl --write-out %{http_code} --silent --output /dev/null $statusUrl` -eq 200 ]  
do
  if [ $sleepCounter -ge $waitTimeout ]
  then
    echo "Jetty didn't start within $waitTimeout seconds."
    PIDS=$(pgrep java -lf | grep {{upper-name}} | cut -d" " -f1);
    if [ -n "$PIDS" ]
	then
	  echo "Killing $PIDS";
	  echo $PIDS | xargs kill;
	else
	  echo "No running instances found";
	fi
    retVal=1
    break
  fi
  sleep $sleepIncrement
  sleepCounter=$(($sleepCounter + $sleepIncrement))
done

echo ======================================================
echo Contents of $LOG_FILE
echo ======================================================
cat $LOG_FILE
echo ====================================================== 1>&2
echo Contents of $ERR_FILE 1>&2
echo ====================================================== 1>&2
cat $ERR_FILE 1>&2

if [ $retVal -eq 1 ]
then
  echo "Starting Jetty failed"
else
  echo "Starting Jetty succeeded"
fi

exit $retVal
