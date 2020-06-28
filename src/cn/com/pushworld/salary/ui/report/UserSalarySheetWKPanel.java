package cn.com.pushworld.salary.ui.report;

import cn.com.infostrategy.ui.common.AbstractWorkPanel;
import cn.com.infostrategy.ui.mdata.BillQueryPanel;
import cn.com.infostrategy.ui.report.BillReportPanel;

/**
 * zzl 员工的绩效报表
 */
public class UserSalarySheetWKPanel extends AbstractWorkPanel {
    private static final long serialVersionUID = -1303612160615140713L;
    private BillQueryPanel billQueryPanel=null;

    public void initialize() {
//        BillReportPanel reportPanel = new BillReportPanel("REPORTQUERY_CODE4",
//                "cn.com.pushworld.salary.bs.report.UserSalarySheetBuilderAdapter");
        billQueryPanel=new BillQueryPanel("REPORTQUERY_CODE4");
        this.add(billQueryPanel);

    }
}
