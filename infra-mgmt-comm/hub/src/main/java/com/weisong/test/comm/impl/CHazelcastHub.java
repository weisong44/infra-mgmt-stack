package com.weisong.test.comm.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.weisong.test.comm.CComponentType;
import com.weisong.test.comm.CHub;
import com.weisong.test.comm.exception.CException;
import com.weisong.test.comm.transport.pdu.CPdu;

public class CHazelcastHub implements CHub {

	final static private String GROUP_NAME = "james";
	final static private String GROUP_PASSWD = "bond";
	final static private int AGENT_BASE_PORT = 8000;
	final static private int PROXY_BASE_PORT = 7000;
	final static private int ENDPOINT_BASE_PORT = AGENT_BASE_PORT; 
	final static private int DRIVER_BASE_PORT = PROXY_BASE_PORT;
	
	static private class CHubListenerAdaptor implements MessageListener<CPdu> {

		private CHub.Listener listener;

		private CHubListenerAdaptor(CHub.Listener listener) {
			this.listener = listener;
		}

		@Override
		public void onMessage(Message<CPdu> msg) {
			CPdu pdu = msg.getMessageObject();
			if(pdu != null) {
				listener.onHubPdu(pdu);
			}
		}
	}

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private HazelcastInstance hInstance;
	
	public CHazelcastHub(CComponentType type) throws IOException {
		
		String configFileName = String.format("config/hazelcast-%s.xml", type);
		if(new File(configFileName).exists()) {
			logger.info("Using configuration file " + configFileName);
		}
		else {
			configFileName = null;
			logger.info("No config file found, configuration will be generated.");
		}
		
		switch(type) {
		case agent:
		case proxy:
			Config cfg = configFileName != null ?
					new XmlConfigBuilder(configFileName).build()
				:	createConfig(type);
			hInstance = Hazelcast.newHazelcastInstance(cfg);
			break;
		case endpoint:
		case driver:
			ClientConfig clientCfg = configFileName != null ?
					new XmlClientConfigBuilder(configFileName).build()
				:	createClientConfig(type);
			hInstance = HazelcastClient.newHazelcastClient(clientCfg);
			break;
		default:
			throw new RuntimeException(String.format("Type not supported: %s", type));
		}
	}
	
	@Override
	public void publish(String topicName, CPdu pdu) throws CException {
		ITopic<CPdu> topic = hInstance.getTopic(topicName);
		topic.publish(pdu);
	}

	@Override
	public void publish(CPdu pdu) throws CException {
		publish(pdu.destAddr, pdu);
	}

	@Override
	public void register(String topic, CHub.Listener listener) {
		ITopic<CPdu> hTopic = hInstance.getTopic(topic);
		hTopic.addMessageListener(new CHubListenerAdaptor(listener));
	}

	private Config createBaseConfig() {
		Config cfg = new Config();
		cfg.getGroupConfig().setName(GROUP_NAME);
		cfg.getGroupConfig().setPassword(GROUP_PASSWD);
		return cfg;
	}
	
	private Config createConfig(CComponentType type) {
		int basePort;
		if(type == CComponentType.agent) {
			basePort = AGENT_BASE_PORT;
		}
		else if(type == CComponentType.proxy) {
			basePort = PROXY_BASE_PORT;
		}
		else {
			throw new RuntimeException("Invalid component type.");
		}

		Config cfg = createBaseConfig();
		cfg.setInstanceName(type.toString());
		cfg.getNetworkConfig().setPort(basePort + 1);
		MulticastConfig multicast = cfg.getNetworkConfig().getJoin().getMulticastConfig();
		multicast.setEnabled(false);
		TcpIpConfig tcpip = cfg.getNetworkConfig().getJoin().getTcpIpConfig();
		tcpip.setEnabled(true);
		tcpip.addMember("localhost:" + (basePort + 1));
		tcpip.addMember("localhost:" + (basePort + 2));
		tcpip.addMember("localhost:" + (basePort + 3));
		
		//cfg.getManagementCenterConfig().setEnabled(true);
		//cfg.getManagementCenterConfig().setUrl("http://localhost:8080/mancenter");
		
		return cfg;
	}
	
	private ClientConfig createBaseClientConfig() {
		ClientConfig cfg = new ClientConfig();
		cfg.getGroupConfig().setName(GROUP_NAME);
		cfg.getGroupConfig().setPassword(GROUP_PASSWD);
		cfg.getNetworkConfig().setConnectionAttemptLimit(Integer.MAX_VALUE);
		return cfg;
	}
	
	private ClientConfig createClientConfig(CComponentType type) {
		int basePort;
		if(type == CComponentType.endpoint) {
			basePort = ENDPOINT_BASE_PORT;
		}
		else if(type == CComponentType.driver) {
			basePort = DRIVER_BASE_PORT;
		}
		else {
			throw new RuntimeException("Invalid component type.");
		}
		
		ClientConfig cfg = createBaseClientConfig();
		List<String> addresses = cfg.getNetworkConfig().getAddresses();
		addresses.add("localhost:" + (basePort + 1));
		addresses.add("localhost:" + (basePort + 2));
		addresses.add("localhost:" + (basePort + 3));
		
		return cfg;
	}
	
}
