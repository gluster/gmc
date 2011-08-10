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
import tempfile
from operator import itemgetter
import Globals
from Protocol import *
from Utils import *
from DiskUtils import *
from ServerUtils import *
import GlusterdUtils as Glusterd


def isVolumeExist(volumeName):
    volumeDom = XDOM()
    return volumeDom.parseFile("%s/%s.xml" % (Globals.VOLUME_CONF_DIR, volumeName)) and \
        Glusterd.isVolumeExist(volumeName)


def getVolumeUuid(volumeName):
    fileName = "%s/%s.xml" % (Globals.VOLUME_CONF_DIR, volumeName)
    volumeDom = XDOM()
    if not volumeDom.parseFile(fileName):
        log("Failed to parse volume configuration file %s of %s" % (fileName, volumeName))
        return None
    return volumeDom.getTextByTagRoute("uuid")


def readVolumeSmbConfFile(fileName=Globals.VOLUME_SMBCONF_FILE):
    entryList = []
    try:
        fp = open(fileName)
        for line in fp:
            tokens = line.split("#")[0].strip().split(";")[0].strip().split("=")
            if len(tokens) != 2:
                continue
            if tokens[0].strip().upper() == "INCLUDE":
                entryList.append(tokens[1].strip())
        fp.close()
    except IOError, e:
        log("Failed to open file %s: %s" % (fileName, str(e)))
    return entryList


def writeVolumeSmbConfFile(entryList, fileName=Globals.VOLUME_SMBCONF_FILE):
    try:
        fp = open(fileName, "w")
        for entry in entryList:
            fp.write("include = %s\n" % entry)
        fp.close()
        return True
    except IOError, e:
        log("Failed to write file %s: %s" % (fileName, str(e)))
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
    log("entryList = %s" % entryList)
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
        log("Failed to write file %s: %s" % (volumeFile, str(e)))
        return False


def removeVolumeCifsConfiguration(volumeName):
    volumeFile = "%s/%s.smbconf" % (Globals.VOLUME_CONF_DIR, volumeName)
    try:
        os.remove(volumeFile)
        return True
    except OSError, e:
        log("Failed to remove file %s: %s" % (volumeFile, str(e)))
        return False


def getVolumeListByPartitionName(partitionName):
    volumeConfigFileList = glob.glob(Globals.VOLUME_CONF_DIR + "/*.xml")
    if not volumeConfigFileList:
        return None

    volumeList = []
    for volumeXmlFile in volumeConfigFileList:
        volumeDom = XDOM()
        volumeDom.parseFile(volumeXmlFile)
        serverTopology = volumeDom.getElementsByTagRoute("volume.topology.group")
        serverPartitionFound = False
        for topology in serverTopology:
            partitionDom = XDOM()
            for partition in topology.getElementsByTagName("partition"):
                partitionDom.setDomObj(partition)
                if partitionDom.getTextByTagRoute("name") == partitionName:
                    serverPartitionFound = True
                    break
            if serverPartitionFound:
                volumeList.append(volumeDom.getElementsByTagRoute("volume")[0])
                break
    return volumeList
    

def addServerPartitionConfig(inputDom, groupOrder, partitionTag):
    if not(inputDom and groupOrder and partitionTag):
        return False
    groupDom = XDOM()
    for group in inputDom.getElementsByTagRoute("topology.group"):
        groupDom.setDomObj(group)
        order = groupDom.getTextByTagRoute("order")
        if order and int(order) == groupOrder:
            group.appendChild(partitionTag)
            return inputDom
    return False
    

def removeServerPartitionConfig(inputDom, partitionName):
    if not(inputDom and partitionName):
        return False
    for group in inputDom.getElementsByTagRoute("topology.group"):
        partitionDom = XDOM()
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            if partitionDom.getTextByTagRoute("name") == partitionName:
                group.removeChild(partition)
                return inputDom
    return False
                       

def updateServerPartitionConfig(inputDom, partitionName, partitionTag):
    if not(inputDom and partitionName and partitionTag):
        return False
    for group in inputDom.getElementsByTagRoute("topology.group"):
        partitionDom = XDOM()
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            if partitionDom.getTextByTagRoute("name") == partitionName:
                try:
                    group.replaceChild(partitionTag, partition)
                    return inputDom
                except AttributeError:
                    return False
    return False
    

