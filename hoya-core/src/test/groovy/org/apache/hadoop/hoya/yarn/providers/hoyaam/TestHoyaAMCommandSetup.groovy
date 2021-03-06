/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hoya.yarn.providers.hoyaam

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hoya.HoyaKeys
import org.apache.hadoop.hoya.api.ClusterDescription
import org.apache.hadoop.hoya.api.RoleKeys
import org.apache.hadoop.hoya.providers.ProviderRole
import org.apache.hadoop.hoya.providers.hoyaam.HoyaAMClientProvider
import org.apache.hadoop.hoya.tools.ConfigHelper
import org.apache.hadoop.hoya.tools.HoyaUtils
import org.apache.hadoop.hoya.yarn.HoyaTestBase
import org.junit.Before
import org.junit.Test

class TestHoyaAMCommandSetup extends HoyaTestBase implements RoleKeys, HoyaKeys{
  public static final int JVM_HEAP_INDEX = 2
  public static final int JVM_OPT_INDEX = JVM_HEAP_INDEX + 1
  private ClusterDescription clusterSpec
  private HoyaAMClientProvider hoyaAM
  private Configuration roleConf

  @Before
  public void setup() {

    Configuration conf = new Configuration()
    hoyaAM = new HoyaAMClientProvider(conf)
    ClusterDescription clusterSpec = new ClusterDescription()
    this.clusterSpec = clusterSpec
    HashMap<String, String> options = new HashMap<String, String>();
    HoyaUtils.mergeEntries(options, hoyaAM.defaultClusterConfiguration);
    clusterSpec.options = options;

    Map<String, Map<String, String>> clusterRoleMap =
        new HashMap<String, Map<String, String>>();
    Collection<ProviderRole> amRoles = this.hoyaAM.getRoles();
    for (ProviderRole role : amRoles) {
      String roleName = role.name;
      Map<String, String> clusterRole =
          this.hoyaAM.createDefaultClusterRole(roleName);
      // get the command line instance count
      clusterRoleMap.put(roleName, clusterRole);
    }
    clusterSpec.roles = clusterRoleMap;
    roleConf = ConfigHelper.loadMandatoryResource(
        HoyaAMClientProvider.AM_ROLE_CONFIG_RESOURCE);
  }
  
  @Test
  public void testDefaultJVMHeap() throws Throwable {
    List<String> commands = [];
    hoyaAM.addJVMOptions(clusterSpec,commands)
    assert commands[JVM_HEAP_INDEX] == "-Xmx" + roleConf.get(JVM_HEAP)
    assert commands.size() == JVM_HEAP_INDEX +1
  }
  
  @Test
  public void testEmptyJVMHeap() throws Throwable {
    List<String> commands = [];
    clusterSpec.setRoleOpt(ROLE_HOYA_AM, JVM_HEAP, "")
    hoyaAM.addJVMOptions(clusterSpec,commands)
    assert commands.size() == JVM_HEAP_INDEX 
  }
    
  @Test
  public void testUpdatedJVMHeap() throws Throwable {
    List<String> commands = [];
    clusterSpec.setRoleOpt(ROLE_HOYA_AM, JVM_HEAP, "1G")
    hoyaAM.addJVMOptions(clusterSpec,commands)
    assert commands[JVM_HEAP_INDEX] == "-Xmx1G" 
  }
  
      
  @Test
  public void testJVMOpts() throws Throwable {
    List<String> commands = [];
    clusterSpec.setRoleOpt(ROLE_HOYA_AM, JVM_OPTS, "-Dsecurity.disabled=true")
    hoyaAM.addJVMOptions(clusterSpec,commands)
    assert commands[JVM_OPT_INDEX] == "-Dsecurity.disabled=true"
  }
  
  
}
