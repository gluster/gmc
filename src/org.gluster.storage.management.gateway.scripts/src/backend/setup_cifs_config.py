#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import time
import Globals
import Utils

def main():
    try:
        if not os.path.exists(Globals.GLUSTER_BASE_DIR):
            os.mkdir(Globals.GLUSTER_BASE_DIR)
        if not os.path.exists(Globals.VOLUME_CONF_DIR):
            os.mkdir(Globals.VOLUME_CONF_DIR)
        if not os.path.exists(Globals.CIFS_EXPORT_DIR):
            os.mkdir(Globals.CIFS_EXPORT_DIR)
        if not os.path.exists(Globals.REEXPORT_DIR):
            os.mkdir(Globals.REEXPORT_DIR)
    except OSError, e:
        Utils.log("failed to create directory: %s" % str(e))
        sys.stderr.write("Failed to create directory: %s\n" % str(e))
        sys.exit(1)
    try:
        if not os.path.exists(Globals.VOLUME_SMBCONF_FILE):
            fp = open(Globals.VOLUME_SMBCONF_FILE, "w")
            fp.close()
    except IOError, e:
        Utils.log("Failed to create file %s: %s" % (Globals.VOLUME_SMBCONF_FILE, str(e)))
        sys.stderr.write("Failed to create file %s: %s\n" % (Globals.VOLUME_SMBCONF_FILE, str(e)))
        sys.exit(2)
    try:
        backupFile = "%s.%s" % (Globals.SAMBA_CONF_FILE, time.time())
        os.rename(Globals.SAMBA_CONF_FILE, backupFile)
    except IOError, e:
        Utils.log("Ignoring rename %s to %s: %s" % (Globals.SAMBA_CONF_FILE, backupFile))
        sys.stderr.write("Ignoring rename %s to %s: %s\n" % (Globals.SAMBA_CONF_FILE, backupFile))
    try:
        fp = open(Globals.SAMBA_CONF_FILE, "w")
        fp.write("##\n")
        fp.write("## THIS FILE SHOULD NOT BE MODIFIED.  IF YOU WANT TO MODIFY SAMBA\n")
        fp.write("## CONFIGURATIONS, USE /etc/samba/real.smb.conf FILE\n")
        fp.write("##\n")
        fp.write("include = %s\n\n" % Globals.REAL_SAMBA_CONF_FILE)
        fp.write("## CAUTION: DO NOT REMOVE BELOW LINE.  REMOVAL OF THE LINE DISABLES\n")
        fp.write("## CIFS REEXPORT OF GLUSTER VOLUMES\n")
        fp.write("include = %s\n" % Globals.VOLUME_SMBCONF_FILE)
        fp.close()
    except IOError, e:
        Utils.log("Failed to create samba configuration file %s: %s" % (Globals.SAMBA_CONF_FILE, str(e)))
        sys.stderr.write("Failed to create samba configuration file %s: %s\n" % (Globals.SAMBA_CONF_FILE, str(e)))
        sys.exit(3)
    try:
        if not os.path.exists(Globals.REAL_SAMBA_CONF_FILE):
            fp = open(Globals.REAL_SAMBA_CONF_FILE, "w")
            fp.write("[global]\n")
            fp.write("## CAUTION: DO NOT REMOVE BELOW INCLUDE LINE.  REMOVAL OF THE LINE\n")
            fp.write("## DISABLES SERVER/CIFS HIGH AVAILABILITY\n")
            #fp.write("include = %s\n" % Globals.CTDB_SAMBA_CONF_FILE)
            fp.write("##\n")
            fp.write("socket options = TCP_NODELAY IPTOS_LOWDELAY SO_SNDBUF=131072 SO_RCVBUF=131072\n")
            fp.write("read raw = yes\n")
            fp.write("server string = %h\n")
            fp.write("write raw = yes\n")
            fp.write("oplocks = yes\n")
            fp.write("max xmit = 131072\n")
            fp.write("dead time = 15\n")
            fp.write("getwd cache = yes\n")
            fp.write("#read size = 131072\n")
            fp.write("use sendfile=yes\n")
            fp.write("block size = 131072\n")
            fp.write("printcap name = /etc/printcap\n")
            fp.write("load printers = no\n")
            fp.close()
    except IOError, e:
        Utils.log("Failed to create samba configuration file %s: %s" % (Globals.REAL_SAMBA_CONF_FILE, str(e)))
        sys.stderr.write("Failed to create samba configuration file %s: %s\n" % (Globals.REAL_SAMBA_CONF_FILE, str(e)))
        sys.exit(4)

    if Utils.runCommand("/usr/sbin/selinuxenabled") == 0:
        if Utils.runCommand("setsebool -P samba_share_fusefs on") != 0:
            Utils.log("failed to set SELinux samba_share_fusefs")
            sys.stderr.write("failed to set SELinux samba_share_fusefs\n")
            sys.exit(5)

    if Utils.runCommand("service smb status") != 0:
        if Utils.runCommand("service smb start") != 0:
            Utils.log("failed to start smb service")
            sys.stderr.write("Failed to start smb service\n")
            sys.exit(6)

    if Utils.runCommand("service smb reload") != 0:
        Utils.log("failed to reload smb configuration")
        sys.stderr.write("Failed to reload smb configuration\n")
        sys.exit(7)
    sys.exit(0)


if __name__ == "__main__":
    main()
