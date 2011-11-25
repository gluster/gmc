#!/bin/bash

#------------------------------------------------------------------
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of GlusterFS.
# 
# Gluster Management Gateway is free software; you can redistribute 
# it and/or modify it under the terms of the GNU General Public 
# License as published by the Free Software Foundation; either 
# version 3 of the License, or (at your option) any later version.
# 
# GlusterFS is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#------------------------------------------------------------------

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
