#!/bin/bash

/usr/bin/rrd_cpu.pl &
/usr/bin/rrd_mem.pl &
/usr/bin/rrd_net.pl &
wait
