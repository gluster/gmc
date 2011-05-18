#  Copyright (c) 2009 Gluster, Inc. <http://www.gluster.com>
#  This file is part of GlusterSP.
#
#  GlusterSP is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterSP is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

import os
import dbus
from Common import *

class Disk:
    def __init__(self):
        """init"""

        self.volumes = []
        self.disks = []
        self.bus = dbus.SystemBus()
        self.hal_obj = self.bus.get_object("org.freedesktop.Hal",
                                      "/org/freedesktop/Hal/Manager")
        self.hal = dbus.Interface(self.hal_obj, "org.freedesktop.Hal.Manager")
        self.devices = []
        self.devices = self.hal.FindDeviceByCapability("storage")

        self.detect_disks()
        self.detect_mountable_volumes()

    def getDiskList(self):

        return self.disks

    def getMountableDiskList(self):

        return self.volumes

    def detect_disks(self):
        for device in self.devices:
            dev = self._get_device(device)
            if dev.GetProperty("storage.drive_type") != "cdrom":
                if not dev.GetProperty("block.is_volume"):
                    self._add_disks(dev)
                    continue

    def _add_disks(self, dev):
        disk = str(dev.GetProperty('block.device'))
        disk_size = str(int(dev.GetProperty('storage.size')) / 1024**2)

        try: 
            if dev.GetProperty('storage.removable'):
                disk_size = str(int(dev.GetProperty('storage.removable.media_size')) / 1024**2)
        except:
            return

        self.disks.append({
                'device': disk,
                'description': str(dev.GetProperty('storage.model')) + " " + str(dev.GetProperty('storage.vendor')),
                'interface': str(dev.GetProperty('storage.bus')),
                'size': disk_size,
                'drive_type': str(dev.GetProperty('storage.drive_type'))
                })

    def detect_mountable_volumes(self):
        """ Detect all mountable volumes using HAL via D-Bus """
        for device in self.devices:
            dev = self._get_device(device)
            if dev.GetProperty("storage.drive_type") != "cdrom":
                if dev.GetProperty("block.is_volume"):
                    self._add_volume(dev)
                    continue
                else: # iterate over children looking for a volume
                    children = self.hal.FindDeviceStringMatch("info.parent",
                                                         device)
                    for child in children:
                        child = self._get_device(child)
                        if child.GetProperty("block.is_volume"):
                            self._add_volume(child, parent=dev)
                            #break      # don't break, allow all partitions

    def _add_volume(self, dev, parent=None):
        volume = str(dev.GetProperty('block.device'))
        self.volumes.append ({
                'device'  : volume,
                'label'   : str(dev.GetProperty('volume.label')),
                'fstype'  : str(dev.GetProperty('volume.fstype')),
                'fsversion': str(dev.GetProperty('volume.fsversion')),
                'uuid'    : str(dev.GetProperty('volume.uuid')),
                'interface': str(parent.GetProperty('storage.bus')),
                'parent'  : str(parent.GetProperty('block.device')),
                'description': str(parent.GetProperty('storage.model')) + " " + str(parent.GetProperty('storage.vendor')),
                'size'    : str(int(dev.GetProperty('volume.size')) / 1024**2),
                'totalsize'    : str(int(parent.GetProperty('storage.size')) / 1024**2),
                'drive_type': str(parent.GetProperty('storage.drive_type')),
                'mount_point': str(dev.GetProperty('volume.mount_point'))
                })

    def _get_device(self, udi):
        """ Return a dbus Interface to a specific HAL device UDI """
        dev_obj = self.bus.get_object("org.freedesktop.Hal", udi)
        return dbus.Interface(dev_obj, "org.freedesktop.Hal.Device")

    def get_free_bytes(self, device=None):
        """ Return the number of available bytes on our device """
        import statvfs
        stat = os.statvfs(device)
        return stat[statvfs.F_BSIZE] * stat[statvfs.F_BAVAIL]

    def get_used_bytes(self, device=None):
        """ Return the number of used bytes on our device """
        import statvfs
        stat = os.statvfs(device)
        return ((stat[statvfs.F_BSIZE] * stat[statvfs.F_BLOCKS]) - (stat[statvfs.F_BSIZE] * stat[statvfs.F_BAVAIL]))
