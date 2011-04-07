#!/usr/bin/python
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
from XmlHandler import ResponseXml
from optparse import OptionParser
import Utils

def stripEmptyLines(content):
    ret = ""
    for line in content.split("\n"):
        if line.strip() != "":
            ret += line
    return ret

def createDirectory(disk, volumename):
    dirname = "/export"
    if not os.path.isdir(dirname) or not os.path.isdir(disk):
        rs = ResponseXml()
        rs.appendTagRoute("code", 1)
        rs.appendTagRoute("message", "Disk is not mounted properly")
        return rs.toprettyxml()
    
    
    if not os.path.isdir(dirname + "/" + disk + "/" + volumename + "/"):
        command = "mkdir " + volumename; 
        rv = Utils.runCommandFG(command, stdout=True, root=True)
        message = stripEmptyLines(rv["Stdout"])
        if rv["Stderr"]:
            message += "Error: [" + stripEmptyLines(rv["Stderr"]) + "]" 
        rs = ResponseXml()
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

def main(disk, volumename):
    return createDirectory(disk, volumename)