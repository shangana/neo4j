/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.causalclustering.load_balancing.plugins.server_policies;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.causalclustering.load_balancing.filters.IdentityFilter;
import org.neo4j.logging.Log;

import static java.lang.String.format;

public class Policies
{
    public static final String POLICY_KEY = "policy";
    static final String DEFAULT_POLICY_NAME = "default";
    static final Policy DEFAULT_POLICY = new FilteringPolicy( IdentityFilter.as() );

    private final Map<String,Policy> policies = new HashMap<>();

    private final Log log;

    Policies( Log log )
    {
        this.log = log;
    }

    void addPolicy( String policyName, Policy policy )
    {
        Policy oldPolicy = policies.putIfAbsent( policyName, policy );
        if ( oldPolicy != null )
        {
            log.error( format( "Policy name conflict for '%s'.", policyName ) );
        }
    }

    Policy selectFor( Map<String,String> context )
    {
        String policyName = context.get( POLICY_KEY );
        policyName = (policyName != null) ? policyName : DEFAULT_POLICY_NAME;

        Policy selectedPolicy = policies.get( policyName );

        if ( selectedPolicy == null )
        {
            log.warn( format( "Policy definition for '%s' could not be found. Will use built-in default instead.", policyName ) );
            return DEFAULT_POLICY;
        }
        else
        {
            return selectedPolicy;
        }
    }
}
