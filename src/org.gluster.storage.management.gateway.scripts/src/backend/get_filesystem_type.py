#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Utils

SUPPORTED_FSTYPE = ['ext3', 'ext4', 'ext4dev', 'xfs']

def main():
    print "\n".join(list(set(Utils.getFileSystemType()).intersection(set(SUPPORTED_FSTYPE))))

if __name__ == "__main__":
    main()
