package jgm.internal.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import jgm.JGlideMon;

public class JgmInfo extends Task {
	public void execute() throws BuildException {
		Project p = getProject();
		
		p.setProperty("jgm.version", JGlideMon.version);
		p.setProperty("jgm.revision", String.valueOf(JGlideMon.revisionNum));
	}
}
