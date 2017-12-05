package ext.hbt.techdocworkflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.log4j.Logger;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;


public class IsWfReceive {

	private static final Logger logger = LogR.getLogger(IsWfReceive.class
			.getName());


	public static boolean IsDocWfReceive(WTObject pbo, ObjectReference self)
			throws WTException, IOException {

		// 判断流程主对象是否是技术文档
		if (pbo instanceof WTDocument) {
			WTDocument doc = (WTDocument) pbo;
			String docType = getDocType(doc);
			System.out.println("文档类型为"+docType);
			if (docType.equals("技术文档")) {
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	//获取文档类型
	public static String getDocType(WTDocument doc) throws IOException,
			WTException {
		String docType = "";
		Locale locale = SessionHelper.manager.getLocale();
		docType = ClientTypedUtility.getLocalizedTypeName(doc, locale);
		return docType;
	}
}
