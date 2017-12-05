/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package ext.tasv.pdfexport;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.tasv.pdfexport.ReportResource")

public final class ReportResource extends WTListResourceBundle 

{   

    //更改通告上批量导出PDF
    @RBEntry("批量导出PDF")
    public static final String PRIVATE_CONSTANT_PDFEXPORT1 = "change.ecnexportpdf.description";
    @RBEntry("批量导出PDF")
    public static final String PRIVATE_CONSTANT_PDFEXPORT2  = "change.ecnexportpdf.title";
    @RBEntry("批量导出PDF")
    public static final String PRIVATE_CONSTANT_PDFEXPORT3  = "change.ecnexportpdf.tooltip";


}
