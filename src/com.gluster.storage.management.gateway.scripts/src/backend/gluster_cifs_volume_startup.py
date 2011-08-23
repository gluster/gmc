#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
import glob
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Globals
import Utils
import VolumeUtils

def getGlusterVolumeInfo(volumeName=None):
    volumeNameList = None
    if Utils.isString(volumeName):
        volumeNameList = [volumeName]
    if type(volumeName) == type([]):
        volumeNameList = volumeName

    status = Utils.runCommand("gluster volume info", output=True, root=True)
    if status["Status"] != 0:
        Utils.log("Failed to execute 'gluster volume info' command")
        return None

    volumeInfoDict = {}
    volumeInfo = {}
    volumeName = None
    brickList = []
    for line in status['Stdout'].split("\n"):
        if not line:
            if volumeName and volumeInfo:
                volumeInfo["Bricks"] = brickList
                volumeInfoDict[volumeName] = volumeInfo
                volumeInfo = {}
                volumeName = None
                brickList = []
            continue

        tokens = line.split(":")
        if tokens[0].strip().upper() == "BRICKS":
            continue
        elif tokens[0].strip().upper() == "VOLUME NAME":
            volumeName = tokens[1].strip()
            volumeInfo["VolumeName"] = volumeName
        elif tokens[0].strip().upper() == "TYPE":
            volumeInfo["VolumeType"] = tokens[1].strip()
        elif tokens[0].strip().upper() == "STATUS":
            volumeInfo["VolumeStatus"] = tokens[1].strip()
        elif tokens[0].strip().upper() == "TRANSPORT-TYPE":
            volumeInfo["TransportType"] = tokens[1].strip()
        elif tokens[0].strip().upper().startswith("BRICK"):
            brickList.append(":".join(tokens[1:]).strip())

    if volumeName and volumeInfo:
        volumeInfoDict[volumeName] = volumeInfo

    if not volumeNameList:
        return volumeInfoDict

    # remove unwanted volume info
    for volumeName in list(set(volumeInfoDict.keys()) - set(volumeNameList)):
        del volumeInfoDict[volumeName]

    return volumeInfoDict


def main():
    volumeInfo = getGlusterVolumeInfo()
    if not volumeInfo:
        print "No volume present.  Removing CIFS volume configuration if any"
        Utils.runCommand("rm -fr %s/*" % Globals.VOLUME_CONF_DIR, root=True, shell=True)
        Utils.runCommand("rm -fr %s/*" % Globals.REEXPORT_DIR, root=True, shell=True)
        Utils.runCommand("rm -fr %s/*" % Globals.CIFS_EXPORT_DIR, root=True, shell=True)
        sys.exit(0)

    try:
        fp = open(Globals.VOLUME_SMBCONF_FILE)
        lines = fp.readlines()
        fp.close()
    except IOError, e:
        Utils.log("Failed to samba volume configuration file %s: %s" % (Globals.VOLUME_SMBCONF_FILE, str(e)))
        sys.stderr.write("Failed to samba volume configuration file %s: %s\n" % (Globals.VOLUME_SMBCONF_FILE, str(e)))
        sys.exit(1)

    volumeSmbConfList = [line.strip() for line in lines]
    for volumeName in volumeInfo.keys():
        if not "include = %s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName) in volumeSmbConfList:
            continue
        if 'STOPPED' == volumeInfo[volumeName]['VolumeStatus'].upper():
            Utils.runCommand("rmdir %s/%s" % (Globals.CIFS_EXPORT_DIR, volumeName), root=True)
            if not VolumeUtils.excludeVolume(volumeName):
                Utils.log("Failed to exclude %s volume for CIFS reexport" % volumeName)
            continue
        if 'STARTED' == volumeInfo[volumeName]['VolumeStatus'].upper():
            volumeMountDirName = "%s/%s" % (Globals.REEXPORT_DIR, volumeName)
            if Utils.runCommand("mount -t glusterfs 127.0.0.1:%s %s" % (volumeName, volumeMountDirName)) != 0:
                Utils.log("Failed to mount volume %s" % (volumeName))

    smbConfFileList = glob.glob("%s/*.smbconf" % Globals.VOLUME_CONF_DIR)
    volumeList = [smbConfFileName.split(".smbconf")[0].split("/")[-1] for smbConfFileName in smbConfFileList]
    danglingVolumeList = list(set(volumeList).difference(set(volumeInfo.keys())))
    if not danglingVolumeList:
        sys.exit(0)

    print "Cleaning up dangling volume(s):", danglingVolumeList
    for volumeName in danglingVolumeList:
        Utils.runCommand("rmdir %s/%s" % (Globals.REEXPORT_DIR, volumeName), root=True)
        Utils.runCommand("rm -f %s/%s" % (Globals.CIFS_EXPORT_DIR, volumeName), root=True)
        if not VolumeUtils.excludeVolume(volumeName):
            Utils.log("Failed to exclude volume %s for CIFS reexport" % volumeName)
        Utils.runCommand("rm -f %s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName), root=True)

    sys.exit(0)

if __name__ == "__main__":
    main()
