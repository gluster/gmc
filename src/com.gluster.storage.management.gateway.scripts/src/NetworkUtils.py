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

import sys

if not "/usr/share/system-config-network/" in sys.path:
    sys.path.append("/usr/share/system-config-network")

import os
import tempfile
import Globals

from Utils import *
#from netconfpkg.NCHardwareList import getHardwareList

def readHostFile(fileName=None):
    hostEntryList = []
    if not fileName:
        fileName = "/etc/hosts"
    try:
        for line in open(fileName):
            tokens = line.split("#")[0].strip().split()
            if len(tokens) < 2:
                continue
            hostEntryList.append({tokens[0] : tokens[1:]})
        return hostEntryList
    except IOError:
        log("failed to read %s file" % fileName)
        return None


def writeHostFile(hostEntryList, fileName=None):
    if fileName:
        hostFile = fileName
    else:
        hostFile = tempfile.mktemp(prefix="GSPSA")
    try:
        fp = open(hostFile, "w")
        for host in hostEntryList:
            fp.write("%s\t%s\n" % (host.keys()[0], " ".join(host.values()[0])))
        fp.close()
        if hostFile == fileName:
            return True
    except IOError:
        log("failed to write %s file" % hostFile)
        return False
    if runCommandFG("mv -f %s /etc/hosts" % hostFile, root=True) != 0:
        log("failed to rename file %s to /etc/hosts" % hostFile)
        return False
    return True


def readResolvConfFile(fileName=None, includeLocalHost=False):
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
    except IOError:
        log("failed to read %s file" % fileName)
        return None, None, None


def writeResolvConfFile(nameServerList, domain, searchDomain, fileName=None, appendLocalHost=True):
    if fileName:
        resolvConfFile = fileName
    else:
        resolvConfFile = tempfile.mktemp(prefix="GSPSA")
    try:
        fp = open(resolvConfFile, "w")
        if appendLocalHost:
            fp.write("nameserver 127.0.0.1\n")
        for nameServer in nameServerList:
            fp.write("nameserver %s\n" % nameServer)
        if domain:
            fp.write("domain %s\n" % " ".join(domain))
        if searchDomain:
            fp.write("search %s\n" % " ".join(searchDomain))
        fp.close()
        if resolvConfFile == fileName:
            return True
    except IOError:
        log("failed to write %s file" % resolvConfFile)
        return False
    if runCommandFG("mv -f %s %s" % (resolvConfFile, Globals.RESOLV_CONF_FILE), root=True) != 0:
        log("failed to rename file %s to %s" % (resolvConfFile, Globals.RESOLV_CONF_FILE))
        return False
    return True


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
    except IOError:
        log("failed to read %s file" % fileName)
        return None


def writeIfcfgConfFile(deviceName, conf, root="", deviceFile=None):
    if not deviceFile:
        deviceFile = "%s%s/ifcfg-%s" % (root, Globals.SYSCONFIG_NETWORK_DIR, deviceName)
    if root:
        ifcfgConfFile = deviceFile
    else:
        ifcfgConfFile = tempfile.mktemp(prefix="GSPSA")
    try:
        fp = open(ifcfgConfFile, "w")
        for key in conf.keys():
            if key == "description":
                fp.write("#%s=%s\n" % (key.upper(), conf[key]))
                continue
            if key in ['link', 'mode']:
                continue
            if conf["device"].startswith("bond") and key in ['hwaddr', 'master', 'slave']:
                continue
            if key == "slave" and conf['master']:
                fp.write("SLAVE=yes\n")
                continue
            if key == "onboot":
                if conf[key] == True:
                    fp.write("ONBOOT=yes\n")
                elif isString(conf[key]) and conf[key].upper() == "YES":
                    fp.write("ONBOOT=yes\n")
                else:
                    fp.write("ONBOOT=no\n")
                continue
            if not conf[key]:
                continue
            fp.write("%s=%s\n" % (key.upper(), conf[key]))
        fp.close()
        if ifcfgConfFile == deviceFile:
            return True
    except IOError:
        log("failed to write %s file" % ifcfgConfFile)
        return False
    if runCommandFG("mv -f %s %s" % (ifcfgConfFile, deviceFile), root=True) != 0:
        log("failed to rename file %s to %s" % (ifcfgConfFile, deviceFile))
        return False
    return True

def getNetDeviceDetail(deviceName):
    deviceDetail = {}
    deviceDetail['Name'] = deviceName
    rv = runCommandFG("ifconfig %s" % deviceName, stdout=True, root=True)
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
                except IndexError:
                    pass
            break
    return deviceDetail

def getNetDeviceGateway(deviceName):
    rv = runCommand("route -n", output=True, root=True)
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
    rv = runCommandFG("ethtool %s" % deviceName, stdout=True, root=True)
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
    rv = runCommandFG("ethtool %s" % deviceName, stdout=True, root=True)
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
    except IOError:
        log("failed to read %s file" % fileName)
        return None


