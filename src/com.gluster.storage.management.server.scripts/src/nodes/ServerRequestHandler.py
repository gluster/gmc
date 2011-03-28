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

import Commands
from Protocol import *
from Globals import *
from GetServerNetworkConfig import *

def handleRequestGetServerNetworkConfig(requestDom):
    return getServerNetworkConfig(requestDom)

def handleRequest(requestString):
    log("Received request %s" % repr(requestString))

    requestDom = XDOM()
    requestDom.parseString(requestString)
    if not requestDom:
        log("Invalid request")
        return None

    preRequestMap = {}

    postRequestMap = {}

    cleanupRequestMap = {}

    requestMap = { Commands.COMMAND_GET_SERVER_NETWORK_CONFIG        : handleRequestGetServerNetworkConfig }

    messageId = requestDom.getMessageId()
    if not messageId:
        log("Invalid message Id")
        return None

    requestCommand = requestDom.getRequestCommand()
    if not requestCommand:
        log("invalid request command")
        return None

    requestAction = requestDom.getRequestAction()
    version = requestDom.getVersion()
    #if not isSupportedVersion(version):
    #    log("Unsupported version request %s" % requestDom.toxml())
    #    return ResponseXml(requestCommand, "Unsupported version request", messageId, version).toxml()

    try:
        if not requestAction:
            responseDom = requestMap[requestCommand](requestDom)
        elif requestAction.upper() == "PRE":
            responseDom = preRequestMap[requestCommand](requestDom)
        elif requestAction.upper() == "POST":
            responseDom = postRequestMap[requestCommand](requestDom)
        elif requestAction.upper() == "CLEANUP":
            responseDom = cleanupRequestMap[requestCommand](requestDom)
        else:
            log("Unknown request action %s" % requestAction)
            return None
        return responseDom.toxml()
    except KeyError:
        log("No handler found for command %s for action %s" % (requestCommand, requestAction))
        return ResponseXml(requestCommand, "Invalid command", messageId, version).toxml()
