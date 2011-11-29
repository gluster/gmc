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

updatenetdata();
#updatenetgraph('hour');
#updatenetgraph('day');
#updatenetgraph('week');
#updatenetgraph('month');
#updatenetgraph('year');

sub updatenetgraph {
    my $period    = $_[0];

    foreach $rrdfile (<$rrdlog/network-*.rrd>) {
        RRDs::graph ("$graphs/network-$device-$period.png",
		     "--start", "-1$period", "-aPNG", "-i", "-z",
		     "--alt-y-grid", "-w 800", "-h 400", "-l 0", "-u 10000000", "-r",
		     "--color", "SHADEA#FFFFFF",
		     "--color", "SHADEB#FFFFFF",
		     "--color", "BACK#FFFFFF",
		     "-t $device load per $period",
		     "DEF:received=$rrdfile:received:AVERAGE",
		     "DEF:transmitted=$rrdfile:transmitted:AVERAGE",

		     "LINE2:received#FF0000:received load\\j",
		     "LINE1:transmitted#0000FF:transmitted load\\j");

        $ERROR = RRDs::error;
        print "Error in RRD::graph for network $device: $ERROR\n" if $ERROR;
    }
}

sub updatenetdata {
    open NETDEV, "/proc/net/dev";
    while (<NETDEV>) {
	chomp;
	s/^\s+//;             # remove left side whitespaces
	/:.+/ or next;        # if input line contains ':' else continue
	next if /^lo:/;       # continue if input line starts with 'lo:'

        @tokens1 = split /:/;                 # split with ':'
	$tokens1[1]=~s/^\s+//;                # remove left side whitespaces
        @tokens2 = split(/\s+/, $tokens1[1]); # split with space

        $device = $tokens1[0];
        $received = $tokens2[0];
        $transmitted = $tokens2[8];

	#print "$device, $received, $transmitted \n";

	if ( ! -e "$rrdlog/network-$device.rrd") {
	    RRDs::create ("$rrdlog/network-$device.rrd", "--step=300",
			  "DS:received:COUNTER:600:0:U",
			  "DS:transmitted:COUNTER:600:0:U",

			  "RRA:AVERAGE:0.5:1:576",
			  "RRA:AVERAGE:0.5:6:672",
			  "RRA:AVERAGE:0.5:24:732",
			  "RRA:AVERAGE:0.5:144:1460");
	    $ERROR = RRDs::error;
	    print "Error in RRD::create for device $device: $ERROR\n" if $ERROR;
	}

	RRDs::update ("$rrdlog/network-$device.rrd",
		      "-t", "received:transmitted",
		      "N:$received:$transmitted");
	$ERROR = RRDs::error;
	print "Error in RRD::update for net: $ERROR\n" if $ERROR;
    }
    close NETDEV
}
