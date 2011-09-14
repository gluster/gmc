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
import Disk
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


def readFile(fileName):
    lines = None
    try:
        fp = open(fileName)
        lines = fp.readlines()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (file, str(e)))
    return lines


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

def getMountInfo():
    mountInfo = {}
    for line in readFile("/proc/mounts"):
        str = line.strip()
        if str.startswith("/dev/"):
            tokens = str.split()
            device = {}
            mountPoint = tokens[1].strip()
            device["MountPoint"] = mountPoint
            device["FsType"] = tokens[2].strip()
            device["Uuid"] = getDiskPartitionUuid(tokens[0].strip())
            device["Status"] = "INITIALIZED"
            if mountPoint:
                if mountPoint in ["/", "/boot"]:
                    device["Type"] = "BOOT"
                else:
                    device["Type"] = "DATA"
            mountInfo[tokens[0].strip()] = device
    return mountInfo

def getRaidDisk():
    array = []
    arrayList = []
    mdFound = False
    
    try:
        fp = open("/proc/mdstat")
        for line in fp:
            str = line.strip()
            if str.startswith("md"):
                array.append(str)
                mdFound = True
                continue
            if mdFound:
                if str:
                    array.append(str)
                else:
                    arrayList.append(array)
                    array = []
                    mdFound = False
        fp.close()
    except IOError, e:
        return None
                
    raidList = {}
    for array in arrayList:
        raid = {}
        tokens = array[0].split()
        raid['Interface'] = tokens[3]
        device = getDevice(tokens[0])
        raid['MountPoint'] = getDeviceMountPoint(device)
        if raid['MountPoint']:
            raid['Type'] = "DATA"
            raid['SpaceInUse'] = getDeviceUsedSpace(device)
        else:
            raid['SpaceInUse'] = None
        rv = Utils.runCommand("blkid -c /dev/null %s" % (device), output=True, root=True)
        raid['Uuid'] = None
        raid['FsType'] = None
        raid['Status'] = "UNINITIALIZED"
        if isDiskInFormatting(device):
            raid['Status'] = "INITIALIZING"
        if not rv["Stderr"]:
            words = rv["Stdout"].strip().split()
            if words:
                raid['Status'] = "INITIALIZED"
            if len(words) > 2:
                raid['Uuid']  = words[1].split("UUID=")[-1].split('"')[1]
                raid['FsType'] = words[2].split("TYPE=")[-1].split('"')[1]
        raid['Disks'] = [x.split('[')[0] for x in tokens[4:]]
        raid['Size'] = float(array[1].split()[0]) / 1024.0
        raidList[tokens[0]] = raid
    return raidList


def getOsDisk():
    Utils.log("WARNING: getOsDisk() is deprecated by getRootPartition()")
    return getRootPartition()

def getAMIDiskInfo():
    diskInfo = {}
    diskList = []

    # In AMI instances, HAL does not provide the required information.
    # So that, the /proc/partitions is used to retrieve the required parameters.
    for line in readFile("/proc/partitions")[2:]:
        disk = {}
        tokens = line.split()
        # In Gluster-AMI instances supports (recommends) only raid disks.
        if tokens[3].startswith("md"):
            continue
        disk["Device"] = tokens[3]
        disk["Description"] = None
        disk["Size"] = long(tokens[2]) / 1024
        disk["Status"] = None
        disk["Interface"] = None
        disk["DriveType"] = None
        disk["Uuid"] = None
        disk["Init"] = False
        disk["Type"] = None
        disk["FsType"] = None
        disk["FsVersion"] = None
        disk["MountPoint"] = None
        disk["ReadOnlyAccess"] = None
        disk["SpaceInUse"] = None
        disk["Partitions"] = []
        diskList.append(disk)
    diskInfo["disks"] = diskList
    return diskInfo


