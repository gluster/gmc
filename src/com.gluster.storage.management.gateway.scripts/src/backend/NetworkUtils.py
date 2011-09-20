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

def readResolvConfFile(fileName=None):
    nameServerList = []
    domain = None
    searchDomain = None
    if not fileName:
        fileName = Globals.RESOLV_CONF_FILE
    lines = Utils.readFile(fileName, lines=True)
    for line in lines:
        tokens = line.split("#")[0].strip().split()
        if len(tokens) < 2:
            continue
        if tokens[0].upper() == "NAMESERVER":
            nameServerList.append(tokens[1])
            continue
        if tokens[0].upper() == "DOMAIN":
            domain = tokens[1:]
            continue
        if tokens[0].upper() == "SEARCH":
            searchDomain = tokens[1:]
            continue
    return nameServerList, domain, searchDomain


def readIfcfgConfFile(deviceName, root=""):
    conf = {}
    fileName = "%s%s/ifcfg-%s" % (root, Globals.SYSCONFIG_NETWORK_DIR, deviceName)
    lines = Utils.readFile(fileName, lines=True)
    for line in lines:
        tokens = line.split("#")[0].split("=")
        if len(tokens) != 2:
            continue
        conf[tokens[0].strip().lower()] = tokens[1].strip()
    return conf


def getBondMode(deviceName, fileName=None):
    if not fileName:
        fileName = Globals.MODPROBE_CONF_FILE
    lines = Utils.readFile(fileName, lines=True)
    for line in lines:
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


def getNetDeviceList(root=""):
    netDeviceList = {}
    for deviceName in os.listdir("/sys/class/net/"):
        netDevice = {}
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

        netDevice["device"] = deviceName
        netDevice["description"] = deviceName
        netDevice["hwaddr"] = Utils.readFile("/sys/class/net/%s/address" % deviceName).strip()

        rv = Utils.runCommand("ifconfig %s" % deviceName, output=True)
        if rv["Status"] == 0:
            for line in rv["Stdout"].split("\n"):
                if line.find("Link encap:") != -1:
                    netDevice["type"] = line.split("Link encap:")[1].split()[0]
                    continue
                if line.find("inet addr:") != -1:
                    tokens = line.split("inet addr:")[1].split()
                    netDevice["ipaddr"] = tokens[0]
                    #print tokens[1].split(":")[1]
                    netDevice["netmask"] = tokens[2].split(":")[1]

        rv = Utils.runCommand("ethtool %s" % deviceName, output=True, root=True)
        if rv["Status"] == 0:
            for line in rv["Stdout"].split("\n"):
                if line.find("Speed: ") != -1:
                    netDevice["speed"] = line.split("Speed: ")[1].upper().split("MB")[0]
                elif line.find("Link detected: ") != -1:
                    netDevice["link"] = line.split("Link detected: ")[1]

        rv = Utils.runCommand("route -n", output=True, root=True)
        if rv["Status"] == 0:
            for line in rv["Stdout"].split("\n"):
                tokens = line.split()
                if len(tokens) == 8 and tokens[-1] == deviceName and tokens[3] == "UG":
                    netDevice["gateway"] = tokens[1]

        netDevice["mode"] = getBondMode(deviceName, root + Globals.MODPROBE_CONF_FILE)

        netDeviceList[deviceName] = netDevice

        conf = readIfcfgConfFile(deviceName, root)
        if not conf:
            continue
        try:
            if not netDevice["ipaddr"]:
                netDevice["ipaddr"] = conf["ipaddr"]
            if not netDevice["netmask"]:
                netDevice["netmask"] = conf["netmask"]
            if not netDevice["gateway"]:
                netDevice["gateway"] = conf["gateway"]
            netDevice["onboot"] = conf["onboot"]
            netDevice["bootproto"] = conf["bootproto"]
            netDevice["peerdns"] = conf["peerdns"]
            netDevice["autodns"] = conf["autodns"]
            netDevice["dns1"] = conf["dns1"]
            netDevice["dns2"] = conf["dns2"]
            netDevice["dns3"] = conf["dns3"]
            netDevice["master"] = conf["master"]
            netDevice["slave"] = conf["slave"]
            netDevice["nmcontrolled"] = conf["nmcontrolled"]
        except KeyError, e:
            pass
    return netDeviceList
