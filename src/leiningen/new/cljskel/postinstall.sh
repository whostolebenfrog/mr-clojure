/bin/echo "postinstall script started [$1]"

APP_NAME={{lower-name}}

if [ "$1" -le 1 ]
then
  /sbin/chkconfig --add $APP_NAME
else
  /sbin/chkconfig --list $APP_NAME
fi

mkdir -p /var/log/$APP_NAME

chown -R $APP_NAME:$APP_NAME /var/log/$APP_NAME

ln -s /var/log/$APP_NAME /usr/local/$APP_NAME/log

chown $APP_NAME:$APP_NAME /usr/local/$APP_NAME

/bin/echo "postinstall script finished"
exit 0
