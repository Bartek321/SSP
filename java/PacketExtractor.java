package pl.edu.agh.kt;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;

import java.io.*;
import java.net.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PacketExtractor {
	private static final Logger logger= LoggerFactory.getLogger(PacketExtractor.class);
	private FloodlightContext cntx;
	private OFMessage msg;
	protected IFloodlightProviderService floodlightProvider;
	private Ethernet eth;
	private IPv4 ipv4;
	private ARP arp;
	private TCP tcp;
	private UDP udp;
	
	public PacketExtractor() {
		logger.error("AFSDFDSF");
	}
	
	public void extractEth() {
		eth = IFloodlightProviderService.bcStore.get(cntx,
		IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		//logger.info("Frame: src mac {}", eth.getSourceMACAddress());
		//logger.info("Frame: dst mac {}", eth.getDestinationMACAddress());
		//logger.info("Frame: ether_type {}", eth.getEtherType());
		if (eth.getEtherType() == EthType.ARP) {
			arp = (ARP) eth.getPayload();
			//logger.info("arp {}", arp.getProtocolType());
			//logger.info("arp {}", arp.getHardwareType());
			//logger.info("arp {}", arp.getTargetProtocolAddress());
		//extractArp();
		}
		if (eth.getEtherType() == EthType.IPv4) {
			ipv4 = (IPv4) eth.getPayload();
			//logger.info("pv4 {}", ipv4.getDestinationAddress());
			//logger.info("pv4 {}", ipv4.getProtocol());
			
			if (ipv4.getProtocol() == IpProtocol.TCP) {
				TCP tcp = (TCP) ipv4.getPayload();
				//logger.info("tcp {}", tcp.getDestinationPort());
			}
			if (ipv4.getProtocol() == IpProtocol.UDP) {
				UDP udp = (UDP) ipv4.getPayload();
				//logger.info("upd {}", udp.getDestinationPort());
			}
			if (ipv4.getProtocol() == IpProtocol.ICMP) {
				ICMP icmp = (ICMP) ipv4.getPayload();
				//logger.info("icmp {}", icmp.getIcmpType());
			}
			
		}
	}
	
	public void packetExtract(FloodlightContext cntx) {
		this.cntx = cntx;
		extractEth();
		}

}
