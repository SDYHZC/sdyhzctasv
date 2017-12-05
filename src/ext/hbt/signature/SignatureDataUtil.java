package ext.hbt.signature;

import java.beans.PropertyVetoException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVotingEventAudit;

import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.cadx.common.util.AssociateUtilities;

import ext.hbt.iba.IBAConstant;
import ext.hbt.iba.IBAUtil;
import ext.hbt.signature.model.CoordinateItem;
import ext.hbt.reviewobjecttype.model.TypeItem;
import ext.hbt.reviewobjecttype.ObjectTypeConstant;
 

public class SignatureDataUtil {
	private static final Logger logger = LogR.getLogger(SignUtil.class.getName());

	public static ArrayList getAllSubItemOfType(String typename) throws WTException
	{
		ArrayList arraylist = new ArrayList();
		QuerySpec qs = new QuerySpec(CoordinateItem.class);
		SearchCondition sc1 = new SearchCondition(CoordinateItem.class, "targetType", SearchCondition.EQUAL, typename);
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
		{
			arraylist.add(qr.nextElement());
		}
		return arraylist;
	}

	public static CoordinateItem getCoordinateItem(TypeItem typeitem, String type, String name) throws WTException
	{
		CoordinateItem item = null;
		QuerySpec qs = new QuerySpec(CoordinateItem.class);
		SearchCondition sc1 = new SearchCondition(CoordinateItem.class, "targetTypeRef.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(typeitem));
		qs.appendSearchCondition(sc1);
		qs.appendAnd();
		SearchCondition sc2 = new SearchCondition(CoordinateItem.class, "attrType", SearchCondition.EQUAL, type);
		qs.appendSearchCondition(sc2);
		qs.appendAnd();
		SearchCondition sc3 = new SearchCondition(CoordinateItem.class, "attrName", SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc3);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
		{
			item = (CoordinateItem) qr.nextElement();
		}
		return item;
	}

	public static ArrayList getCoordinateItems(TypeItem item) throws WTException
	{
		ArrayList list = new ArrayList();
		QuerySpec qs = new QuerySpec(CoordinateItem.class);
		SearchCondition sc1 = new SearchCondition(CoordinateItem.class, "targetTypeRef.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(item));
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
		{
			list.add(qr.nextElement());
		}
		return list;
	}

	public static ArrayList getAllNeedPrint(WTObject pbo, ObjectReference self) throws WTException
	{
		ArrayList arraylist = new ArrayList();
		// 如果是ECA则获取ECA的产生对象
		if (pbo instanceof WTChangeActivity2)
		{
			WTChangeActivity2 eca = (WTChangeActivity2) pbo;
			QueryResult qr = ChangeHelper2.service.getChangeablesAfter(eca);
			while (qr.hasMoreElements())
			{
				WTObject obj = (WTObject) qr.nextElement();
				if (isNeedPrint(obj))
					arraylist.add(obj);
			}
		}
		// 如果是ECN则获取ECN所有产生的对象
		else if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn);
			while (qr.hasMoreElements()) {
				WTObject obj = (WTObject) qr.nextElement();
				if (isNeedPrint(obj))
					arraylist.add(obj);
			}
		}
		// 如果是文档本身，则获取其自身
		else if (pbo instanceof WTDocument)
		{
			arraylist.add(pbo);
		}
		logger.debug(" getAllNeedPrint==="+arraylist);
		return arraylist;
	}

	// 文档或者二维图纸才需要签字。
	public static boolean isNeedPrint(WTObject obj) throws WTException
	{
		boolean isneed = false;
		if (obj != null)
		{
			if (obj instanceof WTDocument)
			{
				isneed = true;
			}
			else if (obj instanceof EPMDocument)
			{
				EPMDocument epm = (EPMDocument) obj;
				if (AssociateUtilities.isCadDrawing(epm))
				{
					isneed = true;
					logger.debug(" isCadDrawing==="+isneed);
				}
			}
			logger.debug(" obj==="+obj.getDisplayIdentifier()+"  isNeedPrint="+isneed);
		}
		return isneed;
	}

