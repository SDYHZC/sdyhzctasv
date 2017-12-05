package ext.tasv.rejectOptimization.processor;

import java.util.ArrayList;

import com.ptc.netmarkets.util.beans.NmCommandBean;

public class TableBuilderHelper {
	
	/**
	 * 获取请求参数，只或取其中的一个参数
	 * @author caizg
	 * @date 2017年9月11日 下午2:26:07
	 * @param cb
	 * @param paramKey
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getParam(NmCommandBean cb, String paramKey) {
		Object object = cb.getComboBox().get(paramKey);
		if (object == null) {
			object = cb.getText().get(paramKey);
		}
		if(null == object){
			if(null != cb.getRequest()){
				object = cb.getRequest().getParameter(paramKey);
			}
		}
		if(null == object){
			object = cb.getTextArea().get(paramKey);
		}
		if(null == object){
			object = cb.getChecked().get(paramKey);
		}
		if (object == null) {
			return null;
		}
		if (object instanceof ArrayList) {
			return (String) ((ArrayList) object).get(0);
		} else if (object instanceof String[]) {
			return ((String[]) object)[0];
		} else if (object instanceof String) {
			return (String) object;
		} else {
			return object.toString();
		}
	}
}
