package com.weisong.infra.mgmt.agent.data;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.weisong.infra.mgmt.agent.model.CPackage;
import com.weisong.infra.mgmt.agent.model.CPackage.Status;
  
public class CTestEBean extends CBaseAgentDataTest {  
  
	@Test
    public void testEBeanConnectivity() {  
    	
    	CPackage e = new CPackage(); 
    	e.setName("name");
    	e.setLocation("location");
    	e.setStatus(Status.running);
    	e.setVersion("1.1");

        // will insert  
        Ebean.save(e);  
        CPackage e2 = Ebean.find(CPackage.class, e.getId());
        Assert.assertEquals(e.getLocation(), e2.getLocation());

        e.setLocation("new location");
          
        // this will update  
        Ebean.save(e);
        
        CPackage e3 = Ebean.find(CPackage.class, e.getId());
        Assert.assertNotSame(e.getLocation(), e2.getLocation());
        Assert.assertEquals(e.getLocation(), e3.getLocation());
        
        Ebean.delete(e);  
                    
        CPackage e4 = Ebean.find(CPackage.class, e.getId());
        Assert.assertNull(e4);

    }  
}