def getServerPartitionConfigUuid(serverGroupList, serverPartition):
    for group in serverGroupList:
        if not group:
            continue
        partitionDom = XDOM()
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            partitionName = partition.getTextByTagName("name")
            if not partitionName:
                continue
            if partitionName == serverPartition:
                return partitionDom.getTextByTagName("uuid")
    return False


def setServerPartitionConfigProperty(inputDom, partitionName, propertyDict):
    if not(inputDom and partitionName and propertyDict):
        return False
    for group in inputDom.getElementsByTagRoute("topology.group"):
        partitionDom = XDOM()
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            if partitionDom.getTextByTagRoute("name") == partitionName:
                for part in propertyDict.keys():
                    x = partition.getElementsByTagName(part)
                    if x:
                        x[0].childNodes[0].nodeValue = propertyDict[part]
                return inputDom
    return False
    

def getSortedServerPartitionConfigProperty(inputDom):
    groupDict = {}
    if not inputDom:
        return None
    groupDom = XDOM()
    for group in inputDom.getElementsByTagRoute("topology.group"):
        groupDom.setDomObj(group)
        groupOrder = groupDom.getTextByTagRoute("order")
        if not groupOrder:
            return None
        groupOrder = int(groupOrder)
        if groupOrder < 1:
            return None
        partitionDom = XDOM()
        partitionDict = {}
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            partitionName = partitionDom.getTextByTagRoute("name")
            if not partitionName:
                return None
            partitionOrder = partitionDom.getTextByTagRoute("order")
            if not partitionOrder:
                return None
            partitionUuid = partitionDom.getTextByTagRoute("uuid")
            partitionOrder = int(partitionOrder)
            if partitionOrder < 1:
                return None
            partitionDetails = partitionName.split(":")
            if not partitionDetails or len(partitionDetails) < 1:
                return None
            partitionDict[partitionOrder] = {  "order":partitionOrder,
                                               "servername":partitionDetails[0],
                                               "name":partitionDetails[1],
                                               "uuid":partitionUuid}
        groupDict[groupOrder] = partitionDict

    serverList = []
    groupOrderList = groupDict.keys()
    groupOrderList.sort()
    for groupOrder in groupOrderList:
            partitionOrderList = groupDict[groupOrder].keys()
            partitionOrderList.sort()
            for partitionOrder in partitionOrderList:
                serverList.append(groupDict[groupOrder][partitionOrder])

    return serverList


def getSortedServerPartitionList(serverGroupElements):
    serverPartitionDict = {}
    groupOrderList = []
    serverList = []
    partitionDom = XDOM()
    for group in serverGroupElements:
        if not group:
            continue
        groupOrderE = group.getElementsByTagName("order")
        if not (groupOrderE and groupOrderE[0].childNodes):
            return None
        value = int(XDOM.getText(groupOrderE[0].childNodes))
        if value > 0:
            groupOrderList.append(value)
        partitionDict = {}
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)

            partitionName = partitionDom.getTextByTagRoute("name")
            if not partitionName:
                return None
            partitionOrder = partitionDom.getTextByTagRoute("order")
            if not partitionOrder:
                return None
            partitionUuid = partitionDom.getTextByTagRoute("uuid")
            partitionDict[int(partitionOrder)] = [partitionName, partitionUuid]
        serverPartitionDict[value] = partitionDict
    groupOrderList.sort()

    for groupOrder in groupOrderList:
        items = serverPartitionDict[groupOrder].items()
        items.sort(key = itemgetter(0))
        serverList = serverList + [ items[i][1] for i in range(0,len(items))]
    return serverList


def clearExportDirectory(serverList, volumeName, volumeUuid):
    thisServerName = getCurrentServerName()
    for exportServer in serverList:
        serverName, partition = exportServer[0].split(":")
        if thisServerName != serverName:
            continue
        partitionUuid = getUuidByDiskPartition(getDevice(partition))
        if not partitionUuid:
            log("unable to find uuid of partition %s" % partition)
            return False
        volumeDirName = "%s/%s/%s" % (Globals.GLUSTER_LUN_DIR, partitionUuid, volumeUuid)
        if os.path.exists(volumeDirName):
            ## Removing /data/PARTITION-UUID/VOLUME-UUID/
            ## TODO: Get an option to remove it at this time
            if runCommandFG("mv -f %s %s.delete" % (volumeDirName, volumeDirName), root=True) != 0:
                return False
            if runCommandFG("rm -f %s/%s/volumes/%s" % (Globals.GLUSTER_LUN_DIR, partitionUuid, volumeName), root=True) != 0:
                return False
    return True


