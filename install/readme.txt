[This script assumes you have Java 1.5+, Maven 2.0.8+ and ANT 1.7+ installed]

Download latest Drools code from SVN: http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/

Run Drools build: in Drools root folder run "mvn -Dmaven.test.skip -Declipse install"
(if it is the first time you do this, this could take a while as it will be downloading
 eclipse as part of the installation, you can remove the -Declipse part if you're not
 interested in the Eclipse plugin)

Download and install JBoss AS version 4.2.3.GA:
If you don't have it installed yet, run installation script:
  ant install.jboss
  This will download and install the server in install/jboss-4.2.3.GA folder
  (if you don't want it to download the zip file, you can put a jboss-4.2.3.GA.zip in the install/lib folder)
In install/build.properties, change the jboss.home property to the location of your JBoss AS installation

Increase the memory that can be used by the application server (especially the PermGen space). To do so,
On linux/mac:
  edit the run.conf in your JBoss AS bin folder and add the following line at the end of the file:
  JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=256m"
On windows: 
  edit the run.bat in your JBoss AS bin folder and change the following line:
  set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m
  to
  set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m -XX:MaxPermSize=256m

Download Eclipse 3.5.1
If you don't have it installed yet, run installation script:
  ant install.eclipse
  This will download and install eclipse in install/eclipse folder
  (if you don't want it to download the eclipse file, you can put the file in the install/lib folder:
    eclipse-SDK-3.5.1-linux-gtk.tar.gz (linux)
    eclipse-SDK-3.5.1-win32.zip (windows)
    eclipse-SDK-3.5.1-macosx-carbon.tar.gz (mac))
In install/build.properties, change the eclipse.home property to the location of your Eclipse installation

in the install dir, run installation script:
ant install.guvnor.into.jboss
ant install.drools-gwt-console.into.jboss
ant install.drools-eclipse.into.eclipse

startup database
ant start.h2

startup JBoss AS
ant start.jboss

startup task service (run org.drools.task.service.DemoTaskService as part of the drools-process\drools-process-task project)

startup eclipse
ant start.eclipse

add existing drools runtime in ${drools.home}\install\runtime (Window - Preferences, Drools - Installed Drools Runtimes, click Add..., name "Drools runtime", Browse to install/runtime directory in drools root and click OK, click OK again, check the created runtime, click OK)
import existing project ${drools.home}\install\sample (File - Import..., General - Existing Projects into Workspace, select install/sample/evaluation and click OK, click Finish)
run example

Add Evaluation.rf to guvnor, package defaultPackage (right-click on evaluation.rf in src/main/rules and select Guvnor - Add..., create a new Guvnor repository connection, under repository fill in /drools-guvnor/org.drools.guvnor.Guvnor/webdav/, click Next, select defaultPackage and click Finish)
open Guvnor http://localhost:8080/drools-guvnor/ (login using any username/password, if it asks about installing samples, click No thanks)
build defaultPackage (under Knowledge Bases, select defaultPackage, click on Build Package)

open gwt-console http://localhost:8080/gwt-console/ (login using krisv/krisv)
start process (Processes - Process overview, Click on Evaluation process and click Start and then OK, fill in username krisv and click Complete and close window)
complete self-evaluation (Tasks - Personal Tasks, click on Performance Evaluation task, click View, fill in evaluation form, click Complete and close window)
logout and login as john (john/john) to complete evaluation (Tasks - Personal Tasks, click on Performance Evaluation task, click View, fill in evaluation form, click Complete and close window)
see reports (Reporting - Report Templates, select Overall Activity Report and click Create Report)

Once you're done playing
stop database: ant stop.h2
stop JBoss AS: ant stop.jboss
and simply close all the rest

// TODO: how to add forms + graphs
// TODO: register custom work item handlers
// TODO: deploy demo task service as a service on the AS?
// TODO: custom logins / task users

// TODO: how to change configuration for another DB?
Update datasource configuration if necessary (uses h2 in memory database by default).
[You don't need to change anything to these files if you just want to use this default configuration]
install/db/testDS1-ds.xml
install/db/persistence.xml
install/db/hibernate.cfg.xml

Put database driver jar in install/db/driver directory (e.g. h2-1.1.117.jar, create the driver dir if necessary)
By default, it will download the h2.jar.