/**
 * GlusterUtil.java
 *
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.gluster.storage.management.core.utils;

import java.util.ArrayList;
import java.util.List;

import com.gluster.storage.management.core.constants.CoreConstants;

/**
 *
 */
public class GlusterUtil {
   public static final String HOSTNAMETAG = "hostname:";
   private static final ProcessUtil processUtil = new ProcessUtil();

   /**
    * Extract value of given token from given line. It is assumed that the token, if present, will be of the following form:
    * <code>token: value</code> 
    * @param line Line to be analyzed
    * @param tokenName Token whose value is to be extracted
    * @return Value of the token, if present in the line
    */
   private final String extractToken(String line, String tokenName) {
       if (line.toLowerCase().contains(tokenName)) {
           for(String part : line.split(",")) {
               if (part.toLowerCase().contains(tokenName)) {
                   return part.split(tokenName)[1].trim();
               }
           }
       }
       return null;
   }
                                                                                                                                                                                                                                      
   public List<String> getGlusterServerNames() {                                                                                                                                                                                      
       ProcessResult result = processUtil.executeCommand("gluster", "peer", "status");                                                                                                                                          
       if (!result.isSuccess()) {                                                                                                                                                                                                       
           return null;
       }
                       
       List<String> glusterServerNames = new ArrayList<String>();                                                                                                                                                                     
       for (String line : result.getOutput().split(CoreConstants.NEWLINE)) {                                                                                                                                                                        
           String hostName = extractToken(line, HOSTNAMETAG);                                                                                                                                                                               
           if (hostName != null) {                                                                                                                                                                                                    
               glusterServerNames.add(hostName);                                                                                                                                                                                      
           }                                                                                                                                                                                                                          
       }                                                                                                                                                                                                                              
       return glusterServerNames;                                                                                                                                                                                                     
   }
   
   public ProcessResult addServer( String serverName ) {
       return processUtil.executeCommand("gluster", "peer", "probe", serverName );                                                                                                                                          
   }
                                                                                                                                                                                                                                      
    public static void main(String args[]) {                                                                                                                                                                                          
        List<String> names = new GlusterUtil().getGlusterServerNames();                                                                                                                                                              
        System.out.println(names);                                                                                                                                                                                                    
    }
}

