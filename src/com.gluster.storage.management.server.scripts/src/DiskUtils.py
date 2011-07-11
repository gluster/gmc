#  Copyright (c) 2010 Gluster, Inc. <http://www.gluster.com>
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

import os
import glob
from copy import deepcopy
import dbus
import Globals
import time
import Utils
import Disk
import Protocol
from FsTabUtils import *

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
    rv = Utils.runCommandFG(["sudo", "e2label", device], stdout=True)
    if rv["Status"] == 0:
        return rv["Stdout"].strip()
    return False


def getRootPartition(fsTabFile=Globals.FSTAB_FILE):
    fsTabEntryList = readFsTab(fsTabFile)
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
            if "/export/" in raid['MountPoint']:
                raid['Type'] = "DATA"
            else:
                raid['Type'] = "BOOT"
        else:
            raid['Type'] = "UNKNOWN"
        rv = Utils.runCommand("blkid -c /dev/null %s" % (device), output=True, root=True)
        raid['Uuid'] = None
        raid['FsType'] = None
        raid['Status'] = "UNINITIALIZED"
        if isDiskInFormatting(device):
            raid['Status'] = "INITIALIZING"
        if not rv["Stderr"]:
            words = rv["Stdout"].strip().split()
            if words:
                raid['Type'] = "INITIALIZED"
            if len(words) > 2:
                raid['Uuid']  = words[1].split("UUID=")[-1].split('"')[1]
                raid['FsType'] = words[2].split("TYPE=")[-1].split('"')[1]

        rv = Utils.runCommand("df %s" % (device), output=True, root=True)
        if rv["Status"] == 0:
            try:
                used = long(rv["Stdout"].split("\n")[1].split()[2]) / 1024
            except IndexError:
                pass
            except ValueError:
                pass
        raid['SpaceInUse'] = used
        raid['Disks'] = [x.split('[')[0] for x in tokens[4:]]
        raid['Size'] = float(array[1].split()[0]) / 1024.0
        raidList[tokens[0]] = raid
    return raidList


def getOsDisk():
    Utils.log("WARNING: getOsDisk() is deprecated by getRootPartition()")
    return getRootPartition()


