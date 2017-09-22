In order to run this project :

1) Clone the project using the command :

    git clone https://github.com/mari-evi/thesis
    
2) Browse into the project directory (cd thesis/) and build it :

    mvn clean install -DskipTests
    
3) After the build has succeeded, browse cd karaf/target/assembly/bin and execute ./karaf . 

4) Using Mininet, open a network topology. For this project, the topology was created with the following script (thesis.py):


        from mininet.topo import Topo
        import time, re, sys, subprocess, os
        from mininet.cli import CLI
        from mininet.net import Mininet
        from mininet.link import TCLink

        class MyTopo( Topo ):
            "Simple topology example."

            def __init__( self ):
                "Create custom topo."

                # Initialize topology
                Topo.__init__( self )

                # Add hosts and switches
                host1 = self.addHost('h1', mac = '00:00:00:00:00:01')
                host2 = self.addHost('h2', mac = '00:00:00:00:00:02')
                switch1 = self.addSwitch('s1')
                switch2 = self.addSwitch('s2')
                switch3 = self.addSwitch('s3')
                switch4 = self.addSwitch('s4')
                switch5 = self.addSwitch('s5')
                switch6 = self.addSwitch('s6')
                switch7 = self.addSwitch('s7')
                switch8 = self.addSwitch('s8')
                switch9 = self.addSwitch('s9')

                # Add links
                self.addLink(switch1, switch2)
                self.addLink(switch2, switch3)
                self.addLink(switch3, switch4)
                self.addLink(switch4, switch5)
                self.addLink(switch5, switch6)
                self.addLink(switch6, switch7)
                self.addLink(switch7, switch8)
                self.addLink(switch1, switch9)
                self.addLink(switch9, switch8)
                self.addLink(host1, switch1)
                self.addLink(host2, switch8)

                topos = { 'mytopo': ( lambda: MyTopo() ) }


    and was opened using the commands :
 
        sudo mn --custom thesis.py --topo mytopo --controller=remote,ip=10.124.83.197

        	pingall



5) Open the ODL DLUX from the link http://localhost:8181/index.html#/ and select the YANG UI tab at the left sidebar.
From there, select odlexample-->operations and then the RPC you want to use.