def getDiskInfo(diskDeviceList=None):
    if Utils.runCommand("wget -t 1 -T 1 -q -O /dev/null %s" % Globals.AWS_WEB_SERVICE_URL) == 0:    # AMI instance
        return getAMIDiskInfo()

    diskDeviceList = getDevice(diskDeviceList)
    if Utils.isString(diskDeviceList):
        diskDeviceList = [diskDeviceList]

    dbusSystemBus = dbus.SystemBus()
    halObj = dbusSystemBus.get_object("org.freedesktop.Hal",
                                      "/org/freedesktop/Hal/Manager")
    halManager = dbus.Interface(halObj, "org.freedesktop.Hal.Manager")
    storageUdiList = halManager.FindDeviceByCapability("storage")

    diskInfo = {}
    diskList = []
    mountInfo = getMountInfo()
    for udi in storageUdiList: # on every disk storage
        halDeviceObj = dbusSystemBus.get_object("org.freedesktop.Hal", udi)
        halDevice = dbus.Interface(halDeviceObj,
                                   "org.freedesktop.Hal.Device")
        if halDevice.GetProperty("storage.drive_type") in ["cdrom", "floppy"] or \
                halDevice.GetProperty("block.is_volume"):
            continue
        disk = {}
        disk["Device"] = str(halDevice.GetProperty('block.device'))
        if diskDeviceList and disk["Device"] not in diskDeviceList:
            continue
        disk["Description"] = str(halDevice.GetProperty('storage.vendor')) + " " + str(halDevice.GetProperty('storage.model'))
        if halDevice.GetProperty('storage.removable'):
            disk["Size"] = long(halDevice.GetProperty('storage.removable.media_size'))
        else:
            disk["Size"] = long(halDevice.GetProperty('storage.size')) / 1024**2
        disk["Interface"] = str(halDevice.GetProperty('storage.bus'))
        disk["DriveType"] = str(halDevice.GetProperty('storage.drive_type'))
        disk["Status"] = None
        disk["Uuid"] = None
        disk["Init"] = False
        disk["Type"] = None
        disk["FsType"] = None
        disk["FsVersion"] = None
        disk["MountPoint"] = None
        disk["ReadOnlyAccess"] = None
        disk["SpaceInUse"] = None

        partitionUdiList = halManager.FindDeviceStringMatch("info.parent", udi)
        if isDiskInFormatting(disk["Device"]):
            disk["Status"] = "INITIALIZING"
        else:
            if partitionUdiList:
                disk["Status"] = "INITIALIZED"
            else:
                disk["Status"] = "UNINITIALIZED"
                disk["Type"] = "UNKNOWN"

        partitionList = []
        diskSpaceInUse = 0
        for partitionUdi in partitionUdiList:
            used = 0
            partitionHalDeviceObj = dbusSystemBus.get_object("org.freedesktop.Hal", partitionUdi)
            partitionHalDevice = dbus.Interface(partitionHalDeviceObj, "org.freedesktop.Hal.Device")
            if not partitionHalDevice.GetProperty("block.is_volume"):
                continue
            partitionDevice = str(partitionHalDevice.GetProperty('block.device'))
            if partitionHalDevice.GetProperty("volume.is_mounted"):
                rv = Utils.runCommand(["df", str(partitionHalDevice.GetProperty('volume.mount_point'))], output=True)
                if rv["Status"] == 0:
                    try:
                        used = long(rv["Stdout"].split("\n")[1].split()[2]) / 1024
                        diskSpaceInUse += used
                    except IndexError:
                        pass
                    except ValueError:
                        pass

            if disk["Device"] == partitionDevice:
                disk["Uuid"] = str(partitionHalDevice.GetProperty('volume.uuid'))
                disk["Init"] = True
                disk["Status"] = "INITIALIZED"
                mountPoint = str(partitionHalDevice.GetProperty('volume.mount_point'))
                if mountPoint:
                    if mountPoint in ["/", "/boot"]:
                        disk["Type"] = "BOOT"
                    else:
                        disk["Type"] = "DATA"
                disk["FsType"] = str(partitionHalDevice.GetProperty('volume.fstype'))
                if disk["FsType"] and "UNINITIALIZED" == disk["Status"]:
                    disk["Status"] = "INITIALIZED"
                disk["FsVersion"] = str(partitionHalDevice.GetProperty('volume.fsversion'))
                disk["MountPoint"] = str(partitionHalDevice.GetProperty('volume.mount_point'))
                disk["ReadOnlyAccess"] = str(partitionHalDevice.GetProperty('volume.is_mounted_read_only'))
                if not disk["Size"]:
                    disk["Size"] = long(partitionHalDevice.GetProperty('volume.size')) / 1024**2
                continue

            partition = {}
            partition["Init"] = False
            partition["Type"] = "UNKNOWN"            
            partition["Device"] = partitionDevice
            partition["Uuid"] = str(partitionHalDevice.GetProperty('volume.uuid'))
            partition["Size"] = long(partitionHalDevice.GetProperty('volume.size')) / 1024**2
            partition["FsType"] = str(partitionHalDevice.GetProperty('volume.fstype'))
            partition["FsVersion"] = str(partitionHalDevice.GetProperty('volume.fsversion'))
            partition["Label"] = str(partitionHalDevice.GetProperty('volume.label'))
            partition["MountPoint"] = str(partitionHalDevice.GetProperty('volume.mount_point'))
            partition["Size"] = long(partitionHalDevice.GetProperty('volume.size')) / 1024**2

            if isDiskInFormatting(partitionDevice):
                partition["Status"] = "INITIALIZING"
            else:
                if partition["FsType"]:
                    partition["Status"] = "INITIALIZED"
                else:
                    partition["Status"] = "UNINITIALIZED"

            partition["SpaceInUse"] = used
            if partition["MountPoint"] or isDataDiskPartitionFormatted(partitionDevice):
                partition["Init"] = True
                partition["Status"] = "INITIALIZED"
            if partition["MountPoint"]:
                if partition["MountPoint"] in ["/", "/boot"]:
                    partition["Type"] = "BOOT"
                else:
                    partition["Type"] = "DATA"
            else:
                if "SWAP" == partition["FsType"].strip().upper():
                    partition["Type"] = "SWAP"
            partition["ReadOnlyAccess"] = str(partitionHalDevice.GetProperty('volume.is_mounted_read_only'))
            partitionList.append(partition)
        disk["Partitions"] = partitionList
        if not disk["SpaceInUse"]:
            disk["SpaceInUse"] = diskSpaceInUse

        # In a paravirtualized server environment, HAL does not provide all the required information.
        # The missing details are replaced using /proc/mounts data or 'df' command.
        if not (mountInfo and mountInfo.has_key(disk["Device"])):
            diskList.append(disk)
            continue
        if not disk["Uuid"]:
            disk["Uuid"] = mountInfo[disk["Device"]]["Uuid"]
        if not disk["Type"]:
            disk["Type"] = mountInfo[disk["Device"]]["Type"]
        if not disk["Status"] or "UNKNOWN" == disk["Status"]:
            disk["Status"] = mountInfo[disk["Device"]]["Status"]
        if not disk["FsType"]:
            disk["FsType"] = mountInfo[disk["Device"]]["FsType"]
        if not disk["MountPoint"]:
            disk["MountPoint"] = mountInfo[disk["Device"]]["MountPoint"]
        if not disk["SpaceInUse"] and disk["MountPoint"]:
            disk["SpaceInUse"] = getDeviceUsedSpace(disk["Device"])
        else:
            disk["SpaceInUse"] = None
        diskList.append(disk)

    diskInfo["disks"] = diskList
    return diskInfo


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
        except IndexError:
            pass
        except ValueError:
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
    #if getDiskPartitionLabel(device) != Globals.DATA_PARTITION_LABEL:
    #    return False
    device = getDeviceName(device)
    diskObj = Disk.Disk()
    for disk in  diskObj.getMountableDiskList():
        if disk['device'].upper() == device.upper():
            mountPoint = disk['mount_point']
            if not mountPoint:
                return False
            if not os.path.exists(mountPoint):
                return False

    uuid = getUuidByDiskPartition(device)
    if not uuid:
        return False

    for fsTabEntry in FsTabUtils.readFsTab():
        if fsTabEntry["Device"] == ("UUID=%s" % uuid) and fsTabEntry["MountPoint"] == mountPoint:
            return True
    return False


