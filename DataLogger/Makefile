# build eFJenergy executable when user executes make
INSTALLDIR=/opt/eFJenergy

eFJenergy: eFJenergy.o rs232.o procstat.o readp1.o
	$(CC) $(LDFLAGS) eFJenergy.o rs232.o readp1.o -o eFJenergy -lm -lmysqlclient -lz -pthread # -luClibc++

eFJenergy.o: eFJenergy.c 
	$(CC) $(CFLAGS) -c  eFJenergy.c 

rs232.o: rs232.c 
	$(CC) $(CFLAGS) -c  rs232.c 

readp1.o: readp1.c
	$(CC) $(CFLAGS) -c  readp1.c 
	
# remove object files and executable when user executes "make clean"
clean:
	rm *.o eFJenergy
	
install: eFJenergy
	mkdir -p $(INSTALLDIR)/bin
	mkdir -p $(INSTALLDIR)/etc/init
	install eFJenergy $(INSTALLDIR)/bin/
	install eFJenergy.init.upstart $(INSTALLDIR)/etc/init
	ln -s $(INSTALLDIR)/etc/init/eFJenergy.init.upstart /etc/init/eFJenergy.conf

uninstall:
	rm /etc/init/eFJenergy.conf
	rm $(INSTALLDIR)/etc/init/eFJenergy.init.upstart
	rm $(INSTALLDIR)/bin/eFJenergy
	rmdir $(INSTALLDIR)/etc/init
	rmdir $(INSTALLDIR)/etc
	rmdir $(INSTALLDIR)/bin
	rmdir $(INSTALLDIR)

