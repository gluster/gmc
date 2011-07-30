cp glustermg-1.0.0.tar.gz /usr/src/redhat/SOURCES
rpmbuild -bb build/glustermg.spec
rm -rf rpms
mkdir rpms
mv /usr/src/redhat/RPMS/x86_64/glustermg*.rpm rpms
