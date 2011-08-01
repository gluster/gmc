#!/usr/bin/perl

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
	next if /^lo:\s/;     # continue if input line starts with 'lo:'

        @tokens1 = split /:/;
        @tokens2 = split(/\s+/, $tokens1[1]);

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
