#!/bin/sh

APP_NAME={{lower-name}}

PIDS=$(pgrep java -lf | grep $APP_NAME | cut -d" " -f1);

if [ -n "$PIDS" ]
then
  echo "Jetty is already running in process $PIDS";
  exit 1
fi

JETTY_HOME=/usr/local/$APP_NAME
JAR_NAME=$JETTY_HOME/$APP_NAME.jar

IFS="$(echo -e "\n\r")"
for LINE in `cat /etc/${APP_NAME}.properties`
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
HEALTHCHECK_PATH=${SERVICE_HEALTHCHECK_PATH:-"/healthcheck"}
SERVICE_JETTY_START_TIMEOUT_SECONDS=${SERVICE_JETTY_START_TIMEOUT_SECONDS:-"60"}
SERVICE_LOGGING_PATH=${SERVICE_LOGGING_PATH:-"/var/log/"${APP_NAME}}
LOG_FILE=${SERVICE_LOGGING_PATH}/jetty.log
ERR_FILE=${SERVICE_LOGGING_PATH}/jetty.err

mkdir -p /var/encrypted/logs/${APP_NAME}

nohup java $SERVICE_JVMARGS -Dservice.logging.path=${SERVICE_LOGGING_PATH} -jar $JAR_NAME > $LOG_FILE 2> $ERR_FILE < /dev/null &

statusUrl=http://localhost:$SERVICE_PORT$HEALTHCHECK_PATH
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
    PIDS=$(pgrep java -lf | grep $APP_NAME | cut -d" " -f1);
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
