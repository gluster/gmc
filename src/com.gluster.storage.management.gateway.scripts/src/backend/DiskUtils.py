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
import glob
from copy import deepcopy
import dbus
import Globals
import time
import Utils
import Protocol
import FsTabUtils

ONE_MB_SIZE = 1048576


def _stripDev(device):
    if Utils.isString(device) and device.startswith("/dev/"):
        return device[5:]
    return device


def _addDev(deviceName):
    if Utils.isString(deviceName) and not deviceName.startswith("/dev/"):
        return "/dev/" + deviceName
    return deviceName


def getDeviceName(device):
    if type(device) == type([]):
        nameList = []
        for d in device:
            nameList.append(_stripDev(d))
        return nameList
    return _stripDev(device)


def getDevice(deviceName):
    if Utils.isString(deviceName):
        return _addDev(deviceName)
    if type(deviceName) == type([]):
        nameList = []
        for d in deviceName:
            nameList.append(_addDev(d))
        return nameList
    return _addDev(deviceName)


def getDiskPartitionByUuid(uuid):
    uuidFile = "/dev/disk/by-uuid/%s" % uuid
    if os.path.exists(uuidFile):
        return getDeviceName(os.path.realpath(uuidFile))
    return None


def getUuidByDiskPartition(device):
    for uuidFile in glob.glob("/dev/disk/by-uuid/*"):
        if os.path.realpath(uuidFile) == device:
            return os.path.basename(uuidFile)
    return None


def getDiskPartitionUuid(partition):
    Utils.log("WARNING: getDiskPartitionUuid() is deprecated by getUuidByDiskPartition()")
    return getUuidByDiskPartition(partition)


def getDiskPartitionByLabel(label):
    ## TODO: Finding needs to be enhanced
    labelFile = "/dev/disk/by-label/%s" % label
    if os.path.exists(labelFile):
        if os.path.islink(labelFile):
            return getDeviceName(os.path.realpath(labelFile))
    return None


def getDeviceByLabel(label):
    Utils.log("WARNING: getDeviceByLabel() is deprecated by getDiskPartitionByLabel()")
    return getDiskPartitionByLabel(label)


def getDiskPartitionLabel(device):
    rv = Utils.runCommand("e2label %s" % device, output=True, root=True)
    if rv["Status"] == 0:
        return rv["Stdout"].strip()
    return False


def getRootPartition(fsTabFile=Globals.FSTAB_FILE):
    fsTabEntryList = FsTabUtils.readFsTab(fsTabFile)
    for fsTabEntry in fsTabEntryList:
        if fsTabEntry["MountPoint"] == "/":
            if fsTabEntry["Device"].startswith("UUID="):
                return getDiskPartitionByUuid(fsTabEntry["Device"].split("UUID=")[-1])
            if fsTabEntry["Device"].startswith("LABEL="):
                partitionName = getDiskPartitionByLabel(fsTabEntry["Device"].split("LABEL=")[-1])
                if partitionName:
                    return partitionName
            return getDeviceName(fsTabEntry["Device"])
    return None


def getOsDisk():
    Utils.log("WARNING: getOsDisk() is deprecated by getRootPartition()")
    return getRootPartition()

def getDiskInfo(diskNameList=None):
    procPartitionsDict = getProcPartitions()
    diskDict = {}
    for name, values in procPartitionsDict.iteritems():
        values["Description"] = None
        values["Uuid"] = None
        values["FsType"] = None
        values["MountPoint"] = None
        values["SpaceInUse"] = None
        values["Member"] = None
        ## extras ?!?!
        values["Init"] = False
        values["Status"] = None
        values["Interface"] = None
        values["DriveType"] = None
        values["Type"] = None
        values["FsVersion"] = None
        values["ReadOnlyAccess"] = None

        device = getDevice(name)
        values["Uuid"] = getUuidByDiskPartition(device)
        rv = Utils.runCommand("blkid -c /dev/null -o value %s" % device, output=True, root=True)
        if rv["Status"] == 0:
            lines = rv["Stdout"].strip().split("\n")
            values["FsType"] = lines[-1].strip()
            values["MountPoint"] = getDeviceMountPoint(device)
        if values["FsType"]:
            values["Init"] = True
        if values["MountPoint"]:
            rv = Utils.runCommand(["df", values["MountPoint"]], output=True)
            if rv["Status"] == 0:
                try:
                    values["SpaceInUse"] = long(rv["Stdout"].split("\n")[1].split()[2])
                except IndexError, e:
                    pass
                except ValueError, e:
                    pass
        if os.path.isdir("/sys/block/%s" % name):
            model = Utils.readFile("/sys/block/%s/device/model" % name)
            vendor = Utils.readFile("/sys/block/%s/device/vendor" % name)
            values["Description"] = "%s %s" % (model.strip(), vendor.strip())
            values["Partitions"] = {}
            diskDict[name] = values

    for diskName in diskDict.keys():
        del procPartitionsDict[diskName]
        for partName, values in procPartitionsDict.iteritems():
            if os.path.isdir("/sys/block/%s/%s" % (diskName, partName)):
                diskDict[diskName]["Partitions"][partName] = values

    procMdstatDict = getProcMdstat()
    for name, values in procMdstatDict.iteritems():
        try:
            diskDict[name]["Description"] = "Software Raid Array - %s - %s" % (values["Type"], values["Status"])
            diskDict[name]["Member"] = values["Member"]
        except KeyError, e:
            pass

    diskNameList = getDeviceName(diskNameList)
    if Utils.isString(diskNameList):
        diskNameList = [diskNameList]

    if not diskNameList:
        return diskDict

    outputDict = {}
    for diskName in list(set(diskDict.keys()).intersection(set(diskNameList))):
        outputDict[diskName] = diskDict[diskName]
    return outputDict


