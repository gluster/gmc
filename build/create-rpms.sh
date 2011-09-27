FILE_ERR=1
RPM_ERR=2
sudo cp glustermg-backend-${VERSION}.tar.gz /usr/src/redhat/SOURCES || exit ${FILE_ERR}
sudo cp glustermg-${VERSION}.war.tar.gz /usr/src/redhat/SOURCES || exit ${FILE_ERR}
sudo rpmbuild --define "release_version ${VERSION}" -bb build/glustermg.spec || exit ${RPM_ERR}
sudo rm -rf rpms || exit ${FILE_ERR}
sudo mkdir rpms || exit ${FILE_ERR}
sudo mv /usr/src/redhat/RPMS/x86_64/glustermg*.rpm rpms || exit ${FILE_ERR}
sudo chown -R jenkins:jenkins rpms || exit ${FILE_ERR}
sudo rm -f /usr/src/redhat/SOURCES/glustermg-backend-${VERSION}.tar.gz /usr/src/redhat/SOURCES/glustermg-${VERSION}.war.tar.gz
