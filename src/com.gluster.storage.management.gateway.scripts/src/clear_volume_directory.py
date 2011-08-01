#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
import syslog
import time
from XmlHandler import ResponseXml
import DiskUtils
import Utils
from optparse import OptionParser

def main():
    parser = OptionParser()
    parser.add_option("-d", "--delete", dest="todelete", action="store_true", default=False, help="force delete")
    (options, args) = parser.parse_args()

    if len(args) != 1:
        sys.stderr.write("usage: %s VOLUME_PATH [-d/--delete]\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeDirectory = args[0]
    if not os.path.exists(volumeDirectory):
        sys.stderr.write("Given volume directory path:%s does not exists" % volumeDirectory)
        sys.exit(1)

    newVolumeDirectoryName = "%s_%s" % (volumeDirectory, time.time())
    rv = Utils.runCommand("mv -f %s %s" % (volumeDirectory, newVolumeDirectoryName), output=True, root=True)
    if rv["Stderr"]:
        sys.stderr.write(Utils.stripEmptyLines(rv["Stderr"]))
        sys.exit(2)

    if not options.todelete:
        sys.exit(0)

    rv = Utils.runCommand("rm -fr %s" % newVolumeDirectoryName, output=True, root=True)
    if rv["Stderr"]:
        sys.stderr.write(Utils.stripEmptyLines(rv["Stderr"]))
        sys.exit(3)

    sys.exit(0)

if __name__ == "__main__":
    main()
