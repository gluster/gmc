cp glustermg-${VERSION}.tar.gz /usr/src/redhat/SOURCES
rpmbuild --define "release_version ${VERSION}" -bb build/glustermg.spec
rm -rf rpms
mkdir rpms
mv /usr/src/redhat/RPMS/x86_64/glustermg*.rpm rpms
chown -R jenkins:jenkins rpms
