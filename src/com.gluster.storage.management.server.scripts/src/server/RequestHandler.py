import os
import glob
#import paramiko
import tempfile
#import uuid
import socket
import tarfile
import time
import Globals
import Commands
from Protocol import *
from RemoteExecute import *
from NetworkUtils import *

def handleRequestGetServerNetworkConfig(requestDom):
    messageId = requestDom.getAttribute("id")
    serverName = requestDom.getTextByTagRoute("command.server-name")
    version = requestDom.getVersion()
    request = requestDom.getAttribute("request")

    if not serverName:
        responseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG, "No server name given", messageId, version)
        responseDom.appendTagRoute("server.name", serverName)
        return responseDom

    #serverIpList = getExecuteServerList([serverName])
    #if not serverIpList:
    #    responseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG, "Unable to get server ip", messageId, version)
    #    responseDom.appendTagRoute("server.name", serverName)
    #    return responseDom

    successStatusDict, failureServerList, cleanupStatusDict = \
        execute({serverName:[serverName]}, requestDom, Globals.REQUEST_MAP[request]["cleanup"])
    if failureServerList:
        response = failureServerList[serverName]["StdOutString"]
        if not response:
            return ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG,
                               "Failed to execute get server network config", messageId, version)
        responseDom = XDOM()
        if responseDom.parseString(response):
            return responseDom
        errorResponseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG,
                           "Invalid response of get server network config", messageId, version)
        errorResponseDom.appendTagRoute("server.name", serverName)
        return errorResponseDom

    responseDom = XDOM()
    if not responseDom.parseString(successStatusDict[serverName]["StdOutString"]):
        errorResponseDom = ResponseXml(Commands.COMMAND_GET_SERVER_NETWORK_CONFIG,
                           "Invalid response of get server network config", messageId, version)
        errorResponseDom.appendTagRoute("server.name", serverName)
        return errorResponseDom

    #configDom = getServerNetworkConfigFromLocalFile(serverName)
    #if not (configDom and compareServerNetworkDom(configDom, responseDom)):
    #    updateServerNetworkConfigXmlFile(serverName, responseDom)
    #    syncConfiguration()
    return responseDom
