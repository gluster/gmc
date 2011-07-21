#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Console.
#

import os
import sys
import Utils

def main():
    if len(sys.argv) != 1:
        sys.stderr.write("usage: %s\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    responseDom = ResponseXml()
    if Utils.runCommand("pidof glusterd") == 0:
        print "ONLINE"
    else:
        print "OFFLINE"
    print responseDom.toxml()
    sys.exit(0)

if __name__ == "__main__":
    main()
