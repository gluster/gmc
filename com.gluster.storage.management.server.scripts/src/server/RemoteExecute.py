#  Copyright (C) 2010 Gluster, Inc. <http://www.gluster.com>
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
import socket
#import paramiko
import syslog
import sys
import Socket
import Globals
from copy import deepcopy
from ServerUtils import *

SERVER_AGENT_COMMAND = "/usr/sbin/server-agent"
SERVER_AGENT_CLEANUP_COMMAND = SERVER_AGENT_COMMAND + " --cleanup"
SERVER_AGENT_PRE_COMMAND = SERVER_AGENT_COMMAND + " --pre"
SERVER_AGENT_POST_COMMAND = SERVER_AGENT_COMMAND + " --post"
TRANSPORT_USER_NAME = "transport"
TRANSPORT_PRIVATE_KEY_FILE = Globals.TRANSPORT_HOME_DIR + "/.ssh/id_rsa"

def remoteExecute(serverList, command, commandInput=None):
    print "REMOTE:", serverList
    statusDict = {}
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        privateKey = paramiko.RSAKey.from_private_key_file(TRANSPORT_PRIVATE_KEY_FILE)
    except IOError:
        log(syslog.LOG_ERR, "Private key file %s not found" % TRANSPORT_PRIVATE_KEY_FILE)
        return None
    print "STAGE1"
    for serverName in serverList.keys():
        serverStatus = {}
        serverStatus["ConnectionStatus"] = None
        serverStatus["ExecutionStatus"] = None
        serverStatus["StdOutString"] = None
        serverStatus["StdErrString"] = None
        serverStatus["ConnectedIp"] = None
        serverStatus["Error"] = None

        isConnected = False
        for serverIp in serverList[serverName]:
            try:
                ssh.connect(serverIp, username=TRANSPORT_USER_NAME, pkey=privateKey)
                isConnected = True
                break
            except socket.error:
                log(syslog.LOG_ERR, "Server %s:%s is inaccessible" % (serverName, serverIp))
                continue
        if not isConnected:
            serverStatus["ConnectionStatus"] = "inaccessible"
            statusDict[serverName] = serverStatus
            continue

        try:
            transport = ssh.get_transport()
            channel = transport.open_session()
            serverStatus["ConnectionStatus"] = True
            channel.exec_command(command)
            stdin = channel.makefile('wb')
            stdout = channel.makefile('rb')
            stderr = channel.makefile_stderr('rb')
            if commandInput:
                stdin.write(commandInput)
            channel.shutdown_write()
        
            returnValue = channel.recv_exit_status() ## this is blocking call
            serverStatus["ExecutionStatus"] = returnValue
            print "RRRRRRRRRRRRRRRR:", returnValue
            errorString = ""
            if -1 == returnValue:
                errorString = stderr.read()
                serverStatus["StdErrString"] = errorString
                if "bash: " + command.split()[0] + ": command not found\n" == errorString:
                    log(syslog.LOG_ERR, "command %s not found in server %s" % (command, serverName))
                    serverStatus["Error"] = "Command not found"
            else:
                serverStatus["StdErrString"] = stderr.read()
            serverStatus["StdOutString"] = stdout.read()
            ssh.close()
        except paramiko.SSHException:
            # Channel error (channel not open)
            log(syslog.LOG_ERR, "Server %s:%s connection aborted" % (serverName, serverIp))
            serverStatus["ConnectionStatus"] = "aborted"
        except socket.error:
            log(syslog.LOG_ERR, "Server %s:%s is inaccessible" % (serverName, serverIp))
            serverStatus["ConnectionStatus"] = "inaccessible"
        except paramiko.AuthenticationException:
            log(syslog.LOG_ERR, "Authentication error on server %s:%s of user %s" % 
                (serverName, serverIp, TRANSPORT_USER_NAME))
            serverStatus["ConnectionStatus"] = "authentication error"
        serverStatus["ConnectedIp"] = serverIp
        statusDict[serverName] = serverStatus
    return statusDict

def cleanupExecuteSsh(serverList, requestDom):
    return remoteExecute(serverList, SERVER_AGENT_CLEANUP_COMMAND, requestDom.toxml())