	/**
	 * 获取默认表示法中的PDF文件
	 * 
	 * @param representable
	 * @return ApplicationData
	 * @throws WTException
	 */
	public static ApplicationData getPDFInRep(Representable representable) throws WTException
	{
		logger.debug("   getPDFInRep===" + representable);
		ApplicationData app = null;

		try
		{
			Representation representation = RepresentationHelper.service.getDefaultRepresentation(representable);
			if (representation != null)
			{
				logger.debug("     representation =" + representation.getName());

				representation = (Representation) ContentHelper.service.getContents(representation);

				Vector contents = ContentHelper.getContentListAll(representation);
				for (int i = 0; i < contents.size(); i++)
				{
					ContentItem contentItem = (ContentItem) contents.get(i);
					if (isPdfApp(contentItem))
					{
						app = (ApplicationData) contentItem;
						break;
					}
				}
			}
			return app;
		}
		catch (PropertyVetoException e)
		{
			throw new WTException(e);
		}
	}

	/**
	 * 获取内容中的PDF文件(如果附件中没有满足规则的pdf，则直接获取主文件的pdf)
	 * 
	 * @param contentHolder
	 *            对象
	 * @return ApplicationData
	 * @throws WTException
	 */
	public static ApplicationData getgetPDFInContent(ContentHolder cholder) throws WTException
	{
		logger.debug("   getgetPDFInContent  ===" + cholder);
		ApplicationData app = null;
		QueryResult contents = ContentHelper.service.getContentsByRole(cholder, ContentRoleType.SECONDARY);
		while (contents.hasMoreElements())
		{
			ContentItem cItem = (ContentItem) contents.nextElement();
			logger.debug("   SECONDARY contentItem  ===" + cItem.getDisplayIdentifier());
			if (cItem != null && cItem instanceof ApplicationData)
			{
				ApplicationData tempapp = (ApplicationData) cItem;
				String fileName = tempapp.getFileName();
				logger.debug("      SECONDARY ApplicationData.FileName=" + fileName);
				if (fileName.startsWith(SignatureConstant.PRINT_FILE_SOU) && fileName.toLowerCase().endsWith(".pdf"))
				{
					app = tempapp;
					break;
				}
			}
		}
		if (app == null)
		{
			contents = ContentHelper.service.getContentsByRole(cholder, ContentRoleType.PRIMARY);
			while (contents.hasMoreElements())
			{
				ContentItem cItem = (ContentItem) contents.nextElement();
				logger.debug("   PRIMARY contentItem  ===" + cItem.getDisplayIdentifier());
				if (isPdfApp(cItem))
				{
					app = (ApplicationData) cItem;
					break;
				}
			}
		}
		return app;
	}

	public static boolean isPdfApp(ContentItem item) throws WTException
	{
		boolean ispdf = false;
		if (item != null && item instanceof ApplicationData)
		{
			ApplicationData tempapp = (ApplicationData) item;
			String fileName = tempapp.getFileName();
			logger.debug("      Rep ApplicationData=" + fileName);
			if (fileName.toLowerCase().endsWith(".pdf"))
			{
				ispdf = true;
			}
		}
		return ispdf;
	}

	public static String getSigneObjectType(WTObject obj) throws WTException
	{
		String objtype = "";
		try
		{
			if (obj instanceof EPMDocument)
			{
				EPMDocument epm = (EPMDocument) obj;
				objtype = epm.getAuthoringApplication().toString();
			}
			else if (obj instanceof WTDocument)
			{
				WTDocument doc = (WTDocument) obj;
				String bigtype = ClientTypedUtility.getLocalizedTypeName(doc, Locale.CHINA);
				String smalltype = (String) IBAUtil.getIBAValue(doc, IBAConstant.DOC_SMALL_TYPE);
				if (smalltype != null && smalltype.trim().length() > 0)
					objtype = bigtype + ObjectTypeConstant.TYPE_SPLIT_IN + smalltype;
				else
					objtype = bigtype;
			}
		}
		catch (RemoteException e)
		{
			throw new WTException(e);
		}
		logger.debug("      getSigneObjectType=" + objtype);

		return objtype;
	}

