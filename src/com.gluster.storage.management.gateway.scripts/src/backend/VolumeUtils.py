#  Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Globals
import Utils


def readVolumeSmbConfFile(fileName=Globals.VOLUME_SMBCONF_FILE):
    entryList = []
    lines = Utils.readFile(fileName, lines=True)
    for line in lines:
        tokens = line.split("#")[0].strip().split(";")[0].strip().split("=")
        if len(tokens) != 2:
            continue
        if tokens[0].strip().upper() == "INCLUDE":
            entryList.append(tokens[1].strip())
    return entryList


def writeVolumeSmbConfFile(entryList, fileName=Globals.VOLUME_SMBCONF_FILE):
    try:
        fp = open(fileName, "w")
        for entry in entryList:
            fp.write("include = %s\n" % entry)
        fp.close()
        return True
    except IOError, e:
        Utils.log("Failed to write file %s: %s" % (fileName, str(e)))
        return False


def includeVolume(volumeName, fileName=Globals.VOLUME_SMBCONF_FILE):
    volumeFile = "%s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName)
    if not os.path.exists(volumeFile):
        return False
    entryList = readVolumeSmbConfFile(fileName)
    if volumeFile in entryList:
        return True
    entryList.append(volumeFile)
    return writeVolumeSmbConfFile(entryList, fileName)


def excludeVolume(volumeName, fileName=Globals.VOLUME_SMBCONF_FILE):
    volumeFile = "%s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName)
    if not os.path.exists(volumeFile):
        return False
    entryList = readVolumeSmbConfFile(fileName)
    if volumeFile not in entryList:
        return True
    entryList.remove(volumeFile)
    Utils.log("entryList = %s" % entryList)
    return writeVolumeSmbConfFile(entryList, fileName)


def writeVolumeCifsConfiguration(volumeName, userList, adminUser=None):
    volumeFile = "%s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName)
    try:
        fp = open(volumeFile, "w")
        fp.write("[%s]\n" % volumeName)
        fp.write("   comment = %s volume served by Gluster\n" % volumeName)
        fp.write("   path = %s/%s\n" % (Globals.CIFS_EXPORT_DIR, volumeName))
        fp.write("   guest ok = yes\n")
        fp.write("   public = yes\n")
        fp.write("   writable = yes\n")
        if adminUser:
            fp.write("   admin users = %s, %s\n" % (adminUser, ", ".join(userList)))
            fp.write("   valid users = %s, %s\n" % (adminUser, ", ".join(userList)))
        else:
            fp.write("   admin users = %s\n" % (", ".join(userList)))
            fp.write("   valid users = %s\n" % (", ".join(userList)))
        fp.close()
        return True
    except IOError, e:
        Utils.log("Failed to write file %s: %s" % (volumeFile, str(e)))
        return False


def removeVolumeCifsConfiguration(volumeName):
    volumeFile = "%s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName)
    try:
        os.remove(volumeFile)
        return True
    except OSError, e:
        Utils.log("Failed to remove file %s: %s" % (volumeFile, str(e)))
        return False

