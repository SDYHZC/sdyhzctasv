 
package ext.hbt.workflow;

import java.util.Locale;

import org.apache.log4j.Logger;

import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.log4j.LogR;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

public class WorkFlowUtil {
	private static final String CLASSNAME = WorkFlowUtil.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	private static  Locale locale;
	static
	{
		try
		{
			locale = SessionHelper.manager.getLocale();
		}
		catch (Throwable throwable)
		{
			throw new ExceptionInInitializerError(throwable);
		}
	}

	/**
	 * 获取指定对象的进程
	 */
	public static WfProcess getProcess(Object obj) throws WTException
	{
		if (obj == null)
			return null;
		Persistable persistable = null;
		if (obj instanceof Persistable)
		{
			persistable = (Persistable) obj;
		}
		else if (obj instanceof ObjectIdentifier)
		{
			persistable = PersistenceHelper.manager.refresh((ObjectIdentifier) obj);
		}
		else if (obj instanceof ObjectReference)
		{
			persistable = ((ObjectReference) obj).getObject();
		}
		if (persistable == null)
			return null;
		if (persistable instanceof WorkItem)
			persistable = ((WorkItem) persistable).getSource().getObject();
		if (persistable instanceof WfActivity)
			persistable = ((WfActivity) persistable).getParentProcess();
		if (persistable instanceof WfConnector)
			persistable = ((WfConnector) persistable).getParentProcessRef().getObject();
		if (persistable instanceof WfBlock)
			persistable = ((WfBlock) persistable).getParentProcess();
		if (persistable instanceof WfProcess)
			return (WfProcess) persistable;
		return null;
	}
	
}
