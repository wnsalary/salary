package cn.com.pushworld.salary.ui.target;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import cn.com.infostrategy.to.common.TBUtil;
import cn.com.infostrategy.to.common.WLTConstants;
import cn.com.infostrategy.to.common.WLTLogger;
import cn.com.infostrategy.to.mdata.BillVO;
import cn.com.infostrategy.to.mdata.RefItemVO;
import cn.com.infostrategy.ui.common.AbstractWorkPanel;
import cn.com.infostrategy.ui.common.MessageBox;
import cn.com.infostrategy.ui.common.SplashWindow;
import cn.com.infostrategy.ui.common.UIUtil;
import cn.com.infostrategy.ui.common.WLTButton;
import cn.com.infostrategy.ui.common.WLTSplitPane;
import cn.com.infostrategy.ui.common.WLTTabbedPane;
import cn.com.infostrategy.ui.mdata.BillCardDialog;
import cn.com.infostrategy.ui.mdata.BillCardPanel;
import cn.com.infostrategy.ui.mdata.BillListPanel;
import cn.com.infostrategy.ui.mdata.BillTreePanel;
import cn.com.infostrategy.ui.mdata.BillTreeSelectListener;
import cn.com.infostrategy.ui.mdata.BillTreeSelectionEvent;
import cn.com.pushworld.salary.ui.SalaryServiceIfc;
import cn.com.pushworld.salary.ui.paymanage.RefDialog_Month;

/***
 * 部门定量指标
 * 后来增加了部门的定量指标
 * 生成考核表的时候需要生成但是不需要打分
 * 需要根据导入的数据中自动生成得分
 */
public class QuantifyTargetListWKPanel extends AbstractWorkPanel implements ActionListener, BillTreeSelectListener, ChangeListener {

	private BillTreePanel treePanel; //指标类型树
	private BillListPanel bp_targetlist, listPanel; //指标列表
	private WLTButton btn_add0, btn_edit0, btn_delete0, btn_add, btn_edit, btn_delete, btn_action; //列表上的所有按钮
	private WLTButton btn_tree_add, btn_tree_edit, btn_tree_del; //分类树上的按钮
	private WLTTabbedPane maintab = null;

	public void initialize() {
		bp_targetlist = new BillListPanel("SAL_TARGET_LIST_CODE_QUANTIFY");
		bp_targetlist.setDataFilterCustCondition("type='部门定量指标'");
		initBtn();
		bp_targetlist.QueryDataByCondition(null);
		maintab = new WLTTabbedPane();
		maintab.addTab("全部显示", bp_targetlist);
		JPanel blankPanel = new JPanel(new BorderLayout());
		maintab.addTab("按指标分类显示", blankPanel);
		maintab.addChangeListener(this);
		this.add(maintab);
	}

	public void initBtn() {
		btn_add0 = new WLTButton("新增");
		btn_edit0 = new WLTButton("修改");
		btn_delete0 = new WLTButton("删除");
		btn_action = new WLTButton("演算", UIUtil.getImage("bug_2.png"));
		btn_add0.addActionListener(this);
		btn_edit0.addActionListener(this);
		btn_delete0.addActionListener(this);
		btn_action.addActionListener(this);
		bp_targetlist.addBatchBillListButton(new WLTButton[] { btn_add0, btn_edit0, btn_delete0, btn_action });
		bp_targetlist.repaintBillListButton();
	}

	public WLTSplitPane getWLTSplitPane() {
		treePanel = new BillTreePanel("SAL_TARGET_CATALOG_CODE1");
		treePanel.queryDataByCondition(null);
		treePanel.addBillTreeSelectListener(this);
		btn_tree_add = WLTButton.createButtonByType(WLTButton.TREE_INSERT);
		btn_tree_edit = WLTButton.createButtonByType(WLTButton.TREE_EDIT);
		btn_tree_del = new WLTButton("删除");
		btn_tree_del.addActionListener(this);
		treePanel.addBatchBillTreeButton(new WLTButton[] { btn_tree_add, btn_tree_edit, btn_tree_del });
		treePanel.repaintBillTreeButton();
		listPanel = new BillListPanel("SAL_TARGET_LIST_CODE_QUANTIFY");
		listPanel.setDataFilterCustCondition("type='部门定量指标'");
		listPanel.setBillQueryPanelVisible(false);
		btn_add = new WLTButton("新增");
		btn_edit = new WLTButton("修改");
		btn_delete = new WLTButton("删除");
		btn_add.addActionListener(this);
		btn_edit.addActionListener(this);
		btn_delete.addActionListener(this);
		listPanel.addBatchBillListButton(new WLTButton[] { btn_add, btn_edit, btn_delete });
		listPanel.repaintBillListButton();
		WLTSplitPane splitPane = new WLTSplitPane(WLTSplitPane.HORIZONTAL_SPLIT, treePanel, listPanel);
		splitPane.setDividerLocation(220);
		return splitPane;
	}