def createExportDirectory(serverList, volumeName, volumeUuid):
    thisServerName = getCurrentServerName()
    tempVolumeNameFile = getTempFileName()

    try:
        fp = open(tempVolumeNameFile, "w")
        fp.write("VOLUME_NAME=%s\n" % volumeName)
        fp.write("VOLUME_UUID=%s\n" % volumeUuid)
        fp.close()
    except IOError, e:
        log("failed to create temporary file for volume-name: %s" % (volumeName, str(e)))
        return False

    for exportServer in serverList:
        serverName, partition = exportServer[0].split(":")
        if thisServerName != serverName:
            continue
        partitionUuid = getUuidByDiskPartition(getDevice(partition))
        if not partitionUuid:
            log("unable to find uuid of partition %s" % partition)
            return False

        volumeDirName = "%s/%s/%s" % (Globals.GLUSTER_LUN_DIR, partitionUuid, volumeUuid)
        ## Creating /data/PARTITION-UUID/VOLUME-UUID/
        if runCommandFG("mkdir %s" % volumeDirName, root=True) != 0:
            return False

        ## Creating /data/PARTITION-UUID/VOLUME-UUID/exports/
        ## Creating /data/PARTITION-UUID/VOLUME-UUID/exports/brick1/
        if runCommandFG("mkdir -p %s/exports/brick1" % volumeDirName, root=True) != 0:
            return False

        ## Creating /data/PARTITION-UUID/VOLUME-UUID/log/
        if runCommandFG("mkdir %s/log" % volumeDirName, root=True) != 0:
            return False

        ## Creating /data/PARTITION-UUID/VOLUME-UUID/config/
        if runCommandFG("mkdir %s/config" % volumeDirName, root=True) != 0:
            return False

        volumeLinkDirName = "%s/%s/volumes" % (Globals.GLUSTER_LUN_DIR, partitionUuid)
        if not os.path.exists(volumeLinkDirName):
            if runCommandFG("mkdir %s" % volumeLinkDirName, root=True) != 0:
                return False

        ## Creating symlink
        ## /data/PARTITION-UUID/volumes/VOLUME-NAME -> /data/PARTITION-UUID/VOLUME-UUID/
        command = "ln -fTs %s %s/%s" % (volumeDirName,
                                       volumeLinkDirName, volumeName)
        if runCommandFG(command, root=True) != 0:
            return False

        if runCommandFG("cp -f %s %s/config/volume-name" % (tempVolumeNameFile, volumeDirName), root=True) != 0:
            return False

    try:
        os.remove(tempVolumeNameFile)
    except OSError, e:
        log("Failed to remove file %s: %s" % (tempVolumeNameFile, str(e)))

    return True


def getPartitionListByServerName(volumeDom, serverName, serverPartitionList=None):
    partitionList = {}
    if serverPartitionList:
        for partitionName in serverPartitionList:
            partitionUuid = getServerDiskPartitionUuid(serverName, partitionName)
            if not partitionUuid:
                log(syslog.LOG_ERR, "failed to get disk partition %s uuid of server %s" % (partitionName, serverName))
                return None
            partitionList[partitionName] = partitionUuid
        return partitionList
    for group in volumeDom.getElementsByTagRoute("topology.group"):
        for partitionTag in group.getElementsByTagName("partition"):
            nameE = partitionTag.getElementsByTagName("name")
            if not nameE:
                continue
            partition = XDOM.getText(nameE[0].childNodes)
            if not partition:
                continue
            server, partitionName = partition.split(":")
            if server != serverName:
                continue
            partitionUuid = getServerDiskPartitionUuid(serverName, partitionName)
            if not partitionUuid:
                log(syslog.LOG_ERR, "failed to get disk partition %s uuid of server %s" % (partitionName, serverName))
                return None
            partitionList[partitionName] = partitionUuid
    return partitionList


def isVolumeRunning(volumeName):
    return Glusterd.isVolumeRunning(volumeName)

