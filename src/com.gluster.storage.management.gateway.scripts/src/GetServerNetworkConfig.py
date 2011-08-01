#  Copyright (C) 2009 Gluster, Inc. <http://www.gluster.com>
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
import syslog
import Globals
import Commands
import re
from ServerUtils import *
from Protocol import *
from NetworkUtils import *

def getServerNetworkConfig(requestXml):
    serverName = requestXml.getTextByTagRoute("command.server-name")
    version = requestXml.getVersion()
    messageId = requestXml.getAttribute("id")

    if not serverName:
        responseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG, "No server name given", messageId, version)
        responseDom.appendTagRoute("server.name", serverName)
        return responseDom
    responseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG, "OK", messageId, version)
    serverTag = responseDom.createTag("server", None)
    serverTag.appendChild(responseDom.createTag("name", serverName))
    nameServerList, domain, searchDomain = readResolvConfFile()
    if domain:
        domainName = domain[0]
    else:
        domainName = None
    serverTag.appendChild(responseDom.createTag("domainname", domainName))
    i = 1
    for dns in nameServerList:
        serverTag.appendChild(responseDom.createTag("dns%s" % i, dns))
        i += 1
    #TODO: probe and retrieve timezone, ntp-server, preferred-network details and update the tags
    configDom = XDOM()
    if configDom.parseFile("%s/%s/network.xml" % (Globals.SERVER_CONF_DIR, serverName)):
        serverTag.appendChild(responseDom.createTag("timezone", configDom.getTextByTagRoute("network.timezone")))
        serverTag.appendChild(responseDom.createTag("ntp-server", configDom.getTextByTagRoute("network.ntp-server")))
        preferredNetwork = configDom.getTextByTagRoute("network.preferred-network")
        if not preferredNetwork:
            preferredNetwork = "any"
        serverTag.appendChild(responseDom.createTag("preferred-network", preferredNetwork))

    deviceList = {}
    for device in getNetDeviceList():
        deviceList[device["device"]] = device
        try:
            macAddress = open("/sys/class/net/%s/address" % device["device"]).read().strip()
        except IOError:
            continue
        interfaceTag = responseDom.createTag("interface", None)
        interfaceTag.appendChild(responseDom.createTag("device",      device["device"]))
        interfaceTag.appendChild(responseDom.createTag("description", device["description"]))
        interfaceTag.appendChild(responseDom.createTag("hwaddr",      macAddress))
        if deviceList[device["device"]]:
            if deviceList[device["device"]]["onboot"]:
                interfaceTag.appendChild(responseDom.createTag("onboot", "yes"))
            else:
                interfaceTag.appendChild(responseDom.createTag("onboot", "no"))
            interfaceTag.appendChild(responseDom.createTag("bootproto", deviceList[device["device"]]["bootproto"]))
            interfaceTag.appendChild(responseDom.createTag("ipaddr",    deviceList[device["device"]]["ipaddr"]))
            interfaceTag.appendChild(responseDom.createTag("netmask",   deviceList[device["device"]]["netmask"]))
            interfaceTag.appendChild(responseDom.createTag("gateway",   deviceList[device["device"]]["gateway"]))
            if deviceList[device["device"]]["mode"]:
                interfaceTag.appendChild(responseDom.createTag("mode",   deviceList[device["device"]]["mode"]))
            if deviceList[device["device"]]["master"]:
                interfaceTag.appendChild(responseDom.createTag("bonding", "yes"))
                spliter = re.compile(r'[\D]')
                interfaceTag.appendChild(responseDom.createTag("bondid", spliter.split(device["master"])[-1]))            
        else:
            interfaceTag.appendChild(responseDom.createTag("onboot",    "no"))
            interfaceTag.appendChild(responseDom.createTag("bootproto", "none"))
        serverTag.appendChild(interfaceTag)
    responseDom.appendTag(serverTag)
    return responseDom

def test():
    requestString = """<command request="get-server-network-config" id="123" version="3.1.2.2">
<server-name>s1</server-name></command>"""
    requestDom = RequestXml(requestString)
    print getServerNetworkConfig(requestDom).toxml()
