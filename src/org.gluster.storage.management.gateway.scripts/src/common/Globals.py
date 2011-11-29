#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway (GlusterMG).
#
#  GlusterMG is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterMG is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
#

MULTICAST_GROUP = '224.224.1.1'
MULTICAST_PORT  = 24729
GLUSTER_PROBE_STRING = "GLUSTERPROBE"
GLUSTER_PROBE_VERSION = "1.0.0"
DEFAULT_BUFSIZE = 1024
SERVER_PORT = 24731
DEFAULT_BACKLOG = 5
DEFAULT_TIMEOUT = 3
DEFAULT_ID_LENGTH = 16
GLUSTER_PLATFORM_VERSION = "3.2"

## System configuration constants
SYSCONFIG_NETWORK_DIR  = "/etc/sysconfig/network-scripts"
FSTAB_FILE             = "/etc/fstab"
SAMBA_CONF_FILE        = "/etc/samba/smb.conf"
REAL_SAMBA_CONF_FILE   = "/etc/samba/real.smb.conf"
MODPROBE_CONF_FILE     = "/etc/modprobe.d/bonding.conf"
RESOLV_CONF_FILE       = "/etc/resolv.conf"
VOLUME_USER_DESCRIPTION   = "Gluster Volume User"
GLUSTER_BASE_DIR          = "/etc/glustermg"
REEXPORT_DIR              = "/reexport"
CIFS_EXPORT_DIR           = "/cifs"

## Derived constants
VOLUME_CONF_DIR    = GLUSTER_BASE_DIR + "/volumes"
VOLUME_SMBCONF_FILE = VOLUME_CONF_DIR + "/volumes.smbconf.list"

AWS_WEB_SERVICE_URL = "http://169.254.169.254/latest"
DEFAULT_UID = 1024000
CIFS_USER_FILE = "/opt/glustermg/etc/users.cifs"
CIFS_VOLUME_FILE  = "/opt/glustermg/etc/volumes.cifs"
