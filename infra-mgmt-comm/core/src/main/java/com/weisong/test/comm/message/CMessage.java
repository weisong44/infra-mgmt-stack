package com.weisong.test.comm.message;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.weisong.test.comm.message.builtin.CCommonMsgs;
import com.weisong.test.comm.message.builtin.CDriverMsgs;
import com.weisong.test.comm.message.builtin.CErrorResponse;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type")
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = CErrorResponse.class, name = "error-response")
  , @JsonSubTypes.Type(value = CCommonMsgs.Ping.Request.class, name = "ping-request")
  , @JsonSubTypes.Type(value = CCommonMsgs.Ping.Response.class, name = "ping-response")
  , @JsonSubTypes.Type(value = CCommonMsgs.Status.class, name = "status-notification")
  , @JsonSubTypes.Type(value = CCommonMsgs.Reset.class, name = "reset-notification")
  , @JsonSubTypes.Type(value = CDriverMsgs.Profile.class, name = "driver-profile-notification")
})
@Data
abstract public class CMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected CMessage() {
	}	
	
	protected CMessage(String srcAddr, String destAddr) {
		this.id = UUID.randomUUID().toString();
		this.srcAddr = srcAddr;
		this.destAddr = destAddr;
	}
	
	/** Random UUID */
	public String id;
	
	/** 
	 * <proto>:<type>//address/id
	 * 
	 *  proto	protocol, e.g. zk, websocket, http, ...
	 *  type	target type, e.g. endpoint, comm-server, comm-client
	 *  address	IP address or hostname
	 *  id		target identifier, locally unique
	 */
	public String srcAddr, destAddr;
	
	public Long timestamp = System.nanoTime();
}