def executeRequestCommandSsh(serverList, command, requestDom, cleanupFlag):
    cleanupStatusDict = {}
    successStatusDict = {}
    failureServerList = {}
    cleanupServerList = {}
    serverList = deepcopy(serverList)
    statusDict = remoteExecute(serverList, command, requestDom.toxml())
    for serverName in statusDict.keys():
        statusDict["Response"] = None
        if statusDict[serverName]["ConnectionStatus"] == True:
            setLastAccessedNetwork(serverName, statusDict[serverName]["ConnectedIp"])
        if statusDict[serverName]["ConnectedIp"]:
            ipList = serverList[serverName]
            ipList.remove(statusDict[serverName]["ConnectedIp"])
            cleanupServerList[serverName] = [statusDict[serverName]["ConnectedIp"]] + ipList
        if statusDict[serverName]["ExecutionStatus"] != 0:
            failureServerList[serverName] = statusDict[serverName]
            continue
        responseDom = XDOM()
        if not responseDom.parseString(statusDict[serverName]["StdOutString"]):
            failureServerList[serverName] = statusDict[serverName]
            continue
        statusDict["Response"] = responseDom
        if "OK" != responseDom.getAttribute("response-code"):
            failureServerList[serverName] = statusDict[serverName]
            continue
        successStatusDict[serverName] = statusDict[serverName]
    if cleanupFlag and failureServerList:
        cleanupStatusDict = remoteExecute(cleanupServerList, SERVER_AGENT_CLEANUP_COMMAND, requestDom.toxml())
    return successStatusDict, failureServerList, cleanupStatusDict

def preExecuteSsh(serverList, requestDom, cleanupFlag=True):
    return executeRequestCommandSsh(serverList, SERVER_AGENT_PRE_COMMAND, requestDom, cleanupFlag)

def executeSsh(serverList, requestDom, cleanupFlag=True):
    return executeRequestCommandSsh(serverList, SERVER_AGENT_COMMAND, requestDom, cleanupFlag)

def postExecuteSsh(serverList, requestDom, cleanupFlag=True):
    return executeRequestCommandSsh(serverList, SERVER_AGENT_POST_COMMAND, requestDom, cleanupFlag)

def runPullUpdatesDir(sourceServerIp, destServerIpList):
    command = "/usr/sbin/pull-dir.sh %s %s %s" % (sourceServerIp,
                                                  Globals.UPDATES_DIR[1:],
                                                  Globals.UPDATES_DIR)
    statusDict = remoteExecute(destServerIpList, command)
    status = True
    for serverName in statusDict.keys():
        if statusDict[serverName]["ExecutionStatus"] != 0:
            log(syslog.LOG_ERR, "Failed to execute [%s] in server %s" % (command, serverName))
            status = False
    return status

def runPullGlusterDir(sourceServerIp, destServerIpList):
    command = "/usr/sbin/pull-dir.sh %s %s %s" % (sourceServerIp,
                                                  Globals.GLUSTER_BASE_DIR[1:],
                                                  Globals.GLUSTER_BASE_DIR)
    statusDict = remoteExecute(destServerIpList, command)
    status = True
    for serverName in statusDict.keys():
        if statusDict[serverName]["ExecutionStatus"] != 0:
            log(syslog.LOG_ERR, "Failed to execute [%s] in server %s" % (command, serverName))
            status = False
    return status

def syncConfiguration(syncToInstaller=False, sourceServerIpList=None):
    thisServerName = getCurrentServerName()
    serverList = getAllServerList()
    serverList.remove(thisServerName)
    serverIpList = getExecuteServerList(serverList)
    if syncToInstaller:
        installerIp = getInstallerIp()
        if not installerIp:
            log(syslog.LOG_ERR, "Installer IP address is not found")
            return False
        serverIpList[Globals.INSTALLER_SERVER_NAME] = [installerIp]

    if not serverIpList:
        log(syslog.LOG_ERR, "No servers found for sync configuration")
        return False

    signature = generateSignature()
    if not storeSignature(signature, Globals.SIGNATURE_FILE):
        log(syslog.LOG_ERR, "failed to store signature %s to %s file" % 
            (signature, Globals.SIGNATURE_FILE))
        return False

    thisServerIpList = getExecuteServerList([thisServerName])
    if sourceServerIpList:
        thisServerIpList = sourceServerIpList
    return runPullGlusterDir(thisServerIpList[thisServerName][0], serverIpList)