def getDiskList(diskDeviceList=None):
    return diskInfo["disks"]


def checkDiskMountPoint(diskMountPoint):
    try:
        fstabEntries = open(Globals.FSTAB_FILE).readlines()
    except IOError, e:
        fstabEntries = []
        Utils.log("failed to read file %s: %s" % (Globals.FSTAB_FILE, str(e)))
    found = False
    for entry in fstabEntries:
        entry = entry.strip()
        if not entry:
            continue
        entries = entry.split()
        if entries and len(entries) > 1 and entries[0].startswith("UUID=") and entries[1].upper() == diskMountPoint.upper():
            return True
    return False


def getMountPointByUuid(partitionUuid):
    # check uuid in etc/fstab
    try:
        fstabEntries = open(Globals.FSTAB_FILE).readlines()
    except IOError, e:
        fstabEntries = []
        Utils.log("failed to read file %s: %s" % (Globals.FSTAB_FILE, str(e)))
    found = False
    for entry in fstabEntries:
        entry = entry.strip()
        if not entry:
            continue
        if entry.split()[0] == "UUID=" + partitionUuid:
            return entry.split()[1]
    return None

def getDeviceUsedSpace(device):
    rv = Utils.runCommand("df -kl %s" % (device), output=True, root=True)
    if rv["Status"] == 0:
        try:
            return long(rv["Stdout"].split("\n")[1].split()[2]) / 1024
        except IndexError, e:
            pass
        except ValueError, e:
            pass

def getDiskSizeInfo(partition):
    # get values from df output
    total = None
    used = None
    free = None
    command = "df -kl -t ext3 -t ext4 -t xfs"
    rv = Utils.runCommand(command, output=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Status"] != 0:
        Utils.log("failed to get disk partition details")
        return None, None, None
    for line in rv["Stdout"].split("\n"):
        tokens = line.split()
        if len(tokens) < 4:
            continue
        if tokens[0] == partition:
            total = int(tokens[1]) / 1024.0
            used  = int(tokens[2]) / 1024.0
            free  = int(tokens[3]) / 1024.0
            break

    if total:
        return total, used, free
    
    # get total size from parted output
    for i in range(len(partition), 0, -1):
        pos = i - 1
        if not partition[pos].isdigit():
            break
    disk = partition[:pos+1]
    partitionNumber = partition[pos+1:]
    if not partitionNumber.isdigit():
        return None, None, None
    
    number = int(partitionNumber)
    command = "parted -ms %s unit kb print" % disk
    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Status"] != 0:
        Utils.log("failed to get disk partition details")
        return None, None, None
    
    lines = rv["Stdout"].split(";\n")
    if len(lines) < 3:
        return None,None,None
    
    for line in lines[2:]:
        tokens = line.split(':')
        if len(tokens) < 4:
            continue
        if tokens[0] == str(number):
            total = int(tokens[3].split('kB')[0]) / 1024.0
            break
    return total, used, free


def isDataDiskPartitionFormatted(device):
    rv = Utils.runCommand("blkid -c /dev/null -o value %s" % device, output=True, root=True)
    if rv["Status"] != 0:
        return False

    uuid = getUuidByDiskPartition(device)
    if not uuid:
        return False

    for fsTabEntry in FsTabUtils.readFsTab():
        if fsTabEntry["Device"] == ("UUID=%s" % uuid) or fsTabEntry["Device"] == device:
            return True
    return False


def isDiskInFormatting(device):
    DEVICE_FORMAT_LOCK_FILE = "/var/lock/%s.lock" % device
    return os.path.exists(DEVICE_FORMAT_LOCK_FILE)


def isDiskInFormat(device):
    Utils.log("WARNING: isDiskInFormat() is deprecated by isDataDiskPartitionFormatted()")
    return isDataDiskPartitionFormatted(device)


def getDeviceMountPoint(device):
    lines = Utils.readFile("/proc/mounts", lines=True)
    uuid = getUuidByDiskPartition(device)
    for line in lines:
        tokens = line.split()
        if tokens[0] == device or (uuid and tokens[0].endswith(uuid)):
            return tokens[1]
    return None

def getProcPartitions():
    procPartitionsDict = {}
    s = Utils.readFile("/proc/partitions", lines=True)
    for line in s[2:]:
        tokens = line.strip().split()
        procPartitionsDict[tokens[3]] = {"Size" : long(tokens[2])}
    return procPartitionsDict

def getProcMdstat():
    raidArrayDict = {}
    lines = Utils.readFile("/proc/mdstat", lines=True)
    for line in lines[1:]:
        tokens = line.strip().split()
        if not tokens:
            continue
        if tokens[0].startswith("md"):
            raidArrayDict[tokens[0]] = {"Status" : tokens[2], "Type" : tokens[3], "Member" : [token.split('[')[0] for token in tokens[4:]]}
    return raidArrayDict
