/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package sois;

import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

/**
 * Print statistics for an average aggregation computation. Statistics printed
 * are defined by {@link IncrementalStats#toString}
 * 
 * @author Alberto Montresor
 * @version $Revision: 1.17 $
 */
public class WriteResults implements Control {

	/**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /** Protocol identifier, obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    
	public WriteResults(String name) {
		pid = Configuration.getPid(name + "." + PAR_PROT);
	}

    public boolean execute() {
        
    	if(CommonState.getPhase() == CDState.POST_SIMULATION)
	    	for(Node node : Network.nodes()){
	    		NodeData nodeData = NodeData.get(node);
	    		System.out.println(nodeData.contributionLevel.getValue());
	    	}
    	
    	return false;
    }
    

}
