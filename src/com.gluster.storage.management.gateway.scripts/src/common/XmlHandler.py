#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
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
        return rc

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

    def createResponseTag(self):
        responseTag = self._domObj.createElement("response")
        return responseTag
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
    _responseTag = None
    def __init__(self):
        XDOM.__init__(self)
        self._responseTag = self.createResponseTag()
        self._domObj.appendChild(self._responseTag)

    @classmethod
    def errorResponse(self, message):
        if not self.responseTag:
            return False
        self.appendTagRoute("status.code", "-1");
        self.appendTagRoute("status.message", message)

    def append(self, tagName, tagValue=None):
        if not self._responseTag:
            return False
        tag = self.createTag(tagName, tagValue)
        if tag:
            self._responseTag.appendChild(tag)
            return True
        return False

    def appendTag(self, tag):
        if not tag:
            return False
        if not self._responseTag:
            return False
        self._responseTag.appendChild(tag)
        return True

    def appendTagRoute(self, tagRoute, value=None):
        if not self._responseTag:
            return None
        if not tagRoute:
            return None

        parentTagE = self._responseTag

        tagNameList = tagRoute.split(".")
        newTagRoute = tagNameList.pop(-1)

        for i in range(len(tagNameList), 0, -1):
            tagE = self.getElementsByTagRoute(".".join(["response"] + tagNameList[:i]))
            if tagE:
                parentTagE = tagE[0]
                break
            newTagRoute = tagNameList[i-1] + "." + newTagRoute

        newTagE = self.createTagRoute(newTagRoute, value)
        if not newTagE:
            return None
        try:
            parentTagE.appendChild(newTagE)
        except xml.dom.HierarchyRequestErr, e:
            Utils.log("error occured.  %s" + str(e))
            return None
        return newTagE

    def appendTagRouteOld(self, tagRoute, value=None):
        if not self._responseTag:
            return False
        if not tagRoute:
            return False

        parentTagE = self._responseTag

        tagNameList = tagRoute.split(".")
        newTagRoute = tagNameList.pop(-1)

        for i in range(len(tagNameList), 0, -1):
            tagE = self.getElementsByTagRoute(".".join(["response"] + tagNameList[:i]))
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
##--end of ResponseXml

def test():
    rs = ResponseXml()
    rs.appendTagRoute("status.code", "0"); 
    rs.appendTagRoute("status.message", "SUCCESS")
    serverTag = rs.appendTagRoute("server.name", "Server1")
    networkInterfaces  = rs.appendTagRoute("server.networkInterfaces", None)
    networkTag = rs.createTag("networkInterface", None)
    networkTag.appendChild(rs.createTag("name", "interface1"))
    networkTag.appendChild(rs.createTag("ipaddress", "192.168.1.40"))
    networkInterfaces.appendChild(networkTag)
    networkTag = rs.createTag("networkInterface", None)
    networkTag.appendChild(rs.createTag("name", "interface2"))
    networkTag.appendChild(rs.createTag("ipaddress", "192.168.1.41"))
    networkInterfaces.appendChild(networkTag)
    print rs.toprettyxml()

#test()