def remoteExecuteTcp(serverIpList, requestString):
    serverStatus = {}
    serverStatus["ConnectionStatus"] = False
    serverStatus["ExecutionStatus"] = -1
    serverStatus["StdOutString"] = None
    serverStatus["StdErrString"] = None
    serverStatus["ConnectedIp"] = None
    serverStatus["Error"] = None

    for ipAddress in serverIpList.values()[0]:
        try:
            sock, inputStream, outputStream = Socket.connectToServer(ipAddress)
            Socket.writePacket(outputStream, requestString)
            packetString = Socket.readPacket(inputStream)
            log('__DEBUG__ Received: %s' % repr(packetString))
            sock.close()
            serverStatus["ConnectionStatus"] = True
            serverStatus["ExecutionStatus"] = 0
            serverStatus["StdOutString"] = packetString
            serverStatus["StdErrString"] = None
            serverStatus["ConnectedIp"] = ipAddress
            serverStatus["Error"] = None
            return serverStatus
        except socket.error, e:
            log("socket error on [%s:%s]: %s" % (serverIpList.keys()[0], ipAddress, str(e)))
    return serverStatus

def executeRequestCommand(serverList, command, requestDom, cleanupFlag):
    cleanupStatusDict = {}
    successStatusDict = {}
    failureServerList = {}
    cleanupServerList = {}
    serverList = deepcopy(serverList)

    statusDict = {}
    for serverName in serverList.keys():
        serverStatus = remoteExecuteTcp({serverName : serverList[serverName]}, requestDom.toxml())
        statusDict[serverName] = serverStatus
    for serverName in statusDict.keys():
        statusDict["Response"] = None
        if statusDict[serverName]["ConnectionStatus"] == True:
            setLastAccessedNetwork(serverName, statusDict[serverName]["ConnectedIp"])
        if statusDict[serverName]["ConnectedIp"]:
            ipList = serverList[serverName]
            ipList.remove(statusDict[serverName]["ConnectedIp"])
            cleanupServerList[serverName] = [statusDict[serverName]["ConnectedIp"]] + ipList
        if statusDict[serverName]["ExecutionStatus"] != 0:
            failureServerList[serverName] = statusDict[serverName]
            continue
        responseDom = XDOM()
        if not responseDom.parseString(statusDict[serverName]["StdOutString"]):
            failureServerList[serverName] = statusDict[serverName]
            continue
        statusDict["Response"] = responseDom
        if "OK" != responseDom.getResponseCode():
            failureServerList[serverName] = statusDict[serverName]
            continue
        successStatusDict[serverName] = statusDict[serverName]
    if cleanupFlag and failureServerList:
        rq = deepcopy(requestDom)
        rq.setRequestAction("cleanup")
        cleanupStatusDict = {}
        for serverName in cleanupServerList.keys():
            serverStatus = remoteExecuteTcp({serverName : cleanupServerList[serverName]}, rq.toxml())
            cleanupStatusDict[serverName] = serverStatus
    return successStatusDict, failureServerList, cleanupStatusDict

def preExecute(serverList, requestDom, cleanupFlag=True):
    rq = deepcopy(requestDom)
    rq.setRequestAction("pre")
    return executeRequestCommand(serverList, SERVER_AGENT_PRE_COMMAND, rq, cleanupFlag)

def execute(serverList, requestDom, cleanupFlag=True):
    return executeRequestCommand(serverList, SERVER_AGENT_COMMAND, requestDom, cleanupFlag)

def postExecute(serverList, requestDom, cleanupFlag=True):
    rq = deepcopy(requestDom)
    rq.setRequestAction("post")
    return executeRequestCommand(serverList, SERVER_AGENT_POST_COMMAND, rq, cleanupFlag)

def cleanupExecute(serverList, requestDom):
    rq = deepcopy(requestDom)
    rq.setRequestAction("cleanup")
    return executeRequestCommand(serverList, SERVER_AGENT_CLEANUP_COMMAND, rq, False)
