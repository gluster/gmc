#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import time
import Utils
from optparse import OptionParser

def main():
    parser = OptionParser()
    parser.add_option("-d", "--delete", dest="todelete", action="store_true", default=False, help="force delete")
    (options, args) = parser.parse_args()

    if len(args) != 1:
        sys.stderr.write("usage: %s [-d | --delete] VOLUME_PATH\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeDirectory = args[0]
    if not os.path.exists(volumeDirectory):
        sys.stderr.write("Given volume directory path:%s does not exists\n" % volumeDirectory)
        sys.exit(1)

    if '/' == volumeDirectory[-1]:
        volumeDirectory = volumeDirectory[:-1]

    newVolumeDirectoryName = "%s_%s" % (volumeDirectory, time.time())
    if Utils.runCommand("mv -f %s %s" % (volumeDirectory, newVolumeDirectoryName), root=True) != 0:
        sys.stderr.write("Failed to rename volume directory\n")
        sys.exit(2)

    if options.todelete:
        process = Utils.runCommandBG("rm -fr %s" % newVolumeDirectoryName, root=True)
        if not process:
            sys.exit(3)
    sys.exit(0)


if __name__ == "__main__":
    main()
