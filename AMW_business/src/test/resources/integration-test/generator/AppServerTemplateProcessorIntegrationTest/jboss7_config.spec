# Initial spec file created by autospec ver. 0.8 with rpm 3 compatibility
Summary: Installs a JBoss configuration, rpm created by getJBossCfg
# The Summary: line should be expanded to about here -----^
Name: MobiJBoss_amw_b_01
Version: 1.0 
Release: 20140424200904
Group: Applications/Communications
License: unknown
Source: https://jbossrepo.mobicorp.ch/repos/jboss
BuildRoot: %{_tmppath}/amw_b_01-root
BuildArch: noarch
Requires: MobiJBoss7
Packager: jboss
#Disable Automatic Dependency Processing
AutoReqProv: no

%description
Generic JBoss Configuration rpm

%prep
rm -rf *
mkdir -p data/jboss/server/amw_b_01/deployments
mkdir data/jboss/server/amw_b_01/tmp
mkdir data/jboss/server/amw_b_01/data
mkdir -p data/jboss/server/amw_b_01/configuration/keys
mkdir data/jboss/server/amw_b_01/lib
mkdir data/jboss/server/amw_b_01/log
mkdir -p app/jboss/current
mkdir -p app/jboss/current/modules
mkdir -p home/jboss/
#create start script
echo -e "#!/bin/sh 
/sbin/service jboss \$1 amw_b_01" > jboss_init_amw_b_01.sh
ln -s jboss_init_amw_b_01.sh jboss_init_mobi.sh
chmod 750 *.sh
mv *.sh home/jboss/

#create symlinks
ln -s amw_b_01 mobi
mv mobi data/jboss/server
ln -s deployments deploy
mv deploy data/jboss/server/amw_b_01/

#Copy from SOURCES to BUILD
cp -a $RPM_SOURCE_DIR/* .

#fix rights
find . -type f -exec chmod 640 {} \;
find .  \( -type d -o -name "*.sh" \) -exec chmod 750 {} \;
#in jboss 7 jboss has to modify the configuration
chmod 770 data/jboss/server/amw_b_01
chmod 770 data/jboss/server/amw_b_01/configuration
chmod 660 data/jboss/server/amw_b_01/configuration/standalone.xml
chmod 660 data/jboss/server/amw_b_01/configuration/logging.properties
#fuer die doeploy files etc. braucht jboss Schreibrechte
chmod 770 data/jboss/server/amw_b_01/deployments
chmod -R 775 data/jboss/server/amw_b_01/data

#no build needed

%install
%__cp -a . $RPM_BUILD_ROOT

%clean
[ "$RPM_BUILD_ROOT" != "/" ] && rm -rf "$RPM_BUILD_ROOT"

%files
#all files
%defattr(-,jbosscfg,jboss)
/data/jboss/server
%attr(600,jboss,jboss) /data/jboss/server/amw_b_01/configuration/keys
%dir %attr(700,jboss,jboss) /data/jboss/server/amw_b_01/configuration/keys

#tmp, data, work, log has to exist in the source dir
%defattr(-,jboss,jboss)
/data/jboss/server/amw_b_01/tmp
%config /data/jboss/server/amw_b_01/data
%config /data/jboss/server/amw_b_01/log

%defattr(-,jboss,jboss)
/home/jboss/

%defattr(644,jbossown,jboss,755)
/app/jboss/current/modules

%post
#for jade
sudo -u jboss touch /data/jboss/server/amw_b_01/log/server.log

%changelog
* Fri Apr 08 2011 yves.peter@mobi.ch
* Fri Nov 02 2012 christoph.aymon@mobi.ch
- Initial spec file created by autospec ver. 0.8 with rpm 3 compatibility