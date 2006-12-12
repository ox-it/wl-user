package org.sakaiproject.user.tool;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;


public class PrivacyBean {
	
	private final String VISIBLE = "privacy_visible";
	private final String HIDDEN = "privacy_hidden";
	private final String HIDE = "privacy_check_hide";
	private final String SHOW = "privacy_check_show";
	private final String SET_SHOW_ALL_STRING = "privacy_show_all_set";
	private final String SET_HIDE_ALL_STRING = "privacy_hide_all_set";
	private final String SHOW_ME="Show Me";
	
	private String checkboxText;
	private boolean changeStatus;
	private String selectedSite;
	private boolean siteSelected = false;
	private String curSite;
	private boolean allChanged = false;
	private String changeAllMsg;
	private boolean noSiteProcessErr = false;

	private SelectItem[] sites;

	/** * Resource bundle messages */
	ResourceLoader msgs = new ResourceLoader("user-tool-prefs");

	/** Inject PrivacyManager */
	private PrivacyManager privacyManager;

	private Log LOG = LogFactory.getLog(PrivacyBean.class);

	public void setPrivacyManager(PrivacyManager privacyManager) {
		this.privacyManager = privacyManager;
	}

	/** ========== Setters/Getters for UI ========== */
	
	public boolean isSiteSelected() {
		return (isMyWorkspace()) ? siteSelected : false;
	}

	public void setSiteSelected(boolean siteSelected) {
		this.siteSelected = siteSelected;
	}

	public boolean getAllChanged() {
		return allChanged;
	}

	public void setAllChanged(boolean allChanged) {
		this.allChanged = allChanged;
	}
	
	public String getChangeAllMsg() {
		return changeAllMsg;
	}

	public boolean isNoSiteProcessErr() {
		return noSiteProcessErr;
	}

	public boolean isChangeStatus() {
		return changeStatus;
	}

	public void setChangeStatus(boolean changeStatus) {
		this.changeStatus = changeStatus;
	}

	public String getSelectedSite() {
		return selectedSite;
	}

	public void setSelectedSite(String selectedSite) {
		this.selectedSite = selectedSite;
	}

	/**
	 * Returns 'visible' or 'hidden' based on status within site
	 */
	public String getCurrentStatus() {
		if (!isMyWorkspace()) {
			curSite = getContextId();
		}

		if (privacyManager.isViewable(curSite, getUserId())) {
			return getMessageBundleString(VISIBLE);
		} 
		else {
			return getMessageBundleString(HIDDEN);
		}
	}

	/**
	 * Return TRUE if privacy set to visible, FALSE if set to hidden
	 */
	public boolean isShow() {
		if (isMyWorkspace()) {
			return privacyManager.isViewable(curSite, getUserId());
		} 
		else {
			return privacyManager.isViewable(getContextId(), getUserId());
		}
	}

	/**
	 * Returns TRUE if on MyWorkspace, FALSE if on a specific site
	 */
	public boolean isMyWorkspace() {

		// get Site id
		String siteId = getContextId();

		if (SiteService.getUserSiteId("admin").equals(siteId))
			return false;

		final boolean where = SiteService.isUserSite(siteId);

		LOG.debug("Result of determinig if My Workspace: " + where);

		return where;
	}

	/**
	 * Returns text currently displayed on checkbox
	 */
	public String getCheckboxText() {
		if (!isMyWorkspace()) {
			curSite = getContextId();
		}

		if (privacyManager.isViewable(curSite, getUserId())) {
			checkboxText = getMessageBundleString(HIDE);
		}
		else {
			checkboxText = getMessageBundleString(SHOW);
		}

		return checkboxText;
	}

	/**
	 * Sets the text for the checkbox
	 */
	public void setCheckboxText(String checkboxText) {
		this.checkboxText = checkboxText;
	}

	/**
	 * Returns an array of SelectItem for MyWorkspace page dropdown list
	 * @return
	 */
	public SelectItem[] getSites() {
		final List mySites = getSiteList();

		sites = new SelectItem[mySites.toArray().length + 1];

		final Iterator siteIter = mySites.iterator();
		int sitesIndex = 1;

		sites[0] = new SelectItem("", "");

		while (siteIter.hasNext()) {
			final Site site = (Site) siteIter.next();
			sites[sitesIndex++] = new SelectItem("/site/" + site.getId(), site.getTitle());
		}

		return sites;
	}

	/** ========== processes iteraction on UI ========== */

	/**
	 * Determines if ready to change status and calls method to do so
	 */
	public String processUpdate() {
		if (isMyWorkspace() && ! siteSelected) {
			noSiteProcessErr = true;
			
			return "main";
		}

		// if user checked the checkbox
		if (changeStatus) {
			processChoice(isMyWorkspace() ? curSite : getContextId(), 
							new Boolean(SHOW_ME.equals(checkboxText)));

			// Reset the checkbox to not checked
			changeStatus = false;
		}
		else {
			FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage("Please check the checkbox " + getCheckboxText() + 
												" in order to change your status."));
		}

		return "main";
	}

	/**
	 * Does the actual setting of the privacy status
	 */
	private void processChoice(String contextId, Boolean status) {
		privacyManager.setViewableState(contextId, getUserId(), status,
				privacyManager.USER_RECORD_TYPE);

	}

	/**
	 * Sets the user's privacy status to Visible for all sites
	 */
	public String processShowAll() {
		List mySites = getSiteList();

		Iterator siteIter = mySites.iterator();

		while (siteIter.hasNext()) {
			Site curSite = (Site) siteIter.next();

			processChoice("/site/" + curSite.getId(), Boolean.TRUE);
		}

		allChanged = true;
		changeAllMsg = getMessageBundleString(SET_SHOW_ALL_STRING);

		// Below so UI shows no site selected
		siteSelected = false;
		selectedSite = "";
		
		return "main";
	}

	/**
	 * Sets the user's privacy status to Hidden for all sites
	 */
	public String processHideAll() {
		List mySites = getSiteList();

		Iterator siteIter = mySites.iterator();

		while (siteIter.hasNext()) {
			Site curSite = (Site) siteIter.next();

			processChoice("/site/" + curSite.getId(), Boolean.FALSE);
		}

		allChanged = true;
		changeAllMsg = getMessageBundleString(SET_HIDE_ALL_STRING);

		// Below so UI shows no site selected
		siteSelected = false;
		selectedSite = "";

		return "main";
	}

	/**
	 * Sets Bean variables to affect display when dropdown list
	 * in MyWorkspace is changed.
	 */
	public void processSiteSelected(ValueChangeEvent e) {
		allChanged = false;
		noSiteProcessErr = false;
		
		if ("".equals((String) e.getNewValue())) {
			siteSelected = false;
		} else {
			curSite = (String) e.getNewValue();
			siteSelected = true;
		}
	}

	/** ========== Utility functions ========== */

	/**
	 * Pulls the message from the message bundle using the name passed in
	 * 
	 * @param key
	 *            The name in the MessageBundle for the message wanted
	 */
	private String getMessageBundleString(String key) {
		return msgs.getString(key);

	}

	/**
	 * Returns a list of sites user has access to (is a member of) 
	 */
	private List getSiteList() {
		return SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
					null, null, null,
					org.sakaiproject.site.api.SiteService.SortType.ID_ASC, null);

	}

	/**
	 * Returns context id (/site/site id)
	 */
	private String getContextId() {
		return "/site/" + ToolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * Returns the current user id
	 */
	public String getUserId() {
		return SessionManager.getCurrentSessionUserId();
	}

}
