#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import rrdtool
import os
from socket import gethostname
from itertools import groupby

class RRD:
   def __init__ (self):
      self.COLORS = [0xff7777, 0x7777ff, 0x55ff55, 0xffcc77, 0xff77ff, 0x77ffff,0xffff77, 0x55aaff] 
      self.HOST = gethostname()
      self.DIR = "/var/lib/collectd"

   def fade_component(self, component):
      return ((component + 255 * 5) / 6)
   
   def fade_color(self, color):
      r = 0;
      for i in [0,1,2]:
         shft = (i * 8)
         component = ((color >> shft) & 255)
         r |= (self.fade_component(component) << shft)
      return r

   def generate_pngs(self):
      
      rrdlist = os.popen ("find %s -type f -name '*.rrd'" % self.DIR)
      
      for rrd in rrdlist:
         self.dss = []
         self.defs = ""

         rrdinfo = rrdtool.info(rrd.strip())
         
         for key in rrdinfo.keys():
            if key.split('[')[0] == 'ds':
               self.dss.append(key.split('[')[1].split(']')[0])
               self.dss.sort()
   
         self.dss = [a for a,b in groupby(self.dss)]

         for ds in self.dss:
            self.defs = self.defs + " DEF:%s_avg=%s:%s:AVERAGE " % (ds, rrd.strip(), ds)
            self.defs = self.defs + " DEF:%s_max=%s:%s:MAX " % (ds, rrd.strip(), ds)

         j = 0
         for ds in self.dss:
            color = self.COLORS[j % len(self.COLORS)]
            j = j + 1
            faded_color = self.fade_color(color)
            self.defs = self.defs + " AREA:%s_max#%06x " % (ds, faded_color)
      
         j = 0
         for ds in self.dss:
            color = self.COLORS[j % len(self.COLORS)]
            j = j + 1
            self.defs = self.defs + " LINE2:%s_avg#%06x:%s " % (ds, color, ds)
            self.defs = self.defs + " GPRINT:%s_avg:AVERAGE:%%5.1lf%%sAvg " % ds
            self.defs = self.defs + " GPRINT:%s_max:MAX:%%5.1lf%%sMax " % ds

         for span in ['1hour', '1day', '1week', '1month']:
            os.system ("mkdir -p %s/%s" % (self.DIR, self.HOST))
            image = os.path.dirname(rrd.strip()) + "-" + span + ".png"
            cmd = "rrdtool graph " + image + " -t \"%s %s\"" % (os.path.dirname(rrd.strip()), span) + " --imgformat PNG --width 600 --height 100 --start now-" + span + " --end now --interlaced " + self.defs + " >/dev/null 2>&1"
            os.system(cmd)


def main ():
   
   rrd = RRD ()
   rrd.generate_pngs ()
      
if __name__ == "__main__":
    main()
