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

def readResolvConfFile(fileName=None, includeLocalHost=True):
    nameServerList = []
    domain = None
    searchDomain = None
    if not fileName:
        fileName = Globals.RESOLV_CONF_FILE
    try:
        for line in open(fileName):
            tokens = line.split("#")[0].strip().split()
            if len(tokens) < 2:
                continue
            if tokens[0].upper() == "NAMESERVER":
                if includeLocalHost == False and tokens[1] == "127.0.0.1":
                    continue
                nameServerList.append(tokens[1])
                continue
            if tokens[0].upper() == "DOMAIN":
                domain = tokens[1:]
                continue
            if tokens[0].upper() == "SEARCH":
                searchDomain = tokens[1:]
                continue
        return nameServerList, domain, searchDomain
    except IOError, e:
        Utils.log("failed to read %s file: %s" % (fileName, str(e)))
        return None, None, None


def readIfcfgConfFile(deviceName, root=""):
    conf = {}
    fileName = "%s%s/ifcfg-%s" % (root, Globals.SYSCONFIG_NETWORK_DIR, deviceName)
    try:
        for line in open(fileName):
            tokens = line.split("#")[0].split("=")
            if len(tokens) != 2:
                continue
            conf[tokens[0].strip().lower()] = tokens[1].strip()
        return conf
    except IOError, e:
        Utils.log("failed to read %s file: %s" % (fileName, str(e)))
        return None


def getNetDeviceDetail(deviceName):
    deviceDetail = {}
    deviceDetail['Name'] = deviceName
    rv = Utils.runCommand("ifconfig %s" % deviceName, output=True, root=True)
    if rv["Status"] != 0:
        return False
    for line in rv["Stdout"].split():
        tokens = line.strip().split(":")
        if tokens[0].upper() == "ENCAP":
            deviceDetail['Model'] = tokens[1].strip().upper()
            break

    for line in rv["Stdout"].split("\n"):
        if line.strip().startswith("inet addr:"):
            tokens = line.strip().split(":")
            if tokens[0].upper() == "INET ADDR":
                try:
                    deviceDetail['Ip'] = tokens[1].strip().split()[0]
                    deviceDetail['Mask'] = tokens[-1].strip()
                except IndexError, e:
                    pass
            break
    return deviceDetail

def getNetDeviceGateway(deviceName):
    rv = Utils.runCommand("route -n", output=True, root=True)
    if rv["Status"] != 0:
        return None
    if not rv["Stdout"]:
        return None
    lines = [line for line in rv["Stdout"].split("\n") if line.find("UG") != -1 and line.find(deviceName)]
    if not lines:
        return None
    line = lines[-1].split()
    if line and len(line) > 1:
        return line[1]
    return None

def getNetSpeed(deviceName):
    rv = Utils.runCommand("ethtool %s" % deviceName, output=True, root=True)
    if rv["Status"] != 0:
        return False
    for line in rv["Stdout"].split("\n"):
        tokens = line.strip().split(":")
        if tokens[0].upper() == "SPEED":
            return tokens[1].strip().upper().split("MB")[0]
    return None

def getLinkStatus(deviceName):
    return True
    ## ethtool takes very long time to respond.  So its disabled now
    rv = Utils.runCommand("ethtool %s" % deviceName, output=True, root=True)
    if rv["Status"] != 0:
        return False
    for line in rv["Stdout"].split("\n"):
        tokens = line.strip().split(":")
        if tokens[0].upper() == "LINK DETECTED":
            if tokens[1].strip().upper() == "YES":
                return True
            else:
                return False
    return False


def getBondMode(deviceName, fileName=None):
    if not fileName:
        fileName = Globals.MODPROBE_CONF_FILE
    try:
        for line in open(fileName):
            tokens = line.split("#")[0].split()
            if len(tokens) < 4:
                continue
            if tokens[0].upper() == "OPTIONS" and tokens[1] == deviceName:
                if tokens[2].startswith("mode="):
                    return tokens[2].split("=")[1]
                if tokens[3].startswith("mode="):
                    return tokens[3].split("=")[1]
                if tokens[4].startswith("mode="):
                    return tokens[4].split("=")[1]
                if tokens[5].startswith("mode="):
                    return tokens[5].split("=")[1]
        return None
    except IOError, e:
        Utils.log("failed to read %s file: %s" % (fileName, str(e)))
        return None