def getDiskDom(diskDeviceList=None, bootPartition=None, skipDisk=None):
    diskDeviceList = getDevice(diskDeviceList)
    if Utils.isString(diskDeviceList):
        diskDeviceList = [diskDeviceList]

    if skipDisk:
        skipDisk = getDevice(skipDisk)
        if Utils.isString(skipDisk):
            skipDisk = [skipDisk]

    diskInfo = getDiskInfo(diskDeviceList)
    diskList = diskInfo["disks"]
    if not diskList:
        return None

    raidPartitions = {}
    raidDisk = getRaidDisk()
    
    for k, v in raidDisk.iteritems():
        for i in v['Disks']:
            raidPartitions[i] = k

    diskDom = Protocol.XDOM()
    disksTag = diskDom.createTag("disks", None)
    raidDisks = {}
    if not bootPartition:
        bootPartition = getRootPartition()
    for disk in diskList:
        if skipDisk and disk["Device"] in skipDisk:
            continue
        diskTag = diskDom.createTag("disk", None)
        diskDevice = getDeviceName(disk["Device"])
        diskTag.appendChild(diskDom.createTag("name", diskDevice))
        diskTag.appendChild(diskDom.createTag("description", disk["Description"]))
        diskTag.appendChild(diskDom.createTag("uuid", disk["Uuid"]))
        diskTag.appendChild(diskDom.createTag("status", disk["Status"]))
        diskTag.appendChild(diskDom.createTag("interface", disk["Interface"]))
        diskTag.appendChild(diskDom.createTag("type", disk["Type"]))
        diskTag.appendChild(diskDom.createTag("fsType", disk["FsType"]))
        diskTag.appendChild(diskDom.createTag("fsVersion", disk["FsVersion"]))
        diskTag.appendChild(diskDom.createTag("mountPoint", disk["MountPoint"]))
        diskTag.appendChild(diskDom.createTag("size", disk["Size"]))
        diskTag.appendChild(diskDom.createTag("spaceInUse", disk["SpaceInUse"]))
        partitionsTag = diskDom.createTag("partitions", None)
        if raidPartitions.has_key(diskDevice):
            rdList = {}
            rdList[diskDevice] = [deepcopy(diskTag)]
            if not raidDisks.has_key(raidPartitions[diskDevice]):
                raidDisks[raidPartitions[diskDevice]] = []
            raidDisks[raidPartitions[diskDevice]] += [rdList]
            continue
        for partition in disk["Partitions"]:
            partitionTag = diskDom.createTag("partition", None)
            device =  getDeviceName(partition["Device"])
            partitionTag.appendChild(diskDom.createTag("name", device))
            if partition["Uuid"]:
                partitionTag.appendChild(diskDom.createTag("uuid", partition["Uuid"]))
            else:
                partitionTag.appendChild(diskDom.createTag("uuid", getUuidByDiskPartition("/dev/" + device)))
            partitionTag.appendChild(diskDom.createTag("status", partition["Status"]))
            partitionTag.appendChild(diskDom.createTag("type", str(partition["Type"])))
            partitionTag.appendChild(diskDom.createTag("fsType", partition["FsType"]))
            partitionTag.appendChild(diskDom.createTag("mountPoint", partition['MountPoint']))
            partitionTag.appendChild(diskDom.createTag("size", partition["Size"]))
            partitionTag.appendChild(diskDom.createTag("spaceInUse", partition["SpaceInUse"]))
            if raidPartitions.has_key(device):
                tempPartitionTag = diskDom.createTag("partitions", None)
                if raidDisks.has_key(raidPartitions[device]):
                    rdList = raidDisks[raidPartitions[device]]
                    for rdItem in rdList:
                        if not rdItem.has_key(diskDevice):
                            rdItem[diskDevice] = [deepcopy(diskTag), tempPartitionTag]
                            rdItem[diskDevice][0].appendChild(tempPartitionTag)
                        rdItem[diskDevice][-1].appendChild(partitionTag)
                    continue
                rdList = {}
                rdList[diskDevice] = [deepcopy(diskTag), tempPartitionTag]
                tempPartitionTag.appendChild(partitionTag)
                rdList[diskDevice][0].appendChild(tempPartitionTag)
                raidDisks[raidPartitions[device]] = [rdList]
                continue
            partitionsTag.appendChild(partitionTag)
        diskTag.appendChild(partitionsTag)
        disksTag.appendChild(diskTag)

    for rdisk in raidDisk.keys():
        raidDiskTag = diskDom.createTag("disk", None)
        raidDiskTag.appendChild(diskDom.createTag("name", rdisk))
        raidDiskTag.appendChild(diskDom.createTag("description"))
        raidDiskTag.appendChild(diskDom.createTag("uuid", raidDisk[rdisk]['Uuid']))
        raidDiskTag.appendChild(diskDom.createTag("type", raidDisk[rdisk]['Type']))
        raidDiskTag.appendChild(diskDom.createTag("mountPoint", raidDisk[rdisk]['MountPoint']))
        raidDiskTag.appendChild(diskDom.createTag("status", raidDisk[rdisk]['Status']))
        raidDiskTag.appendChild(diskDom.createTag("interface", raidDisk[rdisk]['Interface']))
        raidDiskTag.appendChild(diskDom.createTag("fsType", raidDisk[rdisk]['FsType']))
        raidDiskTag.appendChild(diskDom.createTag("fsVersion"))
        raidDiskTag.appendChild(diskDom.createTag("size", raidDisk[rdisk]['Size']))
        raidDiskTag.appendChild(diskDom.createTag("spaceInUse", raidDisk[rdisk]['SpaceInUse']))
        raidDisksTag = diskDom.createTag("raidDisks", None)
        if raidDisks.has_key(rdisk):
            for item in raidDisks[rdisk]:
                for diskTag in item.values():
                    raidDisksTag.appendChild(diskTag[0])
        raidDiskTag.appendChild(raidDisksTag)
        disksTag.appendChild(raidDiskTag)
    diskDom.addTag(disksTag)
    return diskDom


def isDiskInFormatting(device):
    DEVICE_FORMAT_LOCK_FILE = "/var/lock/%s.lock" % device
    return os.path.exists(DEVICE_FORMAT_LOCK_FILE)


def isDiskInFormat(device):
    Utils.log("WARNING: isDiskInFormat() is deprecated by isDataDiskPartitionFormatted()")
    return isDataDiskPartitionFormatted(device)


def getDeviceMountPoint(device):
    try:
        fp = open("/proc/mounts")
        for token in [line.strip().split() for line in fp.readlines()]:
            if token and len(token) > 2 and token[0] == device:
                return token[1]
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % ("/proc/mounts", str(e)))
        return None
