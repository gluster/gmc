%define glustermg_war_url http://build.gluster.com:8080/job/glustermg-package/lastSuccessfulBuild/artifact/glustermg.war.tar.gz

%define product_family Gluster Management Gateway
%define release_version 1.0.0

%define current_arch %{_arch}
%ifarch i386
%define current_arch x86
%endif

Summary:        %{product_family} web IU component
Name:           glustermg
Version:        %{release_version}
Release:        1%{?extra_release}
License:        Proprietary
Group:          System Environment/Base
Source0:        glustermg-%{release_version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Requires:       tomcat5 >= 5.5.23
Requires:       java-1.6.0-openjdk >= 1.6.0.0
Requires:       wget
%description
%{product_family} web UI component for GlusterFS and Gluster appliances

%package        backend
Summary:        %{product_family} server side backend tools
Group:          System Environment/Base
Requires:       python >= 2.4.3
Requires:       perl >= 5.8.8
Requires:       rrdtool-perl >= 1.2.27
Requires:       appliance-base >= 1.2
Requires:       sudo
Requires:       crontabs
%description    backend
%{product_family} server side backend tools


%prep
%setup -q -n glustermg-%{release_version}

%build

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/%{release_version}
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/keys
mkdir -p $RPM_BUILD_ROOT/opt/glustermg/data
mkdir -p $RPM_BUILD_ROOT/var/log/glustermg
wget -P $RPM_BUILD_ROOT %{glustermg_war_url}
tar -C $RPM_BUILD_ROOT/opt/glustermg/%{release_version} -zxf $RPM_BUILD_ROOT/glustermg.war.tar.gz
rm -f $RPM_BUILD_ROOT/glustermg.war.tar.gz

mkdir -p $RPM_BUILD_ROOT/opt/glustermg/%{release_version}/backend
mkdir -p $RPM_BUILD_ROOT/var/lib/rrd
cp -pa gmg-scripts/* $RPM_BUILD_ROOT/opt/glustermg/%{release_version}/backend

%post
chown -R tomcat:tomcat /opt/glustermg/data /var/log/glustermg
ln -fs /opt/glustermg/%{release_version}/glustermg /usr/share/tomcat5/webapps/glustermg
if [ ! -f /opt/glustermg/keys/id_rsa ]; then
    ssh-keygen -r rsa -f /opt/glustermg/keys/id_rsa -N gluster
fi
if ! grep -q '^JAVA_HOME="/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64"' /etc/sysconfig/tomcat5; then
    sed -i 's/^JAVA_HOME=/# JAVA_HOME=/g' /etc/sysconfig/tomcat5
    echo 'JAVA_HOME="/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64"' >> /etc/sysconfig/tomcat5
fi
if ! grep -q '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' /etc/sysconfig/tomcat5; then
    echo '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> /etc/sysconfig/tomcat5
    echo 'JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> /etc/sysconfig/tomcat5
fi
if ! grep -q /usr/share/tomcat5/webapps/glustermg/ssl/gmg-ssl.keystore /etc/tomcat5/server.xml; then
    sed '/<\/Service>/i \
    <Connector SSLEnabled="true" \
               clientAuth="false" \
               executor="tomcatThreadPool" \
               maxThreads="150" \
               port="8443" \
               keystoreFile="/usr/share/tomcat5/webapps/glustermg/ssl/gmg-ssl.keystore" \
               keystorePass="gluster" \
               protocol="org.apache.coyote.http11.Http11Protocol" \
               scheme="https" \
               secure="true" \
               sslProtocol="TLS" />' /etc/tomcat5/server.xml
fi
if ! grep -q "org.apache.catalina.authenticator.NonLoginAuthenticator" /etc/tomcat5/context.xml; then
    sed '/<\/Context>/i \
    <Valve className="org.apache.catalina.authenticator.NonLoginAuthenticator" \
           disableProxyCaching="false" />' /etc/tomcat5/context.xml
fi
if wget -q -O /dev/null http://169.254.169.254/latest; then
    sed -i '/<constructor-arg/c <constructor-arg value="none" \/>' /opt/glustermg/%{release_version}/glustermg/WEB-INF/classes/spring/gluster-server-base.xml
else
    sed -i '/<constructor-arg/c <constructor-arg value="multicast" \/>' /opt/glustermg/%{release_version}/glustermg/WEB-INF/classes/spring/gluster-server-base.xml
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


%clean
rm -rf $RPM_BUILD_ROOT

%files backend
%defattr(-,root,root,0755)
/opt/glustermg/%{release_version}/backend
/var/lib/rrd

%files
%defattr(-,root,root,0755)
/opt/glustermg/%{release_version}/glustermg
/opt/glustermg/keys
/opt/glustermg/data
/var/log/glustermg

%changelog
* Mon Jul 25 2011 Bala.FA <bala@gluster.com> - 1.0.0
- Initial release
