/bin/echo "postinstall script started [$1]"

if [ "$1" -le 1 ]
then
  /sbin/chkconfig --add jetty
else
  /sbin/chkconfig --list jetty
fi

mkdir -p /var/log/jetty

chown -R jetty:jetty /var/log/jetty

ln -s /var/log/jetty /usr/local/jetty/log

chown jetty:jetty /usr/local/jetty

/bin/echo "postinstall script finished"
exit 0
