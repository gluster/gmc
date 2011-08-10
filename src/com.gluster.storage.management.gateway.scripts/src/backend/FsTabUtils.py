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

import Globals

def readFsTab(fsTabFile=Globals.FSTAB_FILE):
    try:
        fsTabfp = open(fsTabFile)
    except IOError, e:
        log("readFsTab(): " + str(e))
        return None

    fsTabEntryList = []
    for line in fsTabfp:
        tokens = line.strip().split()
        if not tokens or tokens[0].startswith('#'):
            continue
        fsTabEntry = {}
        fsTabEntry["Device"] = None
        fsTabEntry["MountPoint"] = None
        fsTabEntry["FsType"] = None
        fsTabEntry["Options"] = None
        fsTabEntry["DumpOption"] = 0
        fsTabEntry["fsckOrder"] = 0
        try:
            fsTabEntry["Device"] = tokens[0]
            fsTabEntry["MountPoint"] = tokens[1]
            fsTabEntry["FsType"] = tokens[2]
            fsTabEntry["Options"] = tokens[3]
            fsTabEntry["DumpOption"] = tokens[4]
            fsTabEntry["fsckOrder"] = tokens[5]
        except IndexError:
            pass
        if fsTabEntry["Device"] and fsTabEntry["MountPoint"] and fsTabEntry["FsType"] and fsTabEntry["Options"]:
            fsTabEntryList.append(fsTabEntry)

    fsTabfp.close()
    return fsTabEntryList

def writeFsTab(fsTabEntryList, fsTabFile=Globals.FSTAB_FILE):
    try:
        fsTabfp = open(fsTabFile, "w")
        for fsTabEntry in fsTabEntryList:
            fsTabfp.write("%s\t%s\t%s\t%s\t%s\t%s\n" %
                          (fsTabEntry["Device"], fsTabEntry["MountPoint"],
                           fsTabEntry["FsType"], fsTabEntry["Options"],
                           fsTabEntry["DumpOption"], fsTabEntry["fsckOrder"]))
        fsTabfp.close()
    except IOError, e:
        log("writeFsTab(): " + str(e))
        return False
    return True

def addFsTabEntry(fsTabEntry, fsTabFile=Globals.FSTAB_FILE):
    try:
        fsTabfp = open(fsTabFile, "a")
        fsTabfp.write("%s\t%s\t%s\t%s\t%s\t%s\n" %
                      (fsTabEntry["Device"], fsTabEntry["MountPoint"],
                       fsTabEntry["FsType"], fsTabEntry["Options"],
                       fsTabEntry["DumpOption"], fsTabEntry["fsckOrder"]))
        fsTabfp.close()
    except IOError, e:
        log("addFsTabEntry(): " + str(e))
        return False
    return True

def removeFsTabEntry(fsTabEntry, fsTabFile=Globals.FSTAB_FILE):
    fsTabEntryList = readFsTab(fsTabFile)
    if not fsTabEntryList:
        return False

    try:
        fsTabEntryList.remove(fsTabEntry)
    except ValueError:
        return False

    return writeFsTab(fsTabEntryList, fsTabFile)

