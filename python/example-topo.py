from mininet.topo import Topo
class MyTopo( Topo ):
	def __init__( self ):
		"Create custom topo."
		# Initialize topology
		Topo.__init__( self )

		# Add hosts and switches
		leftHost = self.addHost( 'h1' )
		rightHost = self.addHost( 'h2' )
		leftSwitch = self.addSwitch( 's1' )
		rightSwitch = self.addSwitch( 's8' )
		topSwitch = self.addSwitch( 's2' )
		topSwitch1 = self.addSwitch( 's3' )
		topSwitch2 = self.addSwitch( 's4' )
		downSwitch = self.addSwitch( 's5' )
		downSwitch1 = self.addSwitch( 's6' )
		downSwitch2 = self.addSwitch( 's7' )
		
		# Add links	
		self.addLink( leftHost, leftSwitch )
		self.addLink( rightSwitch, rightHost )

		self.addLink( leftSwitch, topSwitch )
		self.addLink( leftSwitch, topSwitch1 )
		self.addLink( leftSwitch, topSwitch2 )
		self.addLink( leftSwitch, downSwitch )
		self.addLink( leftSwitch, downSwitch1 )
		self.addLink( leftSwitch, downSwitch2 )
		self.addLink( rightSwitch, topSwitch )
		self.addLink( rightSwitch, topSwitch1 )
		self.addLink( rightSwitch, topSwitch2 )
		self.addLink( rightSwitch, downSwitch )
		self.addLink( rightSwitch, downSwitch1 )
		self.addLink( rightSwitch, downSwitch2 )

topos = { 'mytopo': ( lambda: MyTopo() ) }