def setBondMode(deviceName, mode, fileName=None):
    if not fileName:
        fileName = Globals.MODPROBE_CONF_FILE
    tempFileName = getTempFileName()
    try:
        fp = open(tempFileName, "w")
        lines = open(fileName).readlines()
    except IOError:
        log("unable to open file %s" % Globals.MODPROBE_CONF_FILE)
        return False
    for line in lines:
        tokens = line.split()
        if len(tokens) > 1 and "OPTIONS" == tokens[0].upper() and "BOND" in tokens[1].upper() and deviceName == tokens[1]:
            fp.write("options %s max_bonds=2 mode=%s miimon=100\n" % (deviceName, mode))
            deviceName = None
            continue
        fp.write(line)
    if deviceName:
        fp.write("alias %s bonding\n" % deviceName)
        fp.write("options %s max_bonds=2 mode=%s miimon=100\n" % (deviceName, mode))
    fp.close()
    if runCommandFG(["mv", "-f", tempFileName, fileName], root=True) != 0:
        log("unable to move file from %s to %s" % (tempFileName, fileName))
        return False
    return True

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
        except IOError:
            pass
        
        netDeviceList.append(netDevice)

        conf = readIfcfgConfFile(deviceName, root)
        if not conf:
            continue
        try:
            netDevice["onboot"] = conf["onboot"]
        except KeyError:
            pass
        try:
            netDevice["bootproto"] = conf["bootproto"]
        except KeyError:
            pass
        if conf.has_key("ipaddr") and conf["ipaddr"]:
            netDevice["ipaddr"] = conf["ipaddr"]
        try:
            netDevice["netmask"] = conf["netmask"]
        except KeyError:
            pass
        if conf.has_key("gateway") and conf["gateway"]:
            netDevice["gateway"] = conf["gateway"]
        else:
            netDevice["gateway"] = getNetDeviceGateway(deviceName)
        try:
            netDevice["peerdns"] = conf["peerdns"]
        except KeyError:
            pass
        try:
            netDevice["autodns"] = conf["autodns"]
        except KeyError:
            pass
        try:
            netDevice["dns1"] = conf["dns1"]
        except KeyError:
            pass
        try:
            netDevice["dns2"] = conf["dns2"]
        except KeyError:
            pass
        try:
            netDevice["dns3"] = conf["dns3"]
        except KeyError:
            pass
        try:
            netDevice["master"] = conf["master"]
        except KeyError:
            pass
        try:
            netDevice["slave"] = conf["slave"]
        except KeyError:
            pass
        try:
            netDevice["nmcontrolled"] = conf["nmcontrolled"]
        except KeyError:
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


def configureDhcpServer(serverIpAddress, dhcpIpAddress):
    tmpDhcpConfFile = tempfile.mktemp(prefix="GSPSA")

    serverPortString = "68"
    try:
        for arg in open("/proc/cmdline").read().strip().split():
            token = arg.split("=")
            if token[0] == "dhcp":
                serverPortString = token[1]
                break
    except IOError:
        log(syslog.LOG_ERR, "Failed to read /proc/cmdline.  Continuing with default port 68")
    try:
        serverPort = int(serverPortString)
    except ValueError:
        log(syslog.LOG_ERR, "Invalid dhcp port '%s' in /proc/cmdline.  Continuing with default port 68" % serverPortString)
        serverPort = 68

    try:
        fp = open(tmpDhcpConfFile, "w")
        fp.write("bind-interfaces\n")
        fp.write("except-interface=lo\n")
        fp.write("dhcp-range=%s,%s\n" % (dhcpIpAddress, dhcpIpAddress))
        fp.write("dhcp-lease-max=1\n")
        fp.write("dhcp-alternate-port=%s\n" % serverPort)
        fp.write("dhcp-leasefile=%s\n" % Globals.DNSMASQ_LEASE_FILE)
        #fp.write("server=%s\n" % serverIpAddress)
        #fp.write("dhcp-script=/usr/sbin/server-info\n")
        fp.close()
    except IOError:
        log(syslog.LOG_ERR, "unable to write dnsmasq dhcp configuration %s" % tmpDhcpConfFile)
        return False
    if runCommandFG("mv -f %s %s" % (tmpDhcpConfFile, Globals.DNSMASQ_DHCP_CONF_FILE), root=True) != 0:
        log(syslog.LOG_ERR, "unable to copy dnsmasq dhcp configuration to %s" % Globals.DNSMASQ_DHCP_CONF_FILE)
        return False
    return True


def isDhcpServer():
    return os.path.exists(Globals.DNSMASQ_DHCP_CONF_FILE)


def getDhcpServerStatus():
    if runCommandFG("service dnsmasq status", root=True) == 0:
        return True
    return False


def startDhcpServer():
    if runCommandFG("service dnsmasq start", root=True) == 0:
        return True
    return False


def stopDhcpServer():
    if runCommandFG("service dnsmasq stop", root=True) == 0:
        runCommandFG("rm -f %s" % Globals.DNSMASQ_LEASE_FILE, root=True)
        return True
    return False


def restartDhcpServer():
    stopDhcpServer()
    runCommandFG("rm -f %s" % Globals.DNSMASQ_LEASE_FILE, root=True)
    return startDhcpServer()


def reloadDhcpServer():
    if runCommandFG("service dnsmasq reload", root=True) == 0:
        return True
    return False
