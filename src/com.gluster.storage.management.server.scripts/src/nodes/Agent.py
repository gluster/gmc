#  Copyright (C) 2009 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#
#  Gluster Storage Platform is free software; you can redistribute it
#  and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3 of
#  the License, or (at your option) any later version.
#
#  Gluster Storage Platform is distributed in the hope that it will be
#  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
#  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

import xmpp
import sys
import syslog

class Agent:
    def __init__(self, jidString, jidResource, password, proxySettings=None):
        if jidString and jidResource and password:
            self.password = password
            self.proxySettings = proxySettings
            self.jid = xmpp.protocol.JID(jid=jidString)
            self.jid.setResource(jidResource)
            self.xmppClient = xmpp.Client(self.jid.getDomain(), debug=[])
            self.presenceHandler = None
            self.messageHandler = None
            return
        raise ValueError("jid, resource and password should not be empty")

    def registerPresenceHandler(self, function):
        self.presenceHandler = function

    def registerMessageHandler(self, function):
        self.messageHandler = function

    def __defaultMessageHandler(self, connection, event):
        syslog.syslog(syslog.LOG_DEBUG, 
                      "[Received]: from_jid=%s, type=%s, message=%s, error=%s\n" % 
                      (event.getFrom(), event.getType(), event.getBody(), event.getError()))
        if self.messageHandler:
            self.messageHandler(connection, event)
        else:
            sys.stderr.write("[Message]: from_jid=%s, type=%s, message=%s, error=%s\n" % 
                             (event.getFrom(), event.getType(), event.getBody(), event.getError()))

    def __defaultPresenceHandler(self, connection, event):
        syslog.syslog(syslog.LOG_DEBUG, 
                      "[Presence]: from_jid=%s, type=%s, status=%s, error=%s\n" % 
                      (event.getFrom(), event.getType(), event.getShow(), event.getError()))
        if self.presenceHandler:
            self.presenceHandler(connection, event)
        else:
            sys.stderr.write("[Presence]: from_jid=%s, type=%s, status=%s, error=%s\n" % 
                             (event.getFrom(), event.getType(), event.getShow(), event.getError()))

    def connect(self):
        syslog.syslog("Connecting to server %s\n" % self.jid.getDomain())
        connection = self.xmppClient.connect()
        if not connection:
            syslog.syslog("failed\n")
            if not self.proxySettings:
                return False
            syslog.syslog("Connecting to server %s through proxy server %s, port %s, username %s\n" %
                          (self.jid.getDomain(),
                           self.proxySettings["host"],
                           self.proxySettings["port"],
                           self.proxySettings["user"]))
            connection = self.xmppClient.connect(proxy=self.proxySettings)
            if not connection:
                syslog.syslog("failed\n")
                return False

        syslog.syslog("Authenticating with username %s\n" % self.jid)
        auth = self.xmppClient.auth(self.jid.getNode(),
                                    self.password,
                                    self.jid.getResource())
        if not auth:
            syslog.syslog("failed\n")
            return False
        syslog.syslog("done\n")
        syslog.syslog("connection type is %s.  authentication type is %s\n" % (connection, auth))

        self.xmppClient.RegisterHandler("presence", self.__defaultPresenceHandler)
        self.xmppClient.RegisterHandler("message", self.__defaultMessageHandler)

        self.xmppClient.sendInitPresence()
        return True

    def disconnect(self):
        self.xmppClient.disconnect()

    def processMessage(self, timeout=1):
        return self.xmppClient.Process(timeout)
        #if not self.xmppClient.isConnected():
        #    self.xmppClient.reconnectAndReauth()

    def sendMessage(self, jidString, message, messageType="chat"):
        syslog.syslog(syslog.LOG_DEBUG, 
                      "[send]: from_jid=%s, type=%s, message=%s\n" % 
                      (jidString, messageType, message))
        self.xmppClient.send(xmpp.protocol.Message(to=jidString,
                                                   body=message,
                                                   typ=messageType))

    def getNetworkSocket(self):
        return self.xmppClient.Connection._sock;

    def getRoster(self):
        return self.xmppClient.getRoster()

    def isConnected(self):
        return self.xmppClient.isConnected()
##--end of Agent
