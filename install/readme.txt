Download latest Drools code from SVN: http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/

Run Drools build: in Drools root folder run "mvn -Dmaven.test.skip -Declipse install"
(if it is the first time you do this, this could take a while as it will be downloading eclipse as part of the installation, you can remove the -Declipse part if you're not interested in the Eclipse plugin)

Download and install JBoss AS version 4.2.3.GA
In install/build.xml, change <property name="jboss.home" value="C:/jboss-4.2.3.GA" /> to the location of your JBoss AS installation

Increase the memory that can be used by the application server (especially the PermGen space). To do so, edit the run.conf or run.bat (depending on your OS, Linux/Mac or Windows respectively) in the bin dir of your JBoss AS root folder and add the following line at the end of the file:
JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=256m"

Download Eclipse 3.5.1
In build.xml, change <property name="eclipse.home" value="C:/Progra~1/eclipse-3.5.1" /> to the location of your Eclipse installation

Update datasource configuration if necessary (uses h2 in memory database by default).
[You don't need to change anything to these files if you just want to use this default configuration]
install/db/testDS1-ds.xml
install/db/persistence.xml
install/db/hibernate.cfg.xml

Put database driver jar in install/db/driver directory (e.g. h2-1.1.117.jar, create the driver dir if necessary)

Down BIRT report engine
http://www.eclipse.org/downloads/download.php?file=/birt/downloads/drops/R-R1-2_3_2_2-200906011507/birt-runtime-2_3_2_2.zip
and put it in install/lib dir (create the lib dir if necessary)

in the install dir, run installation script:
ant install.guvnor.into.jboss
ant install.drools-gwt-console.into.jboss
ant install.drools-eclipse.into.eclipse

startup database (e.g. run org.h2.tools.Server.class inside h2-1.1.117.jar)

startup JBoss AS

startup task service (run org.drools.task.service.DemoTaskService as part of the drools-process\drools-process-task project)

startup eclipse
switch to Drools perspective
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

// TODO: how to add forms + graphs
// TODO: register custom work item handlers
