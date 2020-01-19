package pl.edu.agh.kt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;

public class Flows {

	private static final Logger logger = LoggerFactory.getLogger(Flows.class);
	
	public static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // in seconds
	public static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
	public static short FLOWMOD_DEFAULT_PRIORITY = 100; 

	protected static boolean FLOWMOD_DEFAULT_MATCH_VLAN = true;
	protected static boolean FLOWMOD_DEFAULT_MATCH_MAC = true;
	protected static boolean FLOWMOD_DEFAULT_MATCH_IP_ADDR = true;
	protected static boolean FLOWMOD_DEFAULT_MATCH_TRANSPORT = true;
	
	private Ethernet eth;
	//private IPv4 ipv4;
	private FloodlightContext cntx;

	public Flows() {
		logger.info("Flows() begin/end");
	}

	public static void sendPacketOut(IOFSwitch sw) {
		// Ethernet
		Ethernet l2 = new Ethernet();
		l2.setSourceMACAddress(MacAddress.of("00:00:00:00:00:01"));
		
		l2.setDestinationMACAddress(MacAddress.BROADCAST);
		l2.setEtherType(EthType.IPv4);
		
		// IP
		IPv4 l3 = new IPv4();
		l3.setSourceAddress(IPv4Address.of("192.168.1.1"));
		l3.setDestinationAddress(IPv4Address.of("192.168.1.255"));
		l3.setTtl((byte) 64);
		l3.setProtocol(IpProtocol.UDP);
		
		// UDP
		UDP l4 = new UDP();
		l4.setSourcePort(TransportPort.of(65003));
		l4.setDestinationPort(TransportPort.of(53));
		
		// Layer 7 data
		Data l7 = new Data();
		l7.setData(new byte[1000]);
		
		// set the payloads of each layer
		l2.setPayload(l3);
		l3.setPayload(l4);
		l4.setPayload(l7);
		
		// serialize
		byte[] serializedData = l2.serialize();
		
		// Create Packet-Out and Write to Switch
		OFPacketOut po = sw.getOFFactory()
		.buildPacketOut()
		.setData(serializedData)
		.setActions(Collections.singletonList((OFAction)sw.getOFFactory().actions().output(
		OFPort.FLOOD, 0xffFFffFF)))
		.setInPort(OFPort.CONTROLLER).build();
		sw.write(po);
	}

	public static void simpleAdd(IOFSwitch sw, OFPacketIn pin, FloodlightContext cntx, OFPort outPort, Ethernet eth) {
		// FlowModBuilder
		OFFlowMod.Builder fmb = sw.getOFFactory().buildFlowAdd();
		
		//Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		;
		IPv4 ipv4 = (IPv4) eth.getPayload();
		// match
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, pin.getInPort());
		
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();
		
		if (FLOWMOD_DEFAULT_MATCH_MAC) {
			mb.setExact(MatchField.ETH_SRC, srcMac).setExact(MatchField.ETH_DST, dstMac);
		}

		if (FLOWMOD_DEFAULT_MATCH_VLAN) {
			if (!vlan.equals(VlanVid.ZERO)) {
				mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
			}
		}
		
