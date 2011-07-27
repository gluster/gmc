#  Copyright (C) 2010 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#
#  Gluster Storage Platform is free software; you can redistribute it
#  and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3 of
#  the License, or (at your option) any later version.
#
#  Gluster Storage Platform is distributed in the hope that it will be
#  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
#  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

MULTICAST_GROUP = '224.224.1.1'
MULTICAST_PORT  = 5353
GLUSTER_PLATFORM_VERSION = "3.2"

## System configuration constants
SYSCONFIG_NETWORK_DIR  = "/etc/sysconfig/network-scripts"
DNSMASQ_CONF_DIR       = "/etc/dnsmasq.d"

FSTAB_FILE             = "/etc/fstab"
NFS_EXPORTS_FILE       = "/etc/exports"
SAMBA_CONF_FILE        = "/etc/samba/smb.conf"
TIMEZONE_FILE          = "/etc/timezone"
ZONEINFO_DIR           = "/usr/share/zoneinfo"
LOCALTIME_FILE         = "/etc/localtime"
KERBEROS_CONF_FILE     = "/etc/krb5.conf"
NSSWITCH_CONF_FILE     = "/etc/nsswitch.conf"
NTP_CONF_FILE          = "/etc/ntp.conf"
MODPROBE_CONF_FILE     = "/etc/modprobe.d/bonding.conf"
SYSCONFIG_NETWORK_FILE = "/etc/sysconfig/network"
RESOLV_CONF_FILE       = "/etc/resolv.conf"
DNSMASQ_LEASE_FILE     = "/var/tmp/dnsmasq.leases"
LIVE_MODE_FILE         = "/etc/live"
ADD_SERVER_COMPLETED_FILE   = "/var/tmp/installation-completed"

DNSMASQ_DNS_CONF_FILE  = DNSMASQ_CONF_DIR + "/dns.conf"
DNSMASQ_DHCP_CONF_FILE = DNSMASQ_CONF_DIR + "/dhcp.conf"
##

## Base constants
MAX_PARTITION_SIZE = 16777216 # 16 TB
OS_PARTITION_SIZE  = 4000     # 4 GB
SESSION_TIMEOUT    = 1800     # 30 minutes
SERVER_AGENT_PORT  = 50000

BOOT_PARTITION_LABEL      = "GLUSTEROS"
DATA_PARTITION_LABEL      = "GLUSTERDATA"
VOLUME_USER_DESCRIPTION   = "Gluster Volume User"
SERVER_AGENT_RUN_USERNAME = "gluster"
INSTALLER_SERVER_NAME     = "$installer$"

GLUSTER_BASE_DIR         = "/GLUSTER"
GLUSTER_LUN_DIR          = "/data"
REEXPORT_DIR             = "/reexport"
NFS_EXPORT_DIR           = "/nfs"
CIFS_EXPORT_DIR          = "/cifs"
WEBDAV_DOCUMENT_ROOT_DIR = "/var/www/html"
UPDATES_DIR              = "/UPDATES"
TRANSPORT_HOME_DIR       = "/transport"
GLUSTERFS_LOG_DIR        = "/var/log/glusterfs"
LOG_DIR                  = "/var/log/glustermg"

GLUSTER_UPDATES_FILE        = "updates.xml"
INSTALLER_STATUS_FILE       = "/var/log/install-server-status.log"
INSTALL_PLATFORM_LOCK_FILE  = "/var/lock/install-gluster-platform.lock"
LAST_ACCESSED_NETWORK_FILE  = "last-accessed-network"
PREPARE_DATA_DISK_LOCK_FILE = "/var/tmp/prepare-data-disk.lock"
##

## Derived constants
GLUSTER_CONF_DIR   = GLUSTER_BASE_DIR + "/conf"
GLUSTER_TMP_DIR    = GLUSTER_BASE_DIR + "/tmp"
VOLUME_CONF_DIR    = GLUSTER_BASE_DIR + "/volumes"
SERVER_CONF_DIR    = GLUSTER_BASE_DIR + "/servers"
DNS_RECORDS_DIR    = GLUSTER_BASE_DIR + "/dns-records"
INSTALLER_CONF_DIR = SERVER_CONF_DIR + "/" + INSTALLER_SERVER_NAME

GSN_USER_INFO_FILE                  = GLUSTER_BASE_DIR + "/gsn-user.info"
GLUSTER_VERSION_FILE                = GLUSTER_BASE_DIR + "/version"
GLUSTER_UPDATE_SITE_FILE            = GLUSTER_BASE_DIR + "/update-site"
GLUSTER_DIRECTORY_SERVICE_CONF_FILE = GLUSTER_BASE_DIR + "/directory.xml"
GLUSTER_TIME_CONF_FILE              = GLUSTER_BASE_DIR + "/timeconfig.xml"
TRANSACTION_KEY_FILE                = GLUSTER_BASE_DIR + "/transaction.key"
SERVER_COUNT_FILE                   = GLUSTER_BASE_DIR + "/server-count"
SIGNATURE_FILE                      = GLUSTER_BASE_DIR + "/.signature"
GLUSTER_SERVER_POOL_FILE            = GLUSTER_BASE_DIR + "/pool"
GLUSTER_ADMIN_FILE                  = GLUSTER_BASE_DIR + "/.password"

VOLUME_SMBCONF_FILE = VOLUME_CONF_DIR + "/volumes.smbconf.list"

GLOBAL_NETWORK_FILE         = INSTALLER_CONF_DIR + "/network.xml"
INSTALL_SERVER_CONF_FILE    = INSTALLER_CONF_DIR + "/installer.xml"
INSTALLER_INFO_FILE         = INSTALLER_CONF_DIR + "/installer.info"
INSTALLED_SERVER_COUNT_FILE = INSTALLER_CONF_DIR + "/installed-server-count"

SESSION_FILE = GLUSTER_TMP_DIR + "/login.sessions"

GENERAL_LOG_FILE         = LOG_DIR + "/general.log"
INSTALLER_LOG_FILE       = LOG_DIR + "/installer.log"
PEER_AGENT_LOG_FILE    = LOG_DIR + "/peeragent.log"
SERVER_AGENT_LOG_FILE    = LOG_DIR + "/serveragent.log"
TRANSPORT_AGENT_LOG_FILE = LOG_DIR + "/transport.log"
##


## Global variables
## TODO: These should be removed
DOWNLOAD_GLUSTER_UPDATE_PROCESS = None
DOWNLOAD_GLUSTER_UPDATE_LEVEL = None
DOWNLOAD_GLUSTER_CURRENT_UPDATE_LEVEL = None
DOWNLOAD_GLUSTER_UPDATE_MD5SUM = None
REQUEST_MAP = {}
VERSION_DICTONARY = {}
##

AWS_WEB_SERVICE_URL = "http://169.254.169.254/latest"
