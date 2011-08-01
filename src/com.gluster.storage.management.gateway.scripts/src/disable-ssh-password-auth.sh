#!/bin/bash

#-----------------------------------------------------------------------------
# disable-ssh-password-auth.sh
# 	Script for disabling SSH password authentication. This is used by the
# 	management gateway after installing the public key, so that the gluster
#	node can be accessed (using ssh) only from the management gateway.
#-----------------------------------------------------------------------------

CONFIG_FILE="/etc/ssh/sshd_config"
TIMESTAMP=`date +%d%m%Y%H%M%S`
BACKUP_FILE="${CONFIG_FILE}_${TIMESTAMP}"
TEMP_FILE="/tmp/new_sshd_config_${TIMESTAMP}"

# Modify config file to disable password authentication, redirect to a temp file
# TODO: disable only if enabled!
sed "s/^PasswordAuthentication yes$/PasswordAuthentication no/g" ${CONFIG_FILE} > ${TEMP_FILE}

# Secure the file by changing permissions (600)
chmod 600 ${TEMP_FILE}

# Take backup of config file
cp ${CONFIG_FILE} ${BACKUP_FILE}

# Overwrite config file with the modified one
mv ${TEMP_FILE} ${CONFIG_FILE}

# Re-start ssh daemon
/etc/init.d/sshd restart

