#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
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
DNSMASQ_LEASE_FILE     = "/var/tmp/dnsmasq.leases"
LIVE_MODE_FILE         = "/etc/live"
DNSMASQ_CONF_DIR       = "/etc/dnsmasq.d"
DNSMASQ_DHCP_CONF_FILE = DNSMASQ_CONF_DIR + "/dhcp.conf"
DATA_PARTITION_LABEL      = "GLUSTERDATA"
VOLUME_USER_DESCRIPTION   = "Gluster Volume User"
GLUSTER_BASE_DIR          = "/etc/glustermg"
REEXPORT_DIR              = "/reexport"
CIFS_EXPORT_DIR           = "/cifs"
GLUSTER_UPDATES_FILE      = "updates.xml"
INSTALLER_SERVER_NAME     = "$installer$"

## Derived constants
GLUSTER_CONF_DIR   = GLUSTER_BASE_DIR + "/conf"
GLUSTER_TMP_DIR    = GLUSTER_BASE_DIR + "/tmp"
VOLUME_CONF_DIR    = GLUSTER_BASE_DIR + "/volumes"
SERVER_CONF_DIR    = GLUSTER_BASE_DIR + "/servers"
DNS_RECORDS_DIR    = GLUSTER_BASE_DIR + "/dns-records"
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
INSTALLER_CONF_DIR  = SERVER_CONF_DIR + "/" + INSTALLER_SERVER_NAME
VOLUME_SMBCONF_FILE = VOLUME_CONF_DIR + "/volumes.smbconf.list"
GLOBAL_NETWORK_FILE         = INSTALLER_CONF_DIR + "/network.xml"
INSTALLED_SERVER_COUNT_FILE = INSTALLER_CONF_DIR + "/installed-server-count"

AWS_WEB_SERVICE_URL = "http://169.254.169.254/latest"
DEFAULT_UID = 1024000
CIFS_USER_FILE = "/opt/glustermg/etc/users.cifs"
CIFS_VOLUME_FILE  = "/opt/glustermg/etc/volumes.cifs"
