#  Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import re
import subprocess
import glob
import Globals
from Protocol import *
from Utils import *

def isValidServer(serverName):
    for profile in getProfileList():
        if profile.ProfileName == "default" and profile.Active:
            if serverName == profile.DNS.Hostname:
                return True
    return False

def getHostname():
    for profile in getProfileList():
        if profile.ProfileName == "default" and profile.Active:
            return profile.DNS.Hostname
    return None

def getDomainName():
    try:
        domainName = open(Globals.DOMAINNAME_FILE).read()
    except IOError:
        return None
    return domainName.split()[0]

def replaceServerIp(fileName, findWhat, replaceWith):
    try:
        data = open(fileName).read()
        fp = open(fileName, "w")
        fp.write(re.sub(findWhat, replaceWith, data))
        fp.close()
        return True
    except IOError:
        return False
    except ValueError:
        return False
    except OSError:
        return False

def serverName2IpAddress(serverName):
    command = "dig %s | grep '^%s'" % (serverName, serverName)
    ps = subprocess.Popen(command,
                          shell=True,
                          stdout=subprocess.PIPE,
                          stdin=subprocess.PIPE,
                          stderr=subprocess.PIPE,
                          close_fds=True)
    ipAddress = serverName
    if ps.wait() == 0:
        output = ps.communicate()
        ipAddress = output[0].split()[-1]
    return ipAddress

def getInstallerIp():
    if not os.path.exists(Globals.INSTALLER_INFO_FILE):
        return None
    try:
        for line in open(Globals.INSTALLER_INFO_FILE):
            tokens = line.split("=")
            if tokens[0] == "IP-ADDRESS":
                return tokens[1].split(",")[0].strip()
    except IOError:
        syslog.syslog(syslog.LOG_ERR, "unable to read %s file" % Globals.INSTALLER_INFO_FILE)
    return False

def setInstallerIp(installerIp):
    try:
        open(Globals.INSTALLER_INFO_FILE, "w").write("IP-ADDRESS=%s\n" % installerIp)
        return True
    except IOError:
        log(syslog.LOG_ERR, "unable to create %s file" % Globals.INSTALLER_INFO_FILE)
    return False

def getCurrentServerName():
    try:
        for line in open(Globals.SYSCONFIG_NETWORK_FILE):
            tokens = line.split("=")
            if tokens[0] == "HOSTNAME":
                return tokens[1].strip()
    except IOError:
        syslog.syslog(syslog.LOG_ERR, "unable to read %s file" % Globals.SYSCONFIG_NETWORK_FILE)
    return False

def getLastAccessedNetwork(serverName):
    lastAccessedNetworkFile = ("/%s/servers/%s/%s" %
                               (Globals.GLUSTER_CONF_DIR, serverName, Globals.LAST_ACCESSED_NETWORK_FILE))
    try:
        return open(lastAccessedNetworkFile).read().strip()
    except IOError:
        log(syslog.LOG_ERR, "failed to read last accessed network file %s" % lastAccessedNetworkFile)
        pass
    return False

def setLastAccessedNetwork(serverName, ipAddress):
    lastAccessedNetworkFile = ("/%s/servers/%s/%s" %
                               (Globals.GLUSTER_CONF_DIR, serverName, Globals.LAST_ACCESSED_NETWORK_FILE))
    try:
        open(lastAccessedNetworkFile, "w").write(ipAddress.strip() + "\n")
    except IOError:
        log(syslog.LOG_ERR, "failed to write last accessed network file %s" % lastAccessedNetworkFile)
        return False
    return True

def getServerIpList(serverName, preferredNetworkOnly=False):
    networkXmlFile = ("%s/servers/%s/network.xml" % (Globals.GLUSTER_CONF_DIR, serverName))
    configDom = XDOM()
    if not configDom.parseFile(networkXmlFile):
        log(syslog.LOG_ERR, "failed to read %s file" % networkXmlFile)
        return None
    preferredNetwork = configDom.getTextByTagRoute("preferred-network")
    ipList = []
    interfaceDom = XDOM()
    for tagE in configDom.getElementsByTagName("interface"):
        interfaceDom.setDomObj(tagE)
        deviceName = interfaceDom.getTextByTagRoute("device")
        hostIp = interfaceDom.getTextByTagRoute("ipaddr")
        if not hostIp:
            continue
        if preferredNetworkOnly:
            if preferredNetwork.upper() == "ANY" or preferredNetwork.upper() == deviceName.upper():
                ipList.append(hostIp)
        else:
            ipList.append(hostIp)
    if preferredNetworkOnly:
        lastAccessedNetworkIp = getLastAccessedNetwork(serverName)
        if lastAccessedNetworkIp in ipList:
            ipList.remove(lastAccessedNetworkIp)
            ipList = [lastAccessedNetworkIp] + ipList
    return ipList

def getServerPreferredIpList(serverName):
    return getServerIpList(serverName, True)

def getExecuteServerList(serverList):
    executeServerList = {}
    for serverName in serverList:
        if serverName == Globals.INSTALLER_SERVER_NAME:
            installerIp = getInstallerIp()
            if installerIp:
                executeServerList[serverName] = [installerIp]
            continue
        executeServerList[serverName] = getServerPreferredIpList(serverName)
    return executeServerList

def getAllServerList():
    serverList = []
    for filePath in glob.glob("%s/servers/*" % Globals.GLUSTER_CONF_DIR):
        if os.path.isdir(filePath):
            serverList.append(os.path.basename(filePath))
    try:
        serverList.remove(Globals.INSTALLER_SERVER_NAME)
    except ValueError:
        pass
    return serverList

