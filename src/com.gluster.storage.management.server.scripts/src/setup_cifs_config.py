#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
import Globals
import Utils

def main():
    try:
        os.mkdir(Globals.GLUSTER_BASE_DIR)
        os.mkdir(Globals.VOLUME_CONF_DIR)
        os.mkdir(Globals.CIFS_EXPORT_DIR)
        os.mkdir(Globals.REEXPORT_DIR)
    except OSError, e:
        Utils.log("failed to create directory: %s" % str(e))
        sys.exit(1)
    try:
        fp = open(Globals.VOLUME_SMBCONF_FILE, "w")
        fp.close()
    except IOError, e:
        Utils.log("Failed to create file %s: %s" % (Globals.VOLUME_SMBCONF_FILE, str(e)))
        sys.exit(2)
    try:
        os.rename(Globals.SAMBA_CONF_FILE, "%s.orig" % Globals.SAMBA_CONF_FILE)
    except IOError, e:
        Utils.log("Ignoring rename %s to %s: %s" % (Globals.SAMBA_CONF_FILE, "%s.orig" % Globals.SAMBA_CONF_FILE, str(e)))
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
        sys.exit(3)
    try:
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
        sys.exit(4)


    if Utils.runCommand("setsebool -P samba_share_fusefs on") != 0:
        Utils.log("failed to set SELinux samba_share_fusefs")
        sys.exit(5)

    if Utils.runCommand("service smb restart") != 0:
        Utils.log("failed to restart smb service")
        sys.exit(6)
    sys.exit(0)


if __name__ == "__main__":
    main()
