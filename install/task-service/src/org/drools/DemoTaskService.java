package org.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.task.AccessType;
import org.drools.task.AllowedToDelegate;
import org.drools.task.Attachment;
import org.drools.task.BooleanExpression;
import org.drools.task.Comment;
import org.drools.task.Deadline;
import org.drools.task.Deadlines;
import org.drools.task.Delegation;
import org.drools.task.Escalation;
import org.drools.task.Group;
import org.drools.task.I18NText;
import org.drools.task.Notification;
import org.drools.task.OrganizationalEntity;
import org.drools.task.PeopleAssignments;
import org.drools.task.Reassignment;
import org.drools.task.Status;
import org.drools.task.Task;
import org.drools.task.TaskData;
import org.drools.task.User;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.TaskService;
import org.drools.task.service.TaskServiceSession;
import org.drools.task.service.mina.MinaTaskServer;
import org.drools.SystemEventListenerFactory;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

public class DemoTaskService {
	
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.drools.task");
        TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        TaskServiceSession taskSession = taskService.createSession();
        // Add users
        Map vars = new HashMap();
        Reader reader = new InputStreamReader( DemoTaskService.class.getResourceAsStream( "LoadUsers.mvel" ) );     
        Map<String, User> users = ( Map<String, User> ) eval( reader, vars );   
        for ( User user : users.values() ) {
            taskSession.addUser( user );
        }           
        reader = new InputStreamReader( DemoTaskService.class.getResourceAsStream( "LoadGroups.mvel" ) );      
        Map<String, Group> groups = ( Map<String, Group> ) eval( reader, vars );     
        for ( Group group : groups.values() ) {
            taskSession.addGroup( group );
        }
        // start server
        MinaTaskServer server = new MinaTaskServer(taskService);
        Thread thread = new Thread(server);
        thread.start();
        taskSession.dispose();
    }

    public static Object eval(Reader reader, Map vars) {
        try {
            return eval( readerToString( reader ), vars );
        } catch ( IOException e ) {
            throw new RuntimeException( "Exception Thrown", e );
        }
    }
    
    public static String readerToString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer( 1024 );
        while ( (charValue = reader.read()) != -1 ) {
            //result = result + (char) charValue;
            sb.append( (char) charValue );
        }
        return sb.toString();
    }

    public static Object eval(String str, Map vars) {
        ExpressionCompiler compiler = new ExpressionCompiler( str.trim() );

        ParserContext context = new ParserContext();
        context.addPackageImport( "org.drools.task" );
        context.addPackageImport( "java.util" );
        
        context.addImport( "AccessType", AccessType.class );
        context.addImport( "AllowedToDelegate", AllowedToDelegate.class );
        context.addImport( "Attachment", Attachment.class );
        context.addImport( "BooleanExpression", BooleanExpression.class );
        context.addImport( "Comment", Comment.class );
        context.addImport( "Deadline", Deadline.class );
        context.addImport( "Deadlines", Deadlines.class );
        context.addImport( "Delegation", Delegation.class );
        context.addImport( "Escalation", Escalation.class );
        context.addImport( "Group", Group.class );
        context.addImport( "I18NText", I18NText.class );
        context.addImport( "Notification", Notification.class );
        context.addImport( "OrganizationalEntity", OrganizationalEntity.class );
        context.addImport( "PeopleAssignments", PeopleAssignments.class );
        context.addImport( "Reassignment", Reassignment.class );
        context.addImport( "Status", Status.class );
        context.addImport( "Task", Task.class );
        context.addImport( "TaskData", TaskData.class );
        context.addImport( "TaskSummary", TaskSummary.class );
        context.addImport( "User", User.class );

        return MVEL.executeExpression( compiler.compile( context ), vars );
    }

}