def addVolumeMigrationDetails(sourcePartition, destinationPartition, volumeName):
    migrationDom = XDOM()
    if not os.path.exists(Globals.VOLUME_MIGRATION_LIST_FILE):
        migrationDom.appendTagRoute("volume-migration")
    else:
        if not migrationDom.parseFile(Globals.VOLUME_MIGRATION_LIST_FILE):
            log("Failed to load volume-migration.xml file")
            return None
        migrationList = migrationDom.getElementsByTagRoute("volume-migration.migration")
        for tagE in migrationList:
            dom = XDOM()
            dom.setDomObj(tagE)
            if dom.getTextByTagRoute("source-partition") == sourcePartition and \
                dom.getTextByTagRoute("destination-partition") == destinationPartition and \
                    dom.getTextByTagRoute("volume-name") == volumeName:
                return False
    migrationTag = migrationDom.getElementsByTagRoute("volume-migration")
    if not migrationTag:
        return None
    partitionTag = migrationDom.createTag("migration")
    partitionTag.appendChild(migrationDom.createTag("source-partition", sourcePartition))
    partitionTag.appendChild(migrationDom.createTag("destination-partition", destinationPartition))
    partitionTag.appendChild(migrationDom.createTag("volume-name", volumeName))
    migrationTag[0].appendChild(partitionTag)
    if not migrationDom.writexml(Globals.VOLUME_MIGRATION_LIST_FILE):
        log("Unable to write disk migration details into %s/volume-migration.xml" % Globals.GLUSTER_BASE_DIR)
        return False
    return True


def removeVolumeMigrationDetails(sourcePartition, destinationPartition, volumeName):
    migrationDom = XDOM()
    if not os.path.exists(Globals.VOLUME_MIGRATION_LIST_FILE):
        return None
    if not migrationDom.parseFile(Globals.VOLUME_MIGRATION_LIST_FILE):
        log("Failed to load volume-migration.xml file")
        return None
    migrationList = migrationDom.getElementsByTagRoute("volume-migration.migration")
    for tagE in migrationList:
        dom = XDOM()
        dom.setDomObj(tagE)
        if dom.getTextByTagRoute("source-partition") == sourcePartition and \
               dom.getTextByTagRoute("destination-partition") == destinationPartition and \
                   dom.getTextByTagRoute("volume-name") == volumeName:
            migrationDom.getElementsByTagRoute("volume-migration")[0].removeChild(tagE)
    if not migrationDom.writexml(Globals.VOLUME_MIGRATION_LIST_FILE):
        log("Unable to write disk migration details into %s/volume-migration.xml" % Globals.GLUSTER_BASE_DIR)
        return False
    return True


def addPartitionMigrationDetails(sourcePartition, destinationPartition, volumeList=None):
    migrationDom = XDOM()
    if not os.path.exists(Globals.MIGRATE_PARTITION_LIST_FILE):
        migrationDom.appendTagRoute("partition-migration")
    else:
        if not migrationDom.parseFile(Globals.MIGRATE_PARTITION_LIST_FILE):
            log("Failed to load migration.xml file")
            return None
        migrationList = migrationDom.getElementsByTagRoute("partition-migration.migration")
        for tagE in migrationList:
            dom = XDOM()
            dom.setDomObj(tagE)
            if dom.getTextByTagRoute("source-partition") == sourcePartition:
                return False
            if dom.getTextByTagRoute("destination-partition") == destinationPartition:
                return False
    migrationTag = migrationDom.getElementsByTagRoute("partition-migration")
    if not migrationTag:
        return None
    partitionTag = migrationDom.createTag("migration")
    partitionTag.appendChild(migrationDom.createTag("source-partition", sourcePartition))
    partitionTag.appendChild(migrationDom.createTag("destination-partition", destinationPartition))
    migrationTag[0].appendChild(partitionTag)
    if not migrationDom.writexml(Globals.MIGRATE_PARTITION_LIST_FILE):
        log("Unable to write disk migration details into %s/migration.xml" % Globals.GLUSTER_BASE_DIR)
        return False
    if volumeList:
        for volumeName in volumeList:
            addVolumeMigrationDetails(sourcePartition, destinationPartition, volumeName)
    return True

    