def getDiskInfo(diskDeviceList=None):
    diskDeviceList = getDevice(diskDeviceList)
    if Utils.isString(diskDeviceList):
        diskDeviceList = [diskDeviceList]

    if Utils.runCommand("/usr/bin/lshal") != 0:
        Utils.log("failed running /usr/bin/lshal")

    dbusSystemBus = dbus.SystemBus()
    halObj = dbusSystemBus.get_object("org.freedesktop.Hal",
                                      "/org/freedesktop/Hal/Manager")
    halManager = dbus.Interface(halObj, "org.freedesktop.Hal.Manager")
    storageUdiList = halManager.FindDeviceByCapability("storage")

    diskInfo = {}
    diskList = []
    for udi in storageUdiList:
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
        disk["Uuid"] = None
        disk["Init"] = False
        disk["Type"] = None
        disk["FsType"] = None
        disk["FsVersion"] = None
        disk["MountPoint"] = None
        disk["ReadOnlyAccess"] = None
        disk["SpaceInUse"] = None

        partitionList = []
        partitionUdiList = halManager.FindDeviceStringMatch("info.parent", udi)
        disk["Status"] = "UNINITIALIZED"
        if isDiskInFormatting(disk["Device"]):
            disk["Status"] = "INITIALIZING"
        else:
            if partitionUdiList:
                disk["Status"] = "INITIALIZED"
            else:
                disk["Status"] = "UNINITIALIZED"
        diskSpaceInUse = 0
        for partitionUdi in partitionUdiList:
            used = 0
            partitionHalDeviceObj = dbusSystemBus.get_object("org.freedesktop.Hal",
                                                             partitionUdi)
            partitionHalDevice = dbus.Interface(partitionHalDeviceObj,
                                                "org.freedesktop.Hal.Device")
            if not partitionHalDevice.GetProperty("block.is_volume"):
                continue
            partitionDevice = str(partitionHalDevice.GetProperty('block.device'))
            if partitionHalDevice.GetProperty("volume.is_mounted"):
                rv = Utils.runCommandFG(["df", str(partitionHalDevice.GetProperty('volume.mount_point'))], stdout=True)
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
                disk["Init"] = True # TODO: use isDataDiskPartitionFormatted function to cross verify this
                disk["Status"] = "INITIALIZED"
                mountPoint = str(partitionHalDevice.GetProperty('volume.mount_point'))
                if mountPoint:
                    if "/export/" in mountPoint:
                        disk["Type"] = "DATA"
                    else:
                        disk["Type"] = "BOOT"
                else:
                    disk["Type"] = "UNKNOWN"
                disk["FsType"] = str(partitionHalDevice.GetProperty('volume.fstype'))
                if disk["FsType"] and "UNINITIALIZED" == disk["Status"]:
                    disk["Status"] = "INITIALIZED"
                disk["FsVersion"] = str(partitionHalDevice.GetProperty('volume.fsversion'))
                disk["MountPoint"] = str(partitionHalDevice.GetProperty('volume.mount_point'))
                disk["ReadOnlyAccess"] = str(partitionHalDevice.GetProperty('volume.is_mounted_read_only'))
                if not disk["Size"]:
                    disk["Size"] = long(partitionHalDevice.GetProperty('volume.size')) / 1024**2
                disk["SpaceInUse"] = used
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
                #partition["Status"] = "INITIALIZED"
            if partition["MountPoint"]:
                if "/export/" in partition["MountPoint"]:
                    partition["Type"] = "DATA"
                else:
                    partition["Type"] = "BOOT"
            partition["ReadOnlyAccess"] = str(partitionHalDevice.GetProperty('volume.is_mounted_read_only'))
            partitionList.append(partition)
        disk["Partitions"] = partitionList
        if not disk["SpaceInUse"]:
            disk["SpaceInUse"] = diskSpaceInUse
        diskList.append(disk)
    diskInfo["disks"] = diskList
    return diskInfo

def getDiskList(diskDeviceList=None):
    return diskInfo["disks"]

def readFsTab(fsTabFile=Globals.FSTAB_FILE):
    try:
        fsTabfp = open(fsTabFile)
    except IOError, e:
        Utils.log("readFsTab(): " + str(e))
        return None
    
    fsTabEntryList = []
    for line in fsTabfp:
        tokens = line.strip().split()
        if not tokens or tokens[0].startswith('#'):
            continue
        fsTabEntry = {}
        fsTabEntry["Device"] = None
        fsTabEntry["MountPoint"] = None
        fsTabEntry["FsType"] = None
        fsTabEntry["Options"] = None
        fsTabEntry["DumpOption"] = 0
        fsTabEntry["fsckOrder"] = 0
        try:
            fsTabEntry["Device"] = tokens[0]
            fsTabEntry["MountPoint"] = tokens[1]
            fsTabEntry["FsType"] = tokens[2]
            fsTabEntry["Options"] = tokens[3]
            fsTabEntry["DumpOption"] = tokens[4]
            fsTabEntry["fsckOrder"] = tokens[5]
        except IndexError:
            pass
        if fsTabEntry["Device"] and fsTabEntry["MountPoint"] and fsTabEntry["FsType"] and fsTabEntry["Options"]:
            fsTabEntryList.append(fsTabEntry)
    fsTabfp.close()
    return fsTabEntryList
        

def checkDiskMountPoint(diskMountPoint):
    try:
        fstabEntries = open(Globals.FSTAB_FILE).readlines()
    except IOError:
        fstabEntries = []
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
    except IOError:
        fstabEntries = []
    found = False
    for entry in fstabEntries:
        entry = entry.strip()
        if not entry:
            continue
        if entry.split()[0] == "UUID=" + partitionUuid:
            return entry.split()[1]
    return None


