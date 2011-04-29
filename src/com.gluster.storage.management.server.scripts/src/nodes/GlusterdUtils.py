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
import Utils

import ServerUtils


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


def isVolumeRunning(volumeName):
    if not volumeName:
        return False
    volumeInfo = getGlusterVolumeInfo(volumeName)
    if not volumeInfo:
        return False
    status = volumeInfo[volumeName]["VolumeStatus"]
    if not status:
        return False
    if status.upper() == "STARTED":
        return True
    return False


def isVolumeExist(volumeName):
    if not volumeName:
        return False
    if getGlusterVolumeInfo(volumeName):
        return True
    return False


def peerProbe(serverName):
    command = "gluster peer probe %s" % serverName
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def setAuthAllow(volumeName, authList, includeServers=True):
    if not (volumeName and authList):
        return False
    vacl = []
    if includeServers:
        for serverName in ServerUtils.getAllServerList():
            vacl += ServerUtils.getServerIpList(serverName)
    vacl += authList

    command = "gluster volume set %s auth.allow %s" % (volumeName, ",".join(list(set(vacl))))
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def volumeCreate(volumeName, volumeType, transportTypeList, brickList):
    command = "gluster volume create %s" % volumeName

    if volumeType.upper() == "MIRROR":
        command += " replica 2"
    elif volumeType.upper() == "STRIPE":
        command += " stripe 4"

    if "RDMA" in transportTypeList:
        command += " transport rdma"

    command += " " + " ".join(brickList)

    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def volumeDelete(volumeName):
    command = "gluster --mode=script volume delete %s" % volumeName
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def volumeLogFileName(volumeName, brick, logDir):
    command = "gluster volume log filename %s %s %s" % (volumeName, brick, logDir)
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def startVolumeMigration(volumeName, sourcePath, destinationPath):
    command = "gluster volume replace-brick %s %s %s start" % (volumeName, sourcePath, destinationPath)
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if lines[0].split()[-1] == "successfully":
            return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def stopVolumeMigration(volumeName, sourcePath, destinationPath):
    command = "gluster volume replace-brick %s %s %s abort" % (volumeName, sourcePath, destinationPath)
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if lines[0].split()[-1] == "successful":
            return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def commitVolumeMigration(volumeName, sourcePath, destinationPath):
    command = "gluster volume replace-brick %s %s %s commit" % (volumeName, sourcePath, destinationPath)
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if lines[0].split()[-1] == "successful":
            return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False
                                

def getMigrationStatus(volumeName, sourcePath, destinationPath):
    command = "gluster volume replace-brick %s %s %s status" % (volumeName, sourcePath, destinationPath)
    status = Utils.runCommand(command, output=True, root=True)
    if status['Status'] == 0 and status['Stdout']:
        lines = status["Stdout"].split("\n")
        if "Current file" in lines[0]:
            return "started"
        if "Migration complete" in lines[0]:
            return "completed"
        Utils.log("command [%s] returns unknown status:%s" % (command, lines[0]))
        return "failed"
    #if status['Status'] == 0 and status['Stdout']:
    #    for line in status['Stdout'].split('\n'):
    #        words = line.split()
    #        if words and words[0].upper() == "STATUS:":
    #            return " ".join(words[1:]).upper()
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return None


def volumeRebalanceStart(volumeName):
    command = "gluster volume rebalance %s start" % volumeName
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if lines[0].split()[-1] == "successful":
            return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def volumeRebalanceStop(volumeName):
    command = "gluster volume rebalance %s stop" % volumeName
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if lines[0].split()[0] == "stopped":
            return True
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False


def volumeRebalanceStatus(volumeName):
    command = "gluster volume rebalance %s status" % volumeName
    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] == 0:
        lines = status["Stdout"].split("\n")
        if "rebalance not started" in lines[0]:
            return "not started"
        if "rebalance completed" in lines[0]:
            return "completed"
        return "running"
    Utils.log("command [%s] failed with [%d:%s]" % (command, status["Status"], os.strerror(status["Status"])))
    return False