def removePartitionMigrationDetails(sourcePartition, destinationPartition, volumeList=None):
    migrationDom = XDOM()
    if not os.path.exists(Globals.MIGRATE_PARTITION_LIST_FILE):
        return None
    if not migrationDom.parseFile(Globals.MIGRATE_PARTITION_LIST_FILE):
        log("Failed to load migration.xml file")
        return None
    migrationList = migrationDom.getElementsByTagRoute("partition-migration.migration")
    for tagE in migrationList:
        dom = XDOM()
        dom.setDomObj(tagE)
        if dom.getTextByTagRoute("source-partition") == sourcePartition and \
               dom.getTextByTagRoute("destination-partition") == destinationPartition:
            migrationDom.getElementsByTagRoute("partition-migration")[0].removeChild(tagE)
    if not migrationDom.writexml(Globals.MIGRATE_PARTITION_LIST_FILE):
        log("Unable to write disk migration details into %s/migration.xml" % Globals.GLUSTER_BASE_DIR)
        return False
    if volumeList:
        for volumeName in volumeList:
            removeVolumeMigrationDetails(sourcePartition, destinationPartition, volumeName)
    return True


def isMigrationInProgress(partition):
    migrationDom = XDOM()
    if not os.path.exists(Globals.MIGRATE_PARTITION_LIST_FILE):
        return None
    if not migrationDom.parseFile(Globals.MIGRATE_PARTITION_LIST_FILE):
        log("Failed to load migration.xml file")
        return None
    migrationList = migrationDom.getElementsByTagRoute("partition-migration.migration")
    for tagE in migrationList:
        dom = XDOM()
        dom.setDomObj(tagE)
        if migrationDom.getTextByTagRoute("source-partition") == partition or \
               migrationDom.getTextByTagRoute("destination-partition") == partition:
            return True
    return False


def getServerDiskPartitionUuid(serverName, partition):
    diskConfigDom = XDOM()
    if not diskConfigDom.parseFile("%s/%s/disk.xml" % (Globals.SERVER_CONF_DIR, serverName)):
        return None
    for disk in diskConfigDom.getElementsByTagRoute("disks.disk"):
        diskDom = XDOM()
        diskDom.setDomObj(disk)
        partitionList = diskDom.getElementsByTagRoute("partition")
        for tagE in partitionList:
            partitionDom = XDOM()
            partitionDom.setDomObj(tagE)
            if partitionDom.getTextByTagRoute("device") == partition:
                return partitionDom.getTextByTagRoute("uuid")


def getVolumeServerList(requestDom, requestFlag=True):
    if requestFlag:
        serverGroupElementList = requestDom.getElementsByTagRoute("command.volume.topology.group")
    else:
        serverGroupElementList = requestDom.getElementsByTagRoute("volume.topology.group")
    if not serverGroupElementList:
        return None
    serverList = []
    partitionDom = XDOM()
    for group in serverGroupElementList:
        for partition in group.getElementsByTagName("partition"):
            partitionDom.setDomObj(partition)
            partitionName = partitionDom.getTextByTagRoute("name")
            if not partitionName:
                continue
            serverPartition = partitionName.split(":")
            if not(len(serverPartition) > 1 and serverPartition[1]):
                return None
            if serverPartition[0] not in serverList:
                serverList.append(serverPartition[0])
    return serverList


def getVolumeServerListByName(volumeName):
    serverList = []
    serverDom = XDOM()
    volumeDom = XDOM()
    if not os.path.exists("%s/%s.xml" % (Globals.VOLUME_CONF_DIR, volumeName)):
        return False
    if not volumeDom.parseFile("%s/%s.xml" % (Globals.VOLUME_CONF_DIR, volumeName)):
        return False
    return getVolumeServerList(volumeDom, False)


def getMigrateVolumeServerPartitionInfo(volumeName):
    volumeMigrationDom = XDOM()
    if not volumeMigrationDom.parseFile(Globals.VOLUME_MIGRATION_LIST_FILE):
        Utils.log("Failed to parse file %s" % Globals.VOLUME_MIGRATION_LIST_FILE)
        return None
    volumeInfo = {}
    dom = XDOM()
    for tagE in volumeMigrationDom.getElementsByTagRoute("volume-migration.migration"):
        dom.setDomObj(tagE)
        if dom.getTextByTagRoute("volume-name") == volumeName:
            volumeInfo['Name'] = volumeName
            volumeInfo['SourcePartition'] = dom.getTextByTagRoute("source-partition")
            volumeInfo['DestinationPartition'] = dom.getTextByTagRoute("destination-partition")
            return volumeInfo
    return None