def getServerNetworkConfigFromLocalFile(serverName):
    configDom = XDOM()
    configDom.parseFile("%s/servers/%s/network.xml" % (Globals.GLUSTER_CONF_DIR, serverName))
    return configDom

def updateServerNetworkConfigXmlFile(serverName, serverNetworkDom):
    configDom = XDOM()
    serverTag = serverNetworkDom.getElementsByTagRoute("server")[0]
    configDom.setDomObj(serverTag)
    if not configDom.writexml("%s/%s/network.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName)):
        log("Faild to write xml file %s/%s/network.xml" % (Globals.SERVER_VOLUME_CONF_DIR, serverName))

def compareServerNetworkDom(serverNetworkDomA, serverNetworkDomB, requestFlag=True):
    command = "command.server."
    if not requestFlag:
        command = ""
    sourceServer = {}
    tagText = serverNetworkDomA.getTextByTagRoute("name")
    if not tagText:
        taxText = None
    sourceServer["name"] = tagText
    tagText = serverNetworkDomA.getTextByTagRoute("domain-name")
    if not tagText:
        tagText = None
    sourceServer["domain-name"]   = tagText
    tagText = serverNetworkDomA.getTextByTagRoute("search-domain")
    if not tagText:
        tagText = None
    sourceServer["search-domain"] = tagText
    tagText = serverNetworkDomA.getTextByTagRoute("dns1")
    if not tagText:
        tagText = None
    sourceServer["dns1"] = tagText
    tagText = serverNetworkDomA.getTextByTagRoute("dns2")
    if not tagText:
        tagText = None
    sourceServer["dns2"] = tagText
    tagText = serverNetworkDomA.getTextByTagRoute("dns3")
    if not tagText:
        tagText = None
    sourceServer["dns3"] = tagText
    for tagE in serverNetworkDomA.getElementsByTagRoute("interface"):
        interfaceDom = XDOM()
        interfaceDom.setDomObj(tagE)
        sourceServerList = {}
        tagText = interfaceDom.getTextByTagRoute("description")
        if not tagText:
            tagText = None
        sourceServerList["description"] = tagText
        tagText = interfaceDom.getTextByTagRoute("hwaddr")
        if not tagText:
            tagText = None
        sourceServerList["hwaddr"] = tagText
        tagText = interfaceDom.getTextByTagRoute("onboot")
        if not tagText:
            tagText = None
        sourceServerList["onboot"] = tagText
        tagText = interfaceDom.getTextByTagRoute("bootproto")
        if not tagText:
            tagText = None
        sourceServerList["bootproto"] = tagText
        tagText = interfaceDom.getTextByTagRoute("ipaddr")
        if not tagText:
            tagText = None
        sourceServerList["ipaddr"]  = tagText
        tagText = interfaceDom.getTextByTagRoute("netmask")
        if not tagText:
            tagText = None
        sourceServerList["netmask"] = tagText
        tagText = interfaceDom.getTextByTagRoute("gateway")
        if not tagText:
            tagText = None
        sourceServerList["gateway"] = tagText
        sourceServer[interfaceDom.getTextByTagRoute("device")] = sourceServerList
    objServer = {}
    tagText = serverNetworkDomB.getTextByTagRoute(command + "name")
    if not tagText:
        taxText = None
    objServer["name"] = tagText
    tagText = serverNetworkDomB.getTextByTagRoute(command + "domain-name")
    if not tagText:
        tagText = None
    objServer["domain-name"]   = tagText
    tagText = serverNetworkDomB.getTextByTagRoute(command + "search-domain")
    if not tagText:
        tagText = None
    objServer["search-domain"] = tagText
    tagText = serverNetworkDomB.getTextByTagRoute(command + "dns1")
    if not tagText:
        tagText = None
    objServer["dns1"] = tagText
    tagText = serverNetworkDomB.getTextByTagRoute(command + "dns2")
    if not tagText:
        tagText = None
    objServer["dns2"] = tagText
    tagText = serverNetworkDomB.getTextByTagRoute(command + "dns3")
    if not tagText:
        tagText = None
    objServer["dns3"] = tagText
    for tagE in serverNetworkDomB.getElementsByTagRoute(command + "interface"):
        interfaceDom = XDOM()
        interfaceDom.setDomObj(tagE)
        objServerList = {}
        tagText = interfaceDom.getTextByTagRoute("description")
        if not tagText:
            tagText = None
        objServerList["description"] = tagText
        tagText = interfaceDom.getTextByTagRoute("hwaddr")
        if not tagText:
            tagText = None
        objServerList["hwaddr"] = tagText
        tagText = interfaceDom.getTextByTagRoute("onboot")
        if not tagText:
            tagText = None
        objServerList["onboot"] = tagText
        tagText = interfaceDom.getTextByTagRoute("bootproto")
        if not tagText:
            tagText = None
        objServerList["bootproto"] = tagText
        tagText = interfaceDom.getTextByTagRoute("ipaddr")
        if not tagText:
            tagText = None
        objServerList["ipaddr"]  = tagText
        tagText = interfaceDom.getTextByTagRoute("netmask")
        if not tagText:
            tagText = None
        objServerList["netmask"] = tagText
        tagText = interfaceDom.getTextByTagRoute("gateway")
        if not tagText:
            tagText = None
        objServerList["gateway"] = tagText
        objServer[interfaceDom.getTextByTagRoute("device")] = objServerList
    return sourceServer == objServer
