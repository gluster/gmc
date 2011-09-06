#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import xml
import xml.parsers.expat
import xml.dom.minidom as MDOM
import os
import Globals
import copy
import Utils

XML_STRING = 0
XML_FILE = 1

class XDOM:
    _domObj = None

    def __init__(self):
        self._domObj = MDOM.Document()
        return

    @classmethod
    def getText(self, nodeList):
        rc = ""
        for node in nodeList:
            if node.nodeType == node.TEXT_NODE:
                rc = rc + node.data
        return rc.strip()

    def parseString(self, requestString):
        try:
            self._domObj = MDOM.parseString(requestString)
        except xml.parsers.expat.ExpatError, e:
            Utils.log("XML string parse error: %s" % str(e))
            return False
        return True

    def parseFile(self, fileName):
        try:
            self._domObj = MDOM.parse(fileName)
        except IOError, e:
            Utils.log("error reading file: %s" % str(e))
            return False
        except xml.parsers.expat.ExpatError, e:
            Utils.log("XML file %s parse error: %s" % (fileName, str(e)))
            return False
        return True

    def setDomObj(self, dom):
        if dom and type(dom) != type([]):
            self._domObj = dom
            return True
        return False

    def createTextNode(self, text):
        if not self._domObj:
            return False
        if not text:
            return False
        return self._domObj.createTextNode(str(text))

    def createTag(self, tag, text=None):
        if not self._domObj:
            return None
        if tag == None:
            return None

        tagE = self._domObj.createElement(str(tag))
        if text:
            tagEText = self._domObj.createTextNode(str(text))
            tagE.appendChild(tagEText)
        return tagE

    def addTag(self, tag):
        if not self._domObj:
            return False
        if not tag:
            return False

        self._domObj.appendChild(tag)
        return True

    def createTagRoute(self, tagRoute, text=None):
        if not tagRoute:
            return False

        tagList = tagRoute.split(".")
        tag = None
        previousTag = None
        for tagName in tagList[:-1]:
            newTag = self.createTag(tagName, None)
            if not tag:
                tag = newTag
                previousTag = newTag
                continue
            previousTag.appendChild(newTag)
            previousTag = newTag

        if previousTag:
            previousTag.appendChild(self.createTag(tagList[-1], text))
        else:
            tag = self.createTag(tagList[-1], text)
        return tag

    def appendTagRoute(self, tagRoute, value=None):
        if not self._domObj:
            return False
        if not tagRoute:
            return False

        parentTagE = self._domObj

        tagNameList = tagRoute.split(".")
        newTagRoute = tagNameList.pop(-1)

        for i in range(len(tagNameList), 0, -1):
            tagE = self.getElementsByTagRoute(".".join(tagNameList[:i]))
            if tagE:
                parentTagE = tagE[0]
                break
            newTagRoute = tagNameList[i-1] + "." + newTagRoute

        newTagE = self.createTagRoute(newTagRoute, value)
        if not newTagE:
            return False
        try:
            parentTagE.appendChild(newTagE)
        except xml.dom.HierarchyRequestErr, e:
            Utils.log("error occured.  %s" + str(e))
            return False
        return True

    def setTextByTagRoute(self, tagRoute, tagValue):
        if not self._domObj:
            return None

        if not tagRoute:
            return None

        tagE  = self.getElementsByTagRoute(tagRoute)
        if not tagE:
            return False

        parentTagE = self.getElementsByTagRoute(".".join(tagRoute.split(".")[:-1]))
        if not parentTagE:
            return False
        
        parentTagE[0].childNodes.remove(tagE[0])
        parentTagE[0].appendChild(self.createTag(tagRoute.split(".")[-1], tagValue))
        return True

    def getElementsByTagRoute(self, tagRoute):
        if not self._domObj:
            return None
        
        if not tagRoute:
            return None

        x = None
        for tag in tagRoute.split("."):
            if x is None:
                x = self._domObj.getElementsByTagName(tag)
                continue
            if x == []:
                break
            x = x[0].getElementsByTagName(tag)
        return x

    def getTextByTagRoute(self, tagRoute):
        if not self._domObj:
            return None

        x = self.getElementsByTagRoute(tagRoute)
        if x:
            return self.getText(x[0].childNodes)
        return None

    def getElementsByTagName(self, name):
        if not self._domObj:
            return None
        return self._domObj.getElementsByTagName(name)

    def writexml(self, fileName, indent="", addindent="", newl=""):
        if not self._domObj:
            return None
        try:
            fp = open(fileName, "w")
            self._domObj.writexml(fp, indent, addindent, newl)
            fp.close()
            return True
        except IOError:
            return False

    def toString(self, indent="  ", newl="\n", encoding = None):
        if not self._domObj:
            return None
        return self._domObj.toprettyxml(indent, newl, encoding)

    def toxml(self, encoding = None):
        if not self._domObj:
            return None
        return self._domObj.toxml(encoding)

    def toprettyxml(self, indent="  ", newl="\n", encoding = None):
        return self.toString(indent, newl, encoding)

    def getAttribute(self, attributeName):
        if not attributeName:
            return None
        try:
            return self.getElementsByTagName("command")[0].getAttribute(attributeName)
        except IndexError:
            return False

    def setAttribute(self, attributeName, attributeValue):
        if not (attributeName and attributeValue):
            return None
        try:
            return self.getElementsByTagName("command")[0].setAttribute(attributeName, attributeValue)
        except IndexError:
            return False

    def getRequestCommand(self):
        return self.getAttribute("request")

    def getResponseCommand(self):
        return self.getAttribute("response")

    def getResponseCode(self):
        return self.getAttribute("response-code")

    def getMessageId(self):
        return self.getAttribute("id")

    def getVersion(self):
        return self.getAttribute("version")

    def getRequestAction(self):
        return self.getAttribute("action")

    def setVersion(self, value):
        return self.setAttribute("version", value)

    def setRequestAction(self, value):
        return self.setAttribute("action", value)
            
    def createCommandTag(self, command, responseCode, id, version=Globals.GLUSTER_PLATFORM_VERSION):
        commandTag = self._domObj.createElement("command")
        commandTag.setAttribute("response", command)
        commandTag.setAttribute("response-code", responseCode)
        commandTag.setAttribute("id", id)
        commandTag.setAttribute("version", version)
        return commandTag
