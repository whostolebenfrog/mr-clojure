/bin/echo "postinstall script started [$1]"

APP_NAME={{lower-name}}

if [ "$1" -le 1 ]
then
  /sbin/chkconfig --add $APP_NAME
else
  /sbin/chkconfig --list $APP_NAME
fi

ln -s /var/encrypted/logs/$APP_NAME /var/log/$APP_NAME

chown -R $APP_NAME:$APP_NAME /usr/local/$APP_NAME

chmod 755 /usr/local/$APP_NAME/bin

/bin/echo "postinstall script finished"
exit 0
