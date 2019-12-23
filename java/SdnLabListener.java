package pl.edu.agh.kt;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdnLabListener implements IOFMessageListener, IFloodlightModule {
	
	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	
	HashMap<Integer, Integer> processMap = new HashMap<>();
	
	int id = 0;
	
	public SdnLabListener() {
		Runnable run = new Runnable() {
		    public void run() {
		    	id = getId();
	        	System.out.print("f: " + id + "  ");
		    }
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(run, 0, 15, TimeUnit.SECONDS);
	}
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}
	
	@Override
	public String getName() {
		return SdnLabListener.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

/*	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		logger = LoggerFactory.getLogger(SdnLabListener.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		logger.info("******************* START **************************");
	}
	
	
	
	public int getId() {
		int value = 101, temp = 0, id = 0;

		for(int i = 1; i < 7; i++) {
			temp = ApiService.getSwitchIp(i);
			processMap.put(i, temp);
			if(temp < value) {
				System.out.print("MYK  " + value + " " + temp + " " + id + " ");
				value = temp;
				id = i;
			}
		}
		
		System.out.print("mapa" + processMap.toString());
		return id;
	}
	
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage
	msg, FloodlightContext cntx) {
		
		OFPacketIn pin = (OFPacketIn) msg;
		OFPort outPort=OFPort.of(0);
		logger.info("Port: " + pin.getInPort().toString());
		
		if (pin.getInPort() == OFPort.of(1) && sw.getEnabledPortNumbers().size() > 2)
			outPort=OFPort.of(id + 1);
		if(pin.getInPort() == OFPort.of(1) && sw.getEnabledPortNumbers().size() == 2)
			outPort=OFPort.of(2);
		
		if(pin.getInPort() == OFPort.of(2) && sw.getEnabledPortNumbers().size() == 2)
			outPort=OFPort.of(1);
		if (pin.getInPort() == OFPort.of(id + 1) && sw.getEnabledPortNumbers().size() > 2)
			outPort=OFPort.of(1);
				
		Flows.simpleAdd(sw, pin, cntx, outPort);
		Flows.sendPacketOut(sw);
		logger.info("************* NEW PACKET IN *************" + id);
		PacketExtractor extractor = new PacketExtractor();
		extractor.packetExtract(cntx);
		return Command.CONTINUE;
	}
}
