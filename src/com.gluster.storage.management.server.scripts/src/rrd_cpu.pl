#!/usr/bin/perl

use RRDs;

my $rrdlog = '/var/lib/rrd';
my $graphs = '/var/lib/rrd';

updatecpudata();
#updatecpugraph('day');
#updatecpugraph('week');
#updatecpugraph('month');
#updatecpugraph('year');

sub updatecpugraph {
        my $period    = $_[0];

        RRDs::graph ("$graphs/cpu-$period.png",
                "--start", "-1$period", "-aPNG", "-i", "-z",
                "--alt-y-grid", "-w 300", "-h 50", "-l 0", "-u 100", "-r",
                "--color", "SHADEA#FFFFFF",
                "--color", "SHADEB#FFFFFF",
                "--color", "BACK#FFFFFF",
                "-t cpu usage per $period",
                "DEF:user=$rrdlog/cpu.rrd:user:AVERAGE",
                "DEF:system=$rrdlog/cpu.rrd:system:AVERAGE",
                "DEF:idle=$rrdlog/cpu.rrd:idle:AVERAGE",
                
		"CDEF:total=user,system,idle,+,+",
                "CDEF:userpct=100,user,total,/,*",
                "CDEF:systempct=100,system,total,/,*",
                "CDEF:idlepct=100,idle,total,/,*",
                
                "AREA:userpct#0000FF:User cpu usage\\j",
                "STACK:systempct#FF0000:system cpu usage\\j",
                "STACK:idlepct#00FF00:idle cpu usage\\j");
                
    #            "GPRINT:userpct:MAX:maximal user cpu\\:%3.2lf%%",
    #            "GPRINT:userpct:AVERAGE:average user cpu\\:%3.2lf%%",
    #            "GPRINT:userpct:LAST:current user cpu\\:%3.2lf%%\\j",
    #            "GPRINT:systempct:MAX:maximal system cpu\\:%3.2lf%%",
    #            "GPRINT:systempct:AVERAGE:average system cpu\\:%3.2lf%%",
    #            "GPRINT:systempct:LAST:current system cpu\\:%3.2lf%%\\j",
    #            "GPRINT:idlepct:MAX:maximal idle cpu\\:%3.2lf%%",
    #            "GPRINT:idlepct:AVERAGE:average idle cpu\\:%3.2lf%%",
    #            "GPRINT:idlepct:LAST:current idle cpu\\:%3.2lf%%\\j");
        $ERROR = RRDs::error;
        print "Error in RRD::graph for cpu: $ERROR\n" if $ERROR;
}

sub updatecpudata {
        if ( ! -e "$rrdlog/cpu.rrd") {
                RRDs::create ("$rrdlog/cpu.rrd", "--step=300",
                        "DS:user:COUNTER:600:0:U",
                        "DS:system:COUNTER:600:0:U",
                        "DS:idle:COUNTER:600:0:U",
                       
                        "RRA:AVERAGE:0.5:1:576",
                        "RRA:AVERAGE:0.5:6:672",
                        "RRA:AVERAGE:0.5:24:732",
                        "RRA:AVERAGE:0.5:144:1460");
                $ERROR = RRDs::error;
                print "Error in RRD::create for cpu: $ERROR\n" if $ERROR;
        }

        my ($cpu, $user, $nice, $system,$idle);

        open STAT, "/proc/stat";
        while(<STAT>) {
                chomp;
                /^cpu\s/ or next;
                ($cpu, $user, $nice, $system, $idle) = split /\s+/;
                last;
        }
        close STAT;
        $user += $nice;

        RRDs::update ("$rrdlog/cpu.rrd",
                "-t", "user:system:idle", 
                "N:$user:$system:$idle");
        $ERROR = RRDs::error;
        print "Error in RRD::update for cpu: $ERROR\n" if $ERROR;

        print "N:$user:$system:$idle\n";
}