def getDiskSizeInfo(partition):
    # get values from df output
    total = None
    used = None
    free = None
    command = "df -kl -t ext3 -t ext4 -t xfs"
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        Utils.log("failed to get disk details. %s" % Utils.stripEmptyLines(rv["Stdout"]))
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
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        Utils.log("failed to get disk details. %s" % Utils.stripEmptyLines(rv["Stdout"]))
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
    #Todo: Proper label needs to be added for data partition
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

    for fsTabEntry in readFsTab():
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

    #for partition in raidDisk.values():
    #    raidDiskPartitions += partition['disks']

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

        if not disk["Partitions"]:
            diskTag.appendChild(diskDom.createTag("type", disk["Type"]))
            #diskTag.appendChild(diskDom.createTag("init", str(disk["Init"]).lower()))
            diskTag.appendChild(diskDom.createTag("fsType", disk["FsType"]))
            diskTag.appendChild(diskDom.createTag("fsVersion", disk["FsVersion"]))
            diskTag.appendChild(diskDom.createTag("mountPoint", disk["MountPoint"]))

        diskTag.appendChild(diskDom.createTag("size", disk["Size"]))
        diskTag.appendChild(diskDom.createTag("spaceInUse", disk["SpaceInUse"]))
        partitionsTag = diskDom.createTag("partitions", None)
        if raidPartitions.has_key(diskDevice):
            rdList = {}
            rdList[diskDevice] = [deepcopy(diskTag)]
            raidDisks[raidPartitions[diskDevice]] = rdList
            continue
        for partition in disk["Partitions"]:
            partitionTag = diskDom.createTag("partition", None)
            device =  getDeviceName(partition["Device"])
            partitionTag.appendChild(diskDom.createTag("name", device))
            if partition["Uuid"]: #TODO: Move this verification and findings to getDiskInfo function
                partitionTag.appendChild(diskDom.createTag("uuid", partition["Uuid"]))
            else:
                partitionTag.appendChild(diskDom.createTag("uuid", getUuidByDiskPartition("/dev/" + device)))
            partitionTag.appendChild(diskDom.createTag("status", partition["Status"]))
            #partitionTag.appendChild(diskDom.createTag("init", str(partition["Init"]).lower()))
            partitionTag.appendChild(diskDom.createTag("type", str(partition["Type"])))
            partitionTag.appendChild(diskDom.createTag("fsType", partition["FsType"]))
            partitionTag.appendChild(diskDom.createTag("mountPoint", partition['MountPoint']))
            partitionTag.appendChild(diskDom.createTag("size", partition["Size"]))
            partitionTag.appendChild(diskDom.createTag("spaceInUse", partition["SpaceInUse"]))
            if raidPartitions.has_key(device):
                tempPartitionTag = diskDom.createTag("partitions", None)
                if raidDisks.has_key(raidPartitions[device]):
                    rdList = raidDisks[raidPartitions[device]]
                    if not rdList.has_key(diskDevice):
                        rdList[diskDevice] = [deepcopy(diskTag), tempPartitionTag]
                        rdList[diskDevice][0].appendChild(tempPartitionTag)
                    rdList[diskDevice][-1].appendChild(partitionTag)
                    continue
                rdList = {}
                rdList[diskDevice] = [deepcopy(diskTag), tempPartitionTag]
                tempPartitionTag.appendChild(partitionTag)
                rdList[diskDevice][0].appendChild(tempPartitionTag)
                raidDisks[raidPartitions[device]] = rdList
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
            for diskTag in raidDisks[rdisk].values():
                raidDisksTag.appendChild(diskTag[0])
        raidDiskTag.appendChild(raidDisksTag)
        disksTag.appendChild(raidDiskTag)
    diskDom.addTag(disksTag)
    return diskDom


