/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/user/user-impl/impl/src/java/org/sakaiproject/user/impl/UserServiceSqlDefault.java $
 * $Id: UserServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

/**
 * methods for accessing user data in a database.
 */
public class UserServiceSqlDefault implements UserServiceSql
{
	/**
	 * return the sql statement which deletes an external user id for a given user from the sakai_user_id_map table.
	 */
	public String getDeleteUserIdSql()
	{
		return "delete from SAKAI_USER_ID_MAP where USER_ID=?";
	}

	/**
	 * return the sql statement which inserts a user id and an external user id into the sakai_user_id_map table.
	 */
	public String getInsertUserIdSql()
	{
		return "insert into SAKAI_USER_ID_MAP (USER_ID, EID) values (?,?)";
	}

	/**
	 * return the sql statement which updates an external user id for a given user in the sakai_user_id_map table.
	 */
	public String getUpdateUserIdSql()
	{
		return "update SAKAI_USER_ID_MAP set EID=? where USER_ID=?";
	}

	/**
	 * return the sql statement which retrieves the external user id for a given user from the sakai_user_id_map table.
	 */
	public String getUserEidSql()
	{
		return "select EID from SAKAI_USER_ID_MAP where USER_ID=?";
	}

	/**
	 * return the sql statement which retrieves the user id for a given user from the sakai_user_id_map table.
	 */
	public String getUserIdSql()
	{
		return "select USER_ID from SAKAI_USER_ID_MAP where EID=?";
	}

	/**
	 * return the sql statement which retrieves the where clause from the sakai_user_id_map table.
	 */
	public String getUserWhereSql()
	{
		return "SAKAI_USER.USER_ID = SAKAI_USER_ID_MAP.USER_ID AND (SAKAI_USER.USER_ID = ? OR UPPER(EID) LIKE UPPER(?) OR EMAIL_LC LIKE ? OR UPPER(FIRST_NAME) LIKE UPPER(?) OR UPPER(LAST_NAME) LIKE UPPER(?))";
	}
}