##--end of XDOM

class RequestXml(XDOM):
    def __init__(self, requestString, type=None):
        if None == requestString:
            XDOM.__init__(self)
            return
        try:
            if None == type:
                if os.path.isfile(requestString):
                    self._domObj = MDOM.parse(requestString)
                else:
                    self._domObj = MDOM.parseString(requestString)
            elif XML_FILE == type:
                self._domObj = MDOM.parse(requestString)
            elif XML_STRING == type:
                self._domObj = MDOM.parseString(requestString)
        except IOError:
            XDOM.__init__(self)
        except xml.parsers.expat.ExpatError:
            XDOM.__init__(self)

##--end of RequestXML

class ResponseXml(XDOM):
    _commandTag = None
    def __init__(self, command, responseCode, id, version=Globals.GLUSTER_PLATFORM_VERSION):
        XDOM.__init__(self)
        if command and responseCode and id:
            self._commandTag = self.createCommandTag(command, responseCode, id, version)
            self._domObj.appendChild(self._commandTag)

    def appendCommand(self, command, responseCode, id, version=Globals.GLUSTER_PLATFORM_VERSION):
        if command and responseCode and id:
            self._commandTag = self.createCommandTag(command, responseCode, id, version)
            self._domObj.appendChild(self._commandTag)
            return True
        return False

    def append(self, tagName, tagValue=None):
        if not self._commandTag:
            return False
        tag = self.createTag(tagName, tagValue)
        if tag:
            self._commandTag.appendChild(tag)
            return True
        return False

    def appendTag(self, tag):
        if not tag:
            return False
        if not self._commandTag:
            return False
        self._commandTag.appendChild(tag)
        return True

    def appendTagRoute(self, tagRoute, value=None):
        if not self._commandTag:
            return False
        if not tagRoute:
            return False

        parentTagE = self._commandTag

        tagNameList = tagRoute.split(".")
        newTagRoute = tagNameList.pop(-1)

        for i in range(len(tagNameList), 0, -1):
            tagE = self.getElementsByTagRoute(".".join(["command"] + tagNameList[:i]))
            if tagE:
                parentTagE = tagE[0]
                break
            newTagRoute = tagNameList[i-1] + "." + newTagRoute

        newTagE = self.createTagRoute(newTagRoute, value)
        if not newTagE:
            return False
        try:
            parentTagE.appendChild(newTagE)
        except xml.dom.HierarchyRequestErr, e:
            Utils.log("error occured.  %s" + str(e))
            return False
        return True

    def appendTagRouteOld(self, tagRoute, value=None):
        if not tagRoute:
            return False
        if not self._commandTag:
            return False
        
        tmpTagRoute = ""
        previousTagE = self._commandTag
        tagE = None
        for tagName in tagRoute.split("."):
            if not tmpTagRoute:
                tagE = self.getElementsByTagRoute("command." + tagName)
            else:
                tagE = self.getElementsByTagRoute("command." + tmpTagRoute + "." + tagName)
            if not tagE:
                break
            if len(tagE) != 1:
                return False
            previousTagE = tagE[0]
            if not tmpTagRoute:
                tmpTagRoute = tagName
            else:
                tmpTagRoute = tmpTagRoute + "." + tagName

        if tmpTagRoute == tagRoute:
            return False
        newTagRoute = tagRoute[len(tmpTagRoute):]
        if newTagRoute[0] == '.':
            newTagRoute = newTagRoute[1:]

        if previousTagE.childNodes and previousTagE.childNodes[0].nodeType == previousTagE.TEXT_NODE:
            return False
        previousTagE.appendChild(self.createTagRoute(newTagRoute, value))
        return True