		if(ipv4 != null) {
		if(eth.getEtherType() == EthType.IPv4) {
			logger.info("ZZZZ: " + ipv4.getSourceAddress() + " " + ipv4.getProtocol());
			//mb.setExact(MatchField.IPV4_SRC, ipv4.getSourceAddress());
			//mb.setExact(MatchField.IP_PROTO, ipv4.getProtocol());
			//mb.setExact(MatchField.ETH_TYPE, EthType.IPv4).setExact(MatchField.IPV4_SRC, ipv4.getSourceAddress())
			//.setExact(MatchField.IPV4_DST, ipv4.getDestinationAddress());
			
			if (FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
				mb.setExact(MatchField.ETH_TYPE, EthType.IPv4).setExact(MatchField.IPV4_SRC, ipv4.getSourceAddress())
				.setExact(MatchField.IPV4_DST, ipv4.getDestinationAddress());
			}
			
			if (FLOWMOD_DEFAULT_MATCH_TRANSPORT) {
			if (!FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
				mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
			}
			
			if (ipv4.getProtocol() == IpProtocol.TCP) {
				TCP tcp = (TCP) ipv4.getPayload();
				mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP).setExact(MatchField.TCP_SRC, tcp.getSourcePort())
				.setExact(MatchField.TCP_DST, tcp.getDestinationPort());
				//mb.setExact(MatchField.TCP_SRC, tcp.getSourcePort());
				logger.info("TOOO: TCP" );
			} else if (ipv4.getProtocol() == IpProtocol.UDP) {
				UDP udp = (UDP) ipv4.getPayload();
				mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP).setExact(MatchField.UDP_SRC, udp.getSourcePort())
				.setExact(MatchField.UDP_DST, udp.getDestinationPort());
				//mb.setExact(MatchField.UDP_SRC, udp.getSourcePort());
				logger.info("TAAA: UDP" );
			} else if (ipv4.getProtocol() == IpProtocol.ICMP) {
				ICMP icmp = (ICMP) ipv4.getPayload();
				mb.setExact(MatchField.IP_PROTO, IpProtocol.ICMP);
				logger.info("TAAA: ICMP" );
			}
			}} else if (eth.getEtherType() == EthType.ARP) { /*
				 * shallow check for
				 * equality is okay for
				 * EthType
				 */
				mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
			}
		
		} else {
			logger.info("NNNN");
		}
		
		
		Match m = mb.build();
		
		// actions
		OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
		List<OFAction> actions = new ArrayList<OFAction>();
		aob.setPort(outPort);
		aob.setMaxLen(Integer.MAX_VALUE);
		actions.add(aob.build());

		fmb.setMatch(m)
		.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
		.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
		.setBufferId(pin.getBufferId())
		.setOutPort(outPort)
		.setPriority(FLOWMOD_DEFAULT_PRIORITY);
		fmb.setActions(actions);
		
		// write flow to switch
		try {
			sw.write(fmb.build());
			logger.info("Flow from port {} forwarded to port {}; match: {}",
			new Object[] { pin.getInPort().getPortNumber(), outPort.getPortNumber(),
							m.toString() });
		} catch (Exception e) {
			logger.error("error {}", e);
		}
	}

	public static Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {
		// The packet in match will only contain the port number.
		// We need to add in specifics for the hosts we're routing between.
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort);

		if (FLOWMOD_DEFAULT_MATCH_MAC) {
			mb.setExact(MatchField.ETH_SRC, srcMac).setExact(MatchField.ETH_DST, dstMac);
		}

		if (FLOWMOD_DEFAULT_MATCH_VLAN) {
			if (!vlan.equals(VlanVid.ZERO)) {
				mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
			}
		}

		// TODO Detect switch type and match to create hardware-implemented flow
		if (eth.getEtherType() == EthType.IPv4) { /*
													 * shallow check for
													 * equality is okay for
													 * EthType
													 */
			IPv4 ip = (IPv4) eth.getPayload();
			IPv4Address srcIp = ip.getSourceAddress();
			IPv4Address dstIp = ip.getDestinationAddress();

			if (FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
				mb.setExact(MatchField.ETH_TYPE, EthType.IPv4).setExact(MatchField.IPV4_SRC, srcIp)
						.setExact(MatchField.IPV4_DST, dstIp);
			}

			if (FLOWMOD_DEFAULT_MATCH_TRANSPORT) {
				/*
				 * Take care of the ethertype if not included earlier, since
				 * it's a prerequisite for transport ports.
				 */
				if (!FLOWMOD_DEFAULT_MATCH_IP_ADDR) {
					mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
				}

				if (ip.getProtocol().equals(IpProtocol.TCP)) {
					TCP tcp = (TCP) ip.getPayload();
					mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP).setExact(MatchField.TCP_SRC, tcp.getSourcePort())
							.setExact(MatchField.TCP_DST, tcp.getDestinationPort());
				} else if (ip.getProtocol().equals(IpProtocol.UDP)) {
					UDP udp = (UDP) ip.getPayload();
					mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP).setExact(MatchField.UDP_SRC, udp.getSourcePort())
							.setExact(MatchField.UDP_DST, udp.getDestinationPort());
				}
			}
		} else if (eth.getEtherType() == EthType.ARP) { /*
														 * shallow check for
														 * equality is okay for
														 * EthType
														 */
			mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
		}

		return mb.build();
	}
}
