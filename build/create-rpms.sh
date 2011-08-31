sudo cp glustermg-${VERSION}.tar.gz /usr/src/redhat/SOURCES
sudo rpmbuild --define "release_version ${VERSION}" -bb build/glustermg.spec
sudo rm -rf rpms
sudo mkdir rpms
sudo mv /usr/src/redhat/RPMS/x86_64/glustermg*.rpm rpms
sudo chown -R jenkins:jenkins rpms
