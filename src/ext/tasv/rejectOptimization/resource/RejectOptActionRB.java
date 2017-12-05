package ext.tasv.rejectOptimization.resource;

import wt.util.resource.RBComment;
import wt.util.resource.RBEntry;
import wt.util.resource.RBPseudo;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.tasv.rejectOptimization.resource.RejectOptActionRB")
public class RejectOptActionRB extends WTListResourceBundle{
	
	@RBEntry("choose to reject tab")
    public static final String public_constants_1 = "object.rejectOptimizationTab.description";
//	 @RBEntry("reject")
//	 public static final String public_String_bh1 = "rejectOpt.rejectOpt.title";
	@RBEntry("bohui.png")
	@RBPseudo(false)
	@RBComment("DO NOT TRANSLATE")
    public static final String public_String_bh1 = "rejectOpt.rejectOpt.icon";
	 @RBEntry("reject")
	 public static final String public_String_bh2 = "rejectOpt.rejectOpt.description";
	 @RBEntry("reject")
	 public static final String public_String_bh3 = "rejectOpt.rejectOpt.tooltip";
}