def initializeDisk(disk, boot=False, startSize=0, sudo=False):
    if boot and startSize > 0:
        return False

    disk = getDevice(disk)
    diskObj = getDiskList(disk)[0]

    if boot or startSize == 0:
        command = "dd if=/dev/zero of=%s bs=1024K count=1" % diskObj["Device"]
        if runCommandFG(command, root=sudo) != 0:
            if boot:
                Utils.log("failed to clear boot sector of disk %s" % diskObj["Device"])
                return False
            Utils.log("failed to clear boot sector of disk %s.  ignoring" % diskObj["Device"])

        command = "parted -s %s mklabel gpt" % diskObj["Device"]
        if runCommandFG(command, root=sudo) != 0:
            return False

    if boot:
        command = "parted -s %s mkpart primary ext3 0MB %sMB" % (diskObj["Device"], Globals.OS_PARTITION_SIZE)
        if runCommandFG(command, root=sudo) != 0:
            return False
        command = "parted -s %s set 1 boot on" % (diskObj["Device"])
        if runCommandFG(command, root=sudo) != 0:
            return False
        startSize = Globals.OS_PARTITION_SIZE

    size = (diskObj["Size"] / ONE_MB_SIZE) - startSize
    while size > Globals.MAX_PARTITION_SIZE:
        endSize = startSize + Globals.MAX_PARTITION_SIZE
        command = "parted -s %s mkpart primary ext3 %sMB %sMB" % (diskObj["Device"], startSize, endSize)
        if runCommandFG(command, root=sudo) != 0:
            return False
        size -= Globals.MAX_PARTITION_SIZE
        startSize = endSize

    if size:
        command = "parted -s %s mkpart primary ext3 %sMB 100%%" % (diskObj["Device"], startSize)
        if runCommandFG(command, root=sudo) != 0:
            return False

    if runCommandFG("udevadm settle", root=sudo) != 0:
        if runCommandFG("udevadm settle", root=sudo) != 0:
            Utils.log("udevadm settle for disk %s failed.  ignoring" % diskObj["Device"])
    time.sleep(1)

    if runCommandFG("partprobe %s" % diskObj["Device"], root=sudo) != 0:
        Utils.log("partprobe %s failed" % diskObj["Device"])
        return False

    if runCommandFG("gptsync %s" % diskObj["Device"], root=sudo) != 0:
        Utils.log("gptsync %s failed.  ignoring" % diskObj["Device"])

    # wait forcefully to appear devices in /dev
    time.sleep(2)
    return True


def initializeOsDisk(diskObj):
    Utils.log("WARNING: initializeOsDisk() is deprecated by initializeDisk(boot=True)")
    return initializeDisk(diskObj, boot=True)


def initializeDataDisk(diskObj):
    Utils.log("WARNING: initializeDataDisk() is deprecated by initializeDisk()")
    return initializeDisk(diskObj)

