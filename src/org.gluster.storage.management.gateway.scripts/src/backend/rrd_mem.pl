#!/usr/bin/perl
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway (GlusterMG).
#
#  GlusterMG is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterMG is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
#

use RRDs;

my $rrdlog = '/var/lib/rrd';
my $graphs = '/var/lib/rrd';

updatememdata ();
#updatememgraph ('day');
#updatememgraph ('week');
#updatememgraph ('month');
#updatememgraph ('year');

sub updatememgraph {
    my $period    = $_[0];

    RRDs::graph ("$graphs/memory-$period.png",
                "--start", "-1$period", "-aPNG", "-i", "-z",
                "--alt-y-grid", "-w 300", "-h 50", "-l 0", "-u 100", "-r",
                "--color", "SHADEA#FFFFFF",
                "--color", "SHADEB#FFFFFF",
                "--color", "BACK#FFFFFF",
                "-t memory usage per $period",
                "DEF:used=$rrdlog/mem.rrd:memused:AVERAGE",
                "DEF:free=$rrdlog/mem.rrd:memfree:AVERAGE",
                "DEF:cache=$rrdlog/mem.rrd:memcache:AVERAGE",
                "CDEF:total=used,free,+",
                "CDEF:used1=used,buffer,cache,-,-",
                "CDEF:usedpct=100,used1,total,/,*",
                "CDEF:free1=total,used1,-",
                "CDEF:cachepct=100,cache,total,/,*",
                "CDEF:freepct=100,free1,total,/,*",
                "AREA:usedpct#0000FF:used memory\\j",
                "STACK:cachepct#FFFF00:cached memory\\j",
                "STACK:freepct#00FF00:free memory\\j");
    $ERROR = RRDs::error;
    print "Error in RRD::graph for mem: $ERROR\n" if $ERROR;

    RRDs::graph ("$graphs/swap-$period.png",
                "--start", "-1$period", "-aPNG", "-i", "-z",
                "--alt-y-grid", "-w 300", "-h 50", "-l 0", "-u 100", "-r",
                "--color", "SHADEA#FFFFFF",
                "--color", "SHADEB#FFFFFF",
                "--color", "BACK#FFFFFF",
                "-t swap usage per $period",
                "DEF:used=$rrdlog/mem.rrd:swapused:AVERAGE",
                "DEF:free=$rrdlog/mem.rrd:swapfree:AVERAGE",
                "CDEF:total=used,free,+",
                "CDEF:usedpct=100,used,total,/,*",
                "CDEF:freepct=100,free,total,/,*",
                "AREA:usedpct#0000FF:used swap\\j",
                "STACK:freepct#00FF00:free swap\\j");
    $ERROR = RRDs::error;
    print "Error in RRD::graph for swap: $ERROR\n" if $ERROR;
}

sub updatememdata {
    my ($memused, $memfree, $memshared, $membuffers, $memcache, $swapused, $swapfree);
    if ( ! -e "$rrdlog/mem.rrd") {
      RRDs::create ("$rrdlog/mem.rrd", "--step=300",
                        "DS:memused:ABSOLUTE:600:0:U",
                        "DS:memfree:ABSOLUTE:600:0:U",
                        "DS:memcache:ABSOLUTE:600:0:U",
		        "DS:membuffer:ABSOLUTE:600:0:U",
                        "DS:swapused:ABSOLUTE:600:0:U",
                        "DS:swapfree:ABSOLUTE:600:0:U",
                        "RRA:AVERAGE:0.5:1:576",
                        "RRA:AVERAGE:0.5:6:672",
                        "RRA:AVERAGE:0.5:24:732",
                    "RRA:AVERAGE:0.5:144:1460");
      $ERROR = RRDs::error;
      print "Error in RRD::create for mem: $ERROR\n" if $ERROR;
    }

    my @memdata = `free -b -o`;

    my $temp = $memdata[1];

    chomp( $temp );
    my @tempa = split (/\s+/, $temp);
    $memused    = $tempa [2];
    $memfree    = $tempa [3];
    $memshared  = $tempa [4];
    $membuffers = $tempa [5];
    $memcache   = $tempa [6];

    $temp = $memdata[2];
    chomp( $temp );
    @tempa = split (/\s+/, $temp);
    $swapused = $tempa [2];
    $swapfree = $tempa [3];


  RRDs::update ("$rrdlog/mem.rrd",
                "-t", "memused:memfree:memcache:membuffer:swapused:swapfree",
                "N:$memused:$memfree:$memcache:$membuffers:$swapused:$swapfree");

   $ERROR = RRDs::error;
    print "Error in RRD::update for mem: $ERROR\n" if $ERROR;
}
