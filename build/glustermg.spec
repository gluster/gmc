%define product_family Gluster Management Gateway

%define current_arch %{_arch}
%ifarch i386
%define current_arch x86
%endif

Summary:        %{product_family} web IU component
Name:           glustermg
Version:        %{release_version}
Release:        1%{?extra_release}
License:        GPLv3+
Group:          System Environment/Base
Source0:        glustermg-backend-%{release_version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Requires:       tomcat6 >= 6.0.24
Requires:       java-1.6.0-openjdk >= 1.6.0.0
Requires:       wget
%description
%{product_family} web UI component for GlusterFS and Gluster appliances

%package        backend
Summary:        %{product_family} server side backend tools
Group:          System Environment/Base
Requires(post):  /sbin/chkconfig
Requires(preun): /sbin/chkconfig
Requires:       python >= 2.4.3
Requires:       perl >= 5.8.8
Requires:       rrdtool-perl >= 1.2.27
Requires:       sudo
Requires:       crontabs
Requires:       samba >= 3.5.6
Requires:       libxml2 >= 2.6.26
%description    backend
%{product_family} server side backend tools


%prep
%setup -q -n glustermg-backend-%{release_version}

%build

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/%{release_version}
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/keys
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/etc
mkdir -p $RPM_BUILD_ROOT/var/log/glustermg
tar -C $RPM_BUILD_ROOT/opt/glustermg/%{release_version} -zxf %{_sourcedir}/glustermg-%{release_version}.war.tar.gz
%{__install} -d -m0755 %{buildroot}%{_bindir}
%{__install} -d -m0755 %{buildroot}%{_sbindir}
ln -sf /opt/glustermg/%{release_version}/glustermg/scripts/grun.py %{buildroot}%{_bindir}/grun.py
ln -sf /opt/glustermg/%{release_version}/glustermg/scripts/add_user_cifs_all.py %{buildroot}%{_sbindir}/add_user_cifs_all.py
ln -sf /opt/glustermg/%{release_version}/glustermg/scripts/delete_user_cifs_all.py %{buildroot}%{_sbindir}/delete_user_cifs_all.py
ln -sf /opt/glustermg/%{release_version}/glustermg/scripts/setup_cifs_config_all.py %{buildroot}%{_sbindir}/setup_cifs_config_all.py
ln -sf /opt/glustermg/%{release_version}/glustermg/scripts/gmg-reset-password.sh %{buildroot}%{_sbindir}/gmg-reset-password.sh

mkdir -p $RPM_BUILD_ROOT/opt/glustermg/%{release_version}/backend
mkdir -p $RPM_BUILD_ROOT/var/lib/rrd
cp -pa gmg-scripts/* $RPM_BUILD_ROOT/opt/glustermg/%{release_version}/backend
%{__install} -d -m0755 %{buildroot}%{_initrddir}
ln -sf /opt/glustermg/%{release_version}/backend/multicast-discoverd.py %{buildroot}%{_sbindir}/multicast-discoverd
%{__install} -p -m0755 gmg-scripts/multicast-discoverd.init.d %{buildroot}%{_initrddir}/multicast-discoverd
ln -sf /opt/glustermg/%{release_version}/backend/gluster_cifs_volume_startup.py %{buildroot}%{_sbindir}/gluster_cifs_volume_startup
%{__install} -p -m0755 gmg-scripts/gluster-volume-settings.init.d %{buildroot}%{_initrddir}/gluster-volume-settings


%post
if [ -f /usr/share/tomcat6/webapps/glustermg ]; then
    rm -f /usr/share/tomcat6/webapps/glustermg
fi
ln -fs /opt/glustermg/%{release_version}/glustermg /usr/share/tomcat6/webapps/glustermg
if [ ! -f /opt/glustermg/keys/gluster.pem ]; then
x    ssh-keygen -t rsa -f /opt/glustermg/keys/gluster.pem -N ''
    mv -f /opt/glustermg/keys/gluster.pem.pub /opt/glustermg/keys/gluster.pub
fi
chown -R tomcat:tomcat /opt/glustermg /var/log/glustermg
if ! grep -q '^JAVA_HOME="/usr/lib/jvm/jre-1.6.0-openjdk.x86_64"' /etc/sysconfig/tomcat6; then
    sed -i 's/^JAVA_HOME=/# JAVA_HOME=/g' /etc/sysconfig/tomcat6
    echo 'JAVA_HOME="/usr/lib/jvm/jre-1.6.0-openjdk.x86_64"' >> /etc/sysconfig/tomcat6
fi
if ! grep -q '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' /etc/sysconfig/tomcat6; then
    echo '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> /etc/sysconfig/tomcat6
    echo 'JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> /etc/sysconfig/tomcat6
fi
if ! grep -q /usr/share/tomcat6/webapps/glustermg/ssl/gmg-ssl.keystore /etc/tomcat6/server.xml; then
    sed -i '/<\/Service>/i \
    <Connector SSLEnabled="true" \
               clientAuth="false" \
               executor="tomcatThreadPool" \
               maxThreads="150" \
               port="8443" \
               keystoreFile="/usr/share/tomcat6/webapps/glustermg/ssl/gmg-ssl.keystore" \
               keystorePass="gluster" \
               protocol="org.apache.coyote.http11.Http11Protocol" \
               scheme="https" \
               secure="true" \
               sslProtocol="TLS" />' /etc/tomcat6/server.xml
fi
if ! grep -q "org.apache.catalina.authenticator.NonLoginAuthenticator" /etc/tomcat6/context.xml; then
    sed -i '/<\/Context>/i \
    <Valve className="org.apache.catalina.authenticator.NonLoginAuthenticator" \
           disableProxyCaching="false" />' /etc/tomcat6/context.xml
fi
if wget -t 1 -T 1 -q -O /dev/null http://169.254.169.254/latest; then
    sed -i '/<constructor-arg value="multicast"/c <constructor-arg value="none" \/>' /opt/glustermg/%{release_version}/glustermg/WEB-INF/classes/spring/gluster-server-base.xml
fi

%preun
rm -f /usr/share/tomcat6/webapps/glustermg

%pre backend
modprobe -q fuse
if ! lsmod | grep -qw fuse; then
    echo "FATAL: fuse kernel module is not found."
    false
fi

%post backend
if [ -f /etc/sudoers ]; then
    chmod 644 /etc/sudoers
    sed -i '/^Defaults.*requiretty/d' /etc/sudoers
    chmod 0440 /etc/sudoers
fi
if ! grep -q rrd_cpu.pl /etc/crontab; then
    echo '*/5 * * * * root /opt/glustermg/%{release_version}/backend/rrd_cpu.pl' >> /etc/crontab
fi
if ! grep -q rrd_mem.pl /etc/crontab; then
    echo '*/5 * * * * root /opt/glustermg/%{release_version}/backend/rrd_mem.pl' >> /etc/crontab
fi
if ! grep -q rrd_net.pl /etc/crontab; then
    echo '*/5 * * * * root /opt/glustermg/%{release_version}/backend/rrd_net.pl' >> /etc/crontab
fi
/sbin/chkconfig --add multicast-discoverd
/sbin/chkconfig --level 345 multicast-discoverd on
if /etc/init.d/multicast-discoverd status >/dev/null; then
    /etc/init.d/multicast-discoverd restart
else
    /etc/init.d/multicast-discoverd start
fi
/etc/init.d/crond reload
/sbin/chkconfig smb on
/sbin/chkconfig --add gluster-volume-settings

%preun backend
if [ "$1" = 0 ] ; then
    /etc/init.d/multicast-discoverd stop
    /sbin/chkconfig --del multicast-discoverd
    /sbin/chkconfig --del gluster-volume-settings
fi


%clean
rm -rf $RPM_BUILD_ROOT

%files backend
%defattr(-,root,root,0755)
/opt/glustermg/%{release_version}/backend
/var/lib/rrd
%{_sbindir}/multicast-discoverd
%{_initrddir}/multicast-discoverd
%{_sbindir}/gluster_cifs_volume_startup
%{_initrddir}/gluster-volume-settings

%files
%defattr(-,root,root,0755)
/opt/glustermg/%{release_version}/glustermg
/opt/glustermg/keys
/opt/glustermg/etc
/var/log/glustermg
%{_bindir}/grun.py
%{_sbindir}/add_user_cifs_all.py
%{_sbindir}/delete_user_cifs_all.py
%{_sbindir}/setup_cifs_config_all.py
%{_sbindir}/gmg-reset-password.sh


%changelog
* Tue Nov 29 2011 Bala.FA <barumuga@redhat.com> - 1.1.0
- Updated tomcat6/samba dependency
- Added multicast-discoverd stop in preun backend
* Thu Aug  4 2011 Bala.FA <bala@gluster.com> - 1.0.0
- Initial release