##--end of ResponseXml

def test():
    #volumes = RequestXml(VolumeFile, XML_FILE).getElementsByTagRoute("volume-list.volume")
    requestStr = '''<command request="create-volume" id="123" version="3.1">
<volume>
<name>movies1</name>
<type>cluster mirror</type>
<start>512000</start>
<server>zresearch</server>
<vacl>192.168.20.*</vacl>
<vacl>192.168.30.*</vacl>
<nfs>
<export>no</export>
</nfs>
<cifs>
<export>no</export>
</cifs>
<webdav>
<export>no</export>
</webdav>
</volume>
</command>'''

    requestXml = RequestXml(requestStr)
    print requestXml.getAttribute("")

def test1():
    rs = ResponseXml("create-volume", "OK", "xyz")
    rs.appendTagRoute("volume.detail.name", "music")
    print rs.toprettyxml()
    rs.append("volume", "data")
    print rs.toprettyxml()
    rs.appendTagRoute("volume.detail.ipaddr", "192.168.10.1")
    print rs.toprettyxml()
    print rs.appendTagRoute("volume.detail.ipaddr.v6", "ff:ff::ff::")
    print rs.toprettyxml()

    print rs.getTextByTagRoute("command.volume.detail")

def test2():
    rs = ResponseXml("download-volume-logs", "OK", "xyz")
    te = rs.createTag("interface", None)
    te.appendChild(rs.createTag("device", "DEVICE1"))
    te.appendChild(rs.createTag("description", "my device one"))
    rs.appendTag(te)

    te = rs.createTag("interface", None)
    te.appendChild(rs.createTag("device", "DEVICE2"))
    te.appendChild(rs.createTag("description", "my device two"))
    rs.appendTag(te)
    print rs.toprettyxml()