def getBootPartition(serverName):
    diskDom = XDOM()
    diskDom.parseFile("%s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))
    if not diskDom:
        return None
    partitionDom  = XDOM()
    partitionUuid = None
    partitionName = None
    for partitionTag in diskDom.getElementsByTagRoute("disk.partition"):
        partitionDom.setDomObj(partitionTag)
        boot = partitionDom.getTextByTagRoute("boot")
        if boot and boot.strip().upper() == 'YES':
            partitionUuid = partitionDom.getTextByTagRoute("uuid")
            partitionName = partitionDom.getTextByTagRoute("device")
            break
    if not (partitionUuid and partitionName):
        return None
     
    # check device label name
    deviceBaseName = os.path.basename(partitionName)
    process = runCommandBG(['sudo', 'e2label', partitionName])
    if type(process) == type(True):
        return None
    if process.wait() != 0:
        return None
    output = process.communicate()
    deviceLabel = output[0].split()[0]
    if deviceLabel != Globals.BOOT_PARTITION_LABEL:
        return None

    # check uuid in etc/fstab
    try:
        fstabEntries = open(Globals.FSTAB_FILE).readlines()
    except IOError:
        fstabEntries = []
    found = False
    for entry in fstabEntries:
        entry = entry.strip()
        if not entry:
            continue
        if entry.split()[0] == "UUID=" + partitionUuid:
            found = True
            break
    if not found:
        return None
    return partitionName


def isDiskInFormatting(device):
    DEVICE_FORMAT_LOCK_FILE = "/var/lock/%s.lock" % device
    return os.path.exists(DEVICE_FORMAT_LOCK_FILE)


def isDiskInFormat(device):
    Utils.log("WARNING: isDiskInFormat() is deprecated by isDataDiskPartitionFormatted()")
    return isDataDiskPartitionFormatted(device)


def diskOrder(serverExportList):
    newServerExportList = []
    while serverExportList:
        serverExport = deepcopy(serverExportList[0])
        if newServerExportList and serverExport.split(":")[0] == newServerExportList[-1].split(":")[0]:
            inserted = False
            for i in range(0, len(newServerExportList) - 1):
                if serverExport.split(":")[0] == newServerExportList[i].split(":")[0]:
                    continue
                if i == 0:
                    newServerExportList.insert(i, serverExport)
                    inserted = True
                    break
                if serverExport.split(":")[0] == newServerExportList[i - 1].split(":")[0]:
                    continue
                newServerExportList.insert(i, serverExport)
                inserted = True
                break
            if not inserted:
                newServerExportList.append(serverExport)
        else:
            newServerExportList.append(serverExport)
        serverExportList.remove(serverExport)
        i = 0
        while serverExportList and i < len(serverExportList):
            if serverExport.split(":")[0] == serverExportList[i].split(":")[0]:
                i += 1
                continue
            serverExport = deepcopy(serverExportList[i])
            newServerExportList.append(serverExport)
            serverExportList.remove(serverExport)
    return newServerExportList


def updateServerDiskConfig(serverName, diskDom, requestFlag=True, partitionFlag=True):
    command = "command.server."
    if not requestFlag:
        command = ""
    diskList = {}
    for tagE in diskDom.getElementsByTagRoute(command + "disk"):
        diskList[diskDom.getTextByTagRoute(command + "device")] = tagE
    configDom = XDOM()
    if not configDom.parseFile("%s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName)):
        return diskDom.writexml("%s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))
    diskTag = configDom.getElementsByTagRoute("disks.disk")
    disks = configDom.getElementsByTagRoute("disks")
    if not (diskTag or disks):
        return None
    for tagE in diskTag:
        diskDom = XDOM()
        diskDom.setDomObj(tagE)
        device = diskDom.getTextByTagRoute("device")
        if partitionFlag and device in diskList:
            disks[0].removeChild(tagE)
            disks[0].appendChild(deepcopy(diskList[device]))
            continue
        if not partitionFlag and device in diskList:
            partitionList = []
            for childNodeTag in tagE.childNodes:
                if childNodeTag.nodeName == 'partition':
                    partitionList.append(childNodeTag)
            tagE.childNodes = []
            tagE.childNodes = diskList[device].childNodes + partitionList
    return configDom.writexml("%s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))


def compareDisksDom(diskDomA, diskDomB, requestFlag=True):
    command = "command.server.disk."
    if not requestFlag:
        command = ""
    sourceDiskList = {}
    sourceDisk = {}
    for tagE in diskDomA.getElementsByTagRoute("disk"):
        sourceDisk["description"] = diskDomA.getTextByTagRoute("description")
        sourceDisk["size"] = diskDomA.getTextByTagRoute("size")
        sourceDisk["init"] = diskDomA.getTextByTagRoute("init")
        sourceDisk["interface"] = diskDomA.getTextByTagRoute("interface")
        sourceDiskList[diskDomA.getTextByTagRoute("device")] = sourceDisk
    objDiskList = {}
    objDisk = {}
    for tagE in diskDomB.getElementsByTagRoute("disk"):
        objDisk["description"] = diskDomB.getTextByTagRoute("description")
        objDisk["size"] = diskDomB.getTextByTagRoute("size")
        objDisk["init"] = diskDomB.getTextByTagRoute("init")
        objDisk["interface"] = diskDomB.getTextByTagRoute("interface")
        objDiskList[diskDomB.getTextByTagRoute("device")] = objDisk
    return sourceDiskList == objDiskList

 
def compareDiskDom(diskDomA, diskDomB, requestFlag=True):
    command = "command.server.disk."
    if not requestFlag:
        command = ""
    sourceDisk = {}
    sourceDisk["device"] = diskDomA.getTextByTagRoute("device")
    sourceDisk["description"] = diskDomA.getTextByTagRoute("description")
    sourceDisk["size"] = diskDomA.getTextByTagRoute("size")
    sourceDisk["init"] = diskDomA.getTextByTagRoute("init")
    sourceDisk["interface"] = diskDomA.getTextByTagRoute("interface")
    for tagE in diskDomA.getElementsByTagRoute("partition"):
        sourceDiskPartitions = {}
        partitionDom = XDOM()
        partitionDom.setDomObj(tagE)
        sourceDiskPartitions["size"] = partitionDom.getTextByTagRoute("size")
        #sourceDiskPartitions["free"] = partitionDom.getTextByTagRoute("free")
        sourceDiskPartitions["format"] = partitionDom.getTextByTagRoute("format")
        sourceDiskPartitions["uuid"] = partitionDom.getTextByTagRoute("uuid")
        sourceDisk[partitionDom.getTextByTagRoute("device")] = sourceDiskPartitions

    objDisk = {}
    objDisk["device"] = diskDomB.getTextByTagRoute(command + "device")
    objDisk["description"] = diskDomB.getTextByTagRoute(command + "description")
    objDisk["size"] = diskDomB.getTextByTagRoute(command + "size")
    objDisk["init"] = diskDomB.getTextByTagRoute(command + "init")
    objDisk["interface"] = diskDomB.getTextByTagRoute(command + "interface")
    for tagE in diskDomB.getElementsByTagRoute(command + "partition"):
        objDiskPartitions = {}
        partitionDom = XDOM()
        partitionDom.setDomObj(tagE)
        objDiskPartitions["size"] = partitionDom.getTextByTagRoute("size")
        #objDiskPartitions["free"] = partitionDom.getTextByTagRoute("free")
        objDiskPartitions["format"] = partitionDom.getTextByTagRoute("format")
        objDiskPartitions["uuid"] = partitionDom.getTextByTagRoute("uuid")
        objDisk[partitionDom.getTextByTagRoute("device")] = objDiskPartitions
    return sourceDisk == objDisk


def getServerConfigDiskDom(serverName, diskName=None):
    diskConfigDom = XDOM()
    if not diskConfigDom.parseFile("%s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName)):
        Utils.log("Unable to parse %s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))
        return None
    diskTag = diskConfigDom.getElementsByTagRoute("disks.disk")
    if not diskTag:
        Utils.log("Unable to reterive disk information %s/%s/disk.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))
        return None
    if diskName:
        for tagE in diskTag:
            diskDom = XDOM()
            diskDom.setDomObj(tagE)
            if diskName == diskDom.getTextByTagRoute("device"):
                return diskDom
        return None

    for tagE in diskTag:
        for partitionTag in tagE.getElementsByTagName("partition"):
            tagE.removeChild(partitionTag)
    return diskConfigDom


def getDeviceMountPoint(device):
    try:
        fp = open("/proc/mounts")
        for token in [line.strip().split() for line in fp.readlines()]:
            if token and len(token) > 2 and token[0] == device:
                return token[1]
        fp.close()
    except IOError, e:
        return None