	//点击分类树出该分类下的指标, 包含所有叶子节点数据
	public void onBillTreeSelectChanged(BillTreeSelectionEvent _event) {
		if (_event.getCurrSelectedNode().isRoot()) {
			new SplashWindow(this, new AbstractAction() {
				public void actionPerformed(ActionEvent arg0) {
					listPanel.QueryDataByCondition(null);
				}
			});
		} else if (_event.getCurrSelectedVO() == null) {
			listPanel.clearTable();
		} else {
			if (_event.getCurrSelectedNode().isLeaf()) {
				BillVO billVO = _event.getCurrSelectedVO();
				final String catalogID = billVO.getStringValue("id");
				new SplashWindow(this, new AbstractAction() {
					public void actionPerformed(ActionEvent arg0) {
						listPanel.QueryDataByCondition("catalogid=" + catalogID);
					}
				});
			} else {
				final DefaultMutableTreeNode node = _event.getCurrSelectedNode();
				new SplashWindow(this, new AbstractAction() {
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<String> ids = new ArrayList<String>();
						getAllChildIDs(node, ids);
						listPanel.QueryDataByCondition("catalogid in (" + TBUtil.getTBUtil().getInCondition(ids) + ")");
					}
				});
			}
		}
	}

	//递归得出所有叶子节点的ID
	private void getAllChildIDs(DefaultMutableTreeNode node, ArrayList<String> ids) {
		Enumeration<DefaultMutableTreeNode> children = node.children();
		BillVO nodeVO = null;
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = children.nextElement();
			if (child.isLeaf()) { // 是否叶子节点
				nodeVO = treePanel.getBillVOFromNode(child);
				ids.add(nodeVO.getStringValue("id"));
			} else {
				// 非叶子节点则继续递归调用了
				getAllChildIDs(child, ids);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btn_add) {
			onAdd(listPanel, 1);
		} else if (e.getSource() == btn_edit) {
			onEdit(listPanel);
		} else if (e.getSource() == btn_delete) {
			onDelete(listPanel);
		} else if (e.getSource() == btn_edit0) {
			onEdit(bp_targetlist);
		} else if (e.getSource() == btn_delete0) {
			onDelete(bp_targetlist);
		} else if (e.getSource() == btn_add0) {
			onAdd(bp_targetlist, 0);
		} else if (e.getSource() == btn_tree_del) {
			onTreeDelete();
		} else {
			onAction();
		}
	}

	/**
	 * 新增
	 */
	private void onAdd(BillListPanel listPanel, int tab) {
		BillCardPanel cardPanel = null;
		if (tab == 1) {
			BillVO billVO = treePanel.getSelectedVO();
			if (billVO == null) {
				MessageBox.show(this, "请先选择一个指标分类.");
				return;
			}
			if (!treePanel.getSelectedNode().isLeaf()) {
				MessageBox.show(this, "请选择最末级的分类.");
				return;
			}
			//创建一个卡片面板
			cardPanel = new BillCardPanel(listPanel.getTempletVO());
			cardPanel.insertRow();
			cardPanel.setEditableByInsertInit();
			//设置默认值
			cardPanel.setValueAt("catalogid", new RefItemVO(billVO.getStringValue("id"), "", billVO.getStringValue("name")));
		} else {
			//创建一个卡片面板
			cardPanel = new BillCardPanel(listPanel.getTempletVO());
			cardPanel.insertRow();
			cardPanel.setEditableByInsertInit();
		}
		cardPanel.setRealValueAt("type", "部门定量指标");

		//弹出框
		BillCardDialog cardDialog = new BillCardDialog(listPanel, "新增部门定量指标", cardPanel, WLTConstants.BILLDATAEDITSTATE_INSERT);
		cardDialog.setVisible(true);

		//确定返回
		if (cardDialog.getCloseType() == 1) {
			int li_newrow = listPanel.newRow(false); //
			listPanel.setBillVOAt(li_newrow, cardDialog.getBillVO(), false);
			listPanel.setRowStatusAs(li_newrow, WLTConstants.BILLDATAEDITSTATE_INIT); //设置列表该行的数据为初始化状态.
			listPanel.setSelectedRow(li_newrow); //		
			listPanel.refreshCurrSelectedRow();
		}
	}

	/**
	 * 编辑
	 */
	private void onEdit(BillListPanel listPanel) {
		BillVO billVO = listPanel.getSelectedBillVO();
		if (billVO == null) {
			MessageBox.showSelectOne(listPanel);
			return;
		}
		//创建一个卡片面板
		BillCardPanel cardPanel = new BillCardPanel(listPanel.getTempletVO());
		cardPanel.setBillVO(billVO);

		//弹出框
		BillCardDialog dialog = new BillCardDialog(this, "修改部门定量指标", cardPanel, WLTConstants.BILLDATAEDITSTATE_UPDATE);
		dialog.setVisible(true);
		//确定返回
		if (dialog.getCloseType() == 1) {
			if (listPanel.getSelectedRow() == -1) {
			} else {
				listPanel.setBillVOAt(listPanel.getSelectedRow(), dialog.getBillVO());
				listPanel.setRowStatusAs(listPanel.getSelectedRow(), WLTConstants.BILLDATAEDITSTATE_INIT);
				listPanel.refreshCurrSelectedRow();
			}
		}
	}

	/**
	 * 删除
	 */
	private void onDelete(BillListPanel listPanel) {
		BillVO billVO = listPanel.getSelectedBillVO();
		if (billVO == null) {
			MessageBox.showSelectOne(listPanel);
			return;
		}
		listPanel.doDelete(false);
		try {
			UIUtil.executeBatchByDS(null, new String[] { "delete from sal_target_checkeddept where targetid=" + billVO.getPkValue() });
		} catch (Exception e) {
			MessageBox.showException(this, e);
		}
	}

	//指标分类树删除, 连分类以及该分类下的指标一并删除!
	private void onTreeDelete() {
		if (treePanel.getSelectedNode() == null || !treePanel.getSelectedNode().isLeaf()) {
			MessageBox.show(this, "请选择一个末级结点进行删除操作!"); //
			return;
		}
		BillVO billVO = treePanel.getSelectedVO();
		try {
			String count = UIUtil.getStringValueByDS(null, "select count(id) from sal_target_list where catalogid=" + billVO.getStringValue("id"));
			//			if (!"0".equals(count)) {
			//				MessageBox.show(this, "请类型已经被使用,不能删除!"); //
			//				return;
			//			}

			String catalogName = billVO.getStringValue("name");
			String catalogID = billVO.getStringValue("id");
			if (!MessageBox.confirm(this, "要删除的分类【" + catalogName + "】下存在【" + count + "】条指标数据, 请务必谨慎操作!\n确定要永久删除这些数据吗?")) {
				return;
			}

			ArrayList<String> sqlList = new ArrayList<String>();
			sqlList.add("delete from sal_target_catalog where  id=" + catalogID);
			sqlList.add("delete from sal_target_list where catalogid=" + catalogID);
			UIUtil.executeBatchByDS(null, sqlList);
			treePanel.delCurrNode(); //
			treePanel.updateUI();
		} catch (Exception e) {
			MessageBox.showException(this, e);
		}
	}

	public void stateChanged(ChangeEvent e) {
		if (maintab.getSelectedIndex() == 1 && listPanel == null) {
			onClickSecTab();
		}
	}

	private void onClickSecTab() {
		new SplashWindow(this, new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				maintab.getComponentAt(1).add(getWLTSplitPane(), BorderLayout.CENTER);
			}
		});
	}

	/**
	 * 查看日志表的状态是否
	 * @return
	 */
	public boolean checkEditable() {
		return true;
	}

	private void onAction() {
		BillVO targetVO = bp_targetlist.getSelectedBillVO();
		if (targetVO == null) {
			MessageBox.showSelectOne(bp_targetlist);
			return;
		}
		String date = getDate(this);
		if (TBUtil.getTBUtil().isEmpty(date)) {
			return;
		}
		String msg = "";
		try {
			msg = getService().calcOneDeptTargetDL(targetVO.convertToHashVO(), date);
			MessageBox.show(bp_targetlist, msg);
		} catch (Exception e) {
			MessageBox.showException(this, e);
		}

	}

	private SalaryServiceIfc services;

	private SalaryServiceIfc getService() {
		if (services == null) {
			try {
				services = (SalaryServiceIfc) UIUtil.lookUpRemoteService(SalaryServiceIfc.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return services;
	}

	private String getDate(Container _parent) {
		String selectDate = "2013-08";
		try {
			RefDialog_Month chooseMonth = new RefDialog_Month(_parent, "请选择要考核的月份", null, null);
			chooseMonth.initialize();
			chooseMonth.setVisible(true);
			selectDate = chooseMonth.getReturnRefItemVO().getName();
			return selectDate;
		} catch (Exception e) {
			WLTLogger.getLogger(QuantifyTargetListWKPanel.class).error(e);
		}
		return "2013-08";
	}
}