	public static ApplicationData getSourcePdf(WTObject obj) throws WTException
	{
		ApplicationData app = SignatureDataUtil.getPDFInRep((Representable) obj);
		if (app == null)
			app = SignatureDataUtil.getgetPDFInContent((ContentHolder) obj);
		return app;
	}

	public static HashMap getCooConfigByType(String typefullname) throws WTException
	{
		HashMap map = null;
		QuerySpec qs = new QuerySpec(TypeItem.class);
		SearchCondition sc = new SearchCondition(TypeItem.class, TypeItem.FULL_TYPE_NAME, SearchCondition.EQUAL, typefullname);
		qs.appendSearchCondition(sc);
		qs.appendAnd();
		qs.appendOpenParen();
		sc = new SearchCondition(TypeItem.class, TypeItem.ITEM_TYPE, SearchCondition.EQUAL, "1");
		qs.appendSearchCondition(sc);
		qs.appendOr();
		sc = new SearchCondition(TypeItem.class, TypeItem.ITEM_TYPE, SearchCondition.EQUAL, "11");
		qs.appendSearchCondition(sc);
		qs.appendCloseParen();

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
		{
			TypeItem typeitem = (TypeItem) qr.nextElement();
			logger.debug("      getSigneObjectType   typeitem=" + typeitem);
			QuerySpec qs1 = new QuerySpec(CoordinateItem.class);
			SearchCondition sc1 = new SearchCondition(CoordinateItem.class, "targetTypeRef.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(typeitem));
			qs1.appendSearchCondition(sc1);
			QueryResult qr1 = PersistenceHelper.manager.find(qs1);
			while (qr1.hasMoreElements())
			{
				CoordinateItem coorditem = (CoordinateItem) qr1.nextElement();
				if (map == null)
					map = new HashMap();
				map.put(coorditem.getAttrName(), coorditem);
			}
		}
		return map;
	}

	public static HashMap getRoutesInfo(WfProcess process)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		try
		{
			QueryResult qr = NmWorkflowHelper.service.getVotingEventsForProcess(process);
			while (qr.hasMoreElements())
			{
				WfVotingEventAudit voteEvent = (WfVotingEventAudit) qr.nextElement();
				String activityName = voteEvent.getActivityName();
				String timename = activityName + SignatureConstant.ACTIVITY_NAME_AFTR;
				WTUser completby = (WTUser) voteEvent.getUserRef().getPrincipal();
				String completname = completby.getFullName();
				Timestamp complettime = voteEvent.getTimestamp();
				String dateStr = parseTimestampToStr(complettime);
				if (map.get(activityName) != null)
				{
					String oldname = map.get(activityName);
					ArrayList oldarray=converStrToArray(oldname);
					if(oldarray.contains(completname))
						continue;
					String oldtime = map.get(timename);
					map.put(activityName, oldname + SignatureConstant.PRINT_VALUE_SPLIT + completname);
					map.put(timename, oldtime + SignatureConstant.PRINT_VALUE_SPLIT + dateStr);
				}
				else
				{
					map.put(activityName, completname);
					map.put(timename, dateStr);
				}
			}
			logger.debug("      getRoutesInfo=" + map);

		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
		return map;
	}

	public static String parseTimestampToStr(Timestamp ts)
	{
		DateFormat sdf = new SimpleDateFormat(SignatureConstant.DATA_FORMAT);
		String dateStr = sdf.format(ts);
		return dateStr;
	}
	
	public static ArrayList converStrToArray(String str)
	{
		ArrayList array=new ArrayList();
		if(str!=null)
		{
			String splits[]=str.split(SignatureConstant.PRINT_VALUE_SPLIT);
		    for(int i=0;i<splits.length;i++)
		    {
		    	String temp=splits[i];
		    	if(temp!=null&&temp.trim().length()>0)
		    		array.add(temp);
		    }
		}
		return array;
	}
}