def getNetDeviceList(root=""):
    netDeviceList = []
    for deviceName in os.listdir("/sys/class/net/"):
        netDevice = {}
        netDevice["device"] = None
        netDevice["description"] = None
        netDevice["hwaddr"] = None
        netDevice["type"] = None
        netDevice["onboot"] = None
        netDevice["bootproto"] = None
        netDevice["gateway"] = None
        netDevice["peerdns"] = None
        netDevice["autodns"] = None
        netDevice["dns1"] = None
        netDevice["dns2"] = None
        netDevice["dns3"] = None
        netDevice["master"] = None
        netDevice["slave"] = None
        netDevice["nmcontrolled"] = None
        netDevice["link"] = None
        netDevice["mode"] = None

        #netDevice["device"] = device.Name
        netDevice["device"] = deviceName
        #netDevice["description"] = device.Description
        netDevice["description"] = deviceName
        #netDevice["type"] = device.Type
        netDevice["type"] = None
        netDevice["link"] = getLinkStatus(deviceName)
        netDevice["mode"] = getBondMode(deviceName, root + Globals.MODPROBE_CONF_FILE)
        deviceDetail = getNetDeviceDetail(deviceName)
        if deviceDetail.has_key('Model'):
            netDevice["model"] = deviceDetail['Model']
        else:
            netDevice["model"] = None
        if deviceDetail.has_key('Ip'):
            netDevice["ipaddr"] = deviceDetail['Ip']
        else:
            netDevice["ipaddr"] = None
        if deviceDetail.has_key('Mask'):
            netDevice["netmask"] = deviceDetail['Mask']
        else:
            netDevice["netmask"] = None
        netDevice["speed"] = getNetSpeed(deviceName)
        try:
            netDevice["hwaddr"] = open("/sys/class/net/%s/address" % deviceName).read().strip()
        except IOError, e:
            pass
        
        netDeviceList.append(netDevice)

        conf = readIfcfgConfFile(deviceName, root)
        if not conf:
            continue
        try:
            netDevice["onboot"] = conf["onboot"]
        except KeyError, e:
            pass
        try:
            netDevice["bootproto"] = conf["bootproto"]
        except KeyError, e:
            pass
        if conf.has_key("ipaddr") and conf["ipaddr"]:
            netDevice["ipaddr"] = conf["ipaddr"]
        try:
            netDevice["netmask"] = conf["netmask"]
        except KeyError, e:
            pass
        if conf.has_key("gateway") and conf["gateway"]:
            netDevice["gateway"] = conf["gateway"]
        else:
            netDevice["gateway"] = getNetDeviceGateway(deviceName)
        try:
            netDevice["peerdns"] = conf["peerdns"]
        except KeyError, e:
            pass
        try:
            netDevice["autodns"] = conf["autodns"]
        except KeyError, e:
            pass
        try:
            netDevice["dns1"] = conf["dns1"]
        except KeyError, e:
            pass
        try:
            netDevice["dns2"] = conf["dns2"]
        except KeyError, e:
            pass
        try:
            netDevice["dns3"] = conf["dns3"]
        except KeyError, e:
            pass
        try:
            netDevice["master"] = conf["master"]
        except KeyError, e:
            pass
        try:
            netDevice["slave"] = conf["slave"]
        except KeyError, e:
            pass
        try:
            netDevice["nmcontrolled"] = conf["nmcontrolled"]
        except KeyError, e:
            pass

    return netDeviceList

    ## bondDevices = [os.path.basename(device) for device in glob.glob("/sys/class/net/bond*")]

    ## bondDevices = [os.path.basename(device) for device in glob.glob("/sys/class/net/bond*")]
    ## for deviceName in bondDevices:
    ##     if deviceName in linkedBondList:
    ##         if deviceName in sysConfigDeviceList:
    ##             deviceList[deviceName] = sysConfigDeviceList[deviceName]
    ##         else:
    ##             deviceList[deviceName] = {'device':deviceName, 'onboot':'no', 'bootproto':'none'}
    ##         continue
    ##     if len(ethDevices) > 2:
    ##         deviceList[deviceName] = {'device':deviceName, 'onboot':'no', 'bootproto':'none'}